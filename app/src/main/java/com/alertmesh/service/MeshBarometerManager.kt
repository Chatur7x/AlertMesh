package com.alertmesh.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class MeshBarometerManager(context: Context) : SensorEventListener {
    private val TAG = "AlertMesh-Barometer"
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
    private val pressureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)

    var hasBarometer: Boolean = pressureSensor != null
        private set

    var currentPressureHpa: Float? = null
        private set

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
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_PRESSURE) {
            currentPressureHpa = event.values[0]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
