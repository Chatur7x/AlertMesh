package com.alertmesh.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.alertmesh.AlertMeshApp
import com.alertmesh.R
import com.alertmesh.protocol.model.PresencePayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MeshService : Service() {
    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var transportStarted = false

    inner class LocalBinder : Binder() {
        fun getService(): MeshService = this@MeshService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun getBatteryPercent(): Int {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            (level * 100f / scale).toInt()
        } else {
            0
        }
    }

    private fun createPresencePayload(app: AlertMeshApp): PresencePayload {
        val location = app.container.locationManager.getCurrentLocation()
        return PresencePayload(
            deviceId = app.container.cryptoProvider.getDeviceId(),
            fingerprint = app.container.cryptoProvider.getFingerprint(),
            publicKey = app.container.cryptoProvider.getPublicKey(),
            hybridPublicKey = app.container.cryptoProvider.getHybridPublicKey(),
            batteryPercent = getBatteryPercent(),
            hopCount = 0,
            neighborCount = 0,
            timestamp = System.currentTimeMillis(),
            latitude = location?.latitude,
            longitude = location?.longitude,
            accuracy = location?.accuracy,
            hasSensor = app.container.barometerManager.hasBarometer,
            pressureHpa = app.container.barometerManager.currentPressureHpa
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, "mesh_channel")
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_alertmesh_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)

        if (!allPermissionsGranted()) {
            Log.w("MeshService", "Required permissions not granted; starting service without transport")
            return START_STICKY
        }

        val app = application as AlertMeshApp
        val engine = app.container.protocolEngine

        serviceScope.launch {
            engine.start()
            if (!transportStarted) {
                transportStarted = true
                try {
                    app.container.transport.start()
                } catch (e: Exception) {
                    Log.e("MeshService", "Transport start failed", e)
                    transportStarted = false
                }
            }
        }

        app.container.locationManager.startLocationUpdates()
        app.container.barometerManager.startListening()
        engine.startPresenceBroadcast { createPresencePayload(app) }

        if (getBatteryPercent() < 20) {
            engine.presenceIntervalMs = 30_000
        } else {
            engine.presenceIntervalMs = 10_000
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        val app = application as AlertMeshApp
        runBlocking {
            try { app.container.transport.stop() } catch (_: Exception) {}
        }
        app.container.locationManager.stopLocationUpdates()
        app.container.barometerManager.stopListening()
        app.container.protocolEngine.stopPresenceBroadcast()
        app.container.protocolEngine.stop()
        serviceScope.cancel()
        transportStarted = false
    }

    private fun allPermissionsGranted(): Boolean {
        var permissions = mutableListOf<String>(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.addAll(listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.addAll(listOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ))
        }
        return permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("mesh_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
