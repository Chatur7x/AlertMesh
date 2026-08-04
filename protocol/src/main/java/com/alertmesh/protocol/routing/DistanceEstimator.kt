package com.alertmesh.protocol.routing

import kotlin.math.*

object DistanceEstimator {
    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0
        val a = sin(dLat / 2).pow(2) + cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun rssiToDistanceBand(rssi: Int): String {
        val refRssi = -59.0
        val n = 2.0
        val dist = 10.0.pow((refRssi - rssi) / (10 * n))
        return when {
            dist < 5 -> "Very Close"
            dist <= 30 -> "Nearby"
            dist <= 100 -> "Some Distance"
            else -> "Far"
        }
    }

    fun barometricHeight(localPressure: Float, remotePressure: Float): Double {
        return 8435.0 * ln(localPressure.toDouble() / remotePressure.toDouble())
    }
}
