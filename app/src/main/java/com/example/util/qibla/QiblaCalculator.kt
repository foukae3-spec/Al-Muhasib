package com.example.util.qibla

import kotlin.math.*

object QiblaCalculator {

    // Holy Kaaba exact coordinates in Makkah Al-Mukarramah
    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculates the forward azimuth bearing (0 to 360 degrees clockwise from True North)
     * pointing directly towards the Holy Kaaba from given observer coordinates.
     */
    fun calculateQiblaBearing(latitude: Double, longitude: Double): Double {
        val lat1Rad = Math.toRadians(latitude)
        val lat2Rad = Math.toRadians(KAABA_LATITUDE)
        val deltaLngRad = Math.toRadians(KAABA_LONGITUDE - longitude)

        val y = sin(deltaLngRad) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(deltaLngRad)

        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad)
        return (bearingDeg + 360.0) % 360.0
    }

    /**
     * Calculates great-circle distance from observer to the Holy Kaaba in kilometers.
     */
    fun calculateDistanceToKaabaKm(latitude: Double, longitude: Double): Double {
        val lat1Rad = Math.toRadians(latitude)
        val lat2Rad = Math.toRadians(KAABA_LATITUDE)
        val deltaLatRad = Math.toRadians(KAABA_LATITUDE - latitude)
        val deltaLngRad = Math.toRadians(KAABA_LONGITUDE - longitude)

        val a = sin(deltaLatRad / 2.0).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltaLngRad / 2.0).pow(2)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Checks if current heading is aligned with Qibla bearing within tolerance (default +- 2.5 degrees).
     */
    fun isAligned(currentHeading: Float, qiblaBearing: Double, toleranceDegrees: Float = 2.5f): Boolean {
        val diff = abs((currentHeading - qiblaBearing + 540.0) % 360.0 - 180.0)
        return diff <= toleranceDegrees
    }
}
