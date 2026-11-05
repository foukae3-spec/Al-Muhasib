package com.example.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// ponytail: pure standard library spherical trigonometry Qibla compass bearing helper
object QiblaHelper {
    const val KAABA_LATITUDE = 21.4225
    const val KAABA_LONGITUDE = 39.8262

    /**
     * Calculates great-circle forward azimuth (bearing) from user coordinates (lat, lon)
     * towards the Kaaba (21.4225° N, 39.8262° E).
     * Returns compass bearing normalized to [0.0, 360.0) degrees.
     */
    fun calculateQiblaBearing(userLat: Double, userLon: Double): Double {
        val phi1 = Math.toRadians(userLat)
        val phi2 = Math.toRadians(KAABA_LATITUDE)
        val deltaLambda = Math.toRadians(KAABA_LONGITUDE - userLon)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)

        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad)
        return ((bearingDeg % 360.0) + 360.0) % 360.0
    }
}
