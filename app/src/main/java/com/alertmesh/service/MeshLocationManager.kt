package com.alertmesh.service

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

data class LocationData(val latitude: Double, val longitude: Double, val accuracy: Float, val fixAgeMs: Long)

class MeshLocationManager(context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var lastLatitude: Double = 0.0
    private var lastLongitude: Double = 0.0
    private var lastAccuracy: Float = 0f
    private var lastFixTime: Long = 0L

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                lastLatitude = location.latitude
                lastLongitude = location.longitude
                lastAccuracy = location.accuracy
                lastFixTime = System.currentTimeMillis()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30_000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(30_000)
            .build()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun getCurrentLocation(): LocationData? {
        if (lastFixTime == 0L) return null
        val fixAgeMs = System.currentTimeMillis() - lastFixTime
        return LocationData(lastLatitude, lastLongitude, lastAccuracy, fixAgeMs)
    }
}
