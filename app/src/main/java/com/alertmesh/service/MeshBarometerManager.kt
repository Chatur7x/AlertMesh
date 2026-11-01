package com.alertmesh.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MeshBarometerManager(context: Context) : SensorEventListener {
    private val TAG = "AlertMesh-Barometer"
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
    private val pressureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)

    var hasBarometer: Boolean = pressureSensor != null
        private set

    var currentPressureHpa: Float? = null
        private set
    private val _pressureHpa = MutableStateFlow<Float?>(null)
    val pressureHpa = _pressureHpa.asStateFlow()
    private val _pressureHistory = MutableStateFlow<List<Float>>(emptyList())
    val pressureHistory = _pressureHistory.asStateFlow()
    private var lastPublishedMs = 0L

    fun startListening() {
        if (hasBarometer && pressureSensor != null) {
            sensorManager?.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Barometer sensor listening started")
        } else {
            Log.d(TAG, "Barometer sensor not available on this device")
        }
    }

    fun stopListening() {
        if (hasBarometer) {
            sensorManager?.unregisterListener(this)
            Log.d(TAG, "Barometer sensor listening stopped")
        }
        currentPressureHpa = null
        _pressureHpa.value = null
        _pressureHistory.value = emptyList()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_PRESSURE) {
            val pressure = event.values[0]
            if (pressure !in 300f..1100f) return
            currentPressureHpa = pressure
            val now = SystemClock.elapsedRealtime()
            if (now - lastPublishedMs >= 1000L) {
                lastPublishedMs = now
                _pressureHpa.value = pressure
                _pressureHistory.value = (_pressureHistory.value + pressure).takeLast(60)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
