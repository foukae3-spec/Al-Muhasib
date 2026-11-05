package com.example.util.prayer

import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.*

/**
 * High-precision mathematical astronomical calculator for Islamic Prayer Times.
 * Implements pure solar geometry algorithms:
 * - Julian Day calculation
 * - Solar coordinates (Declination, Equation of Time)
 * - Hour Angle equations with atmospheric refraction and sun radius correction
 * - High latitude adjustments (Mid-Night, One-Seventh, Angle-Based)
 * 100% offline, zero network requests, zero external dependencies.
 */
object SolarCalculator {

    private const val DEG_TO_RAD = Math.PI / 180.0
    private const val RAD_TO_DEG = 180.0 / Math.PI

    /**
     * Compute full prayer schedule for given coordinates, date, and method conventions.
     */
    fun calculate(
        latitude: Double,
        longitude: Double,
        timezoneOffsetHours: Double,
        date: LocalDate = LocalDate.now(),
        method: CalculationMethod = CalculationMethod.EGYPTIAN,
        juristicMethod: JuristicMethod = JuristicMethod.SHAFI_MALIKI_HANBALI,
        highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MID_NIGHT,
        manualOffsetsMinutes: Map<String, Int> = emptyMap()
    ): CalculatedPrayerTimes {
        val jd = julianDay(date.year, date.monthValue, date.dayOfMonth)
        val d = jd - 2451545.0 // Julian centuries/days from J2000.0

        // Solar coordinates
        val (declination, eqTime) = solarCoordinates(d)

        // Midday (Dhuhr) in local civil decimal hours
        val dhuhrCivilHours = 12.0 + timezoneOffsetHours - (longitude / 15.0) - (eqTime / 60.0)

        // Sunrise & Sunset solar altitude (-0.8333 degrees: 0.533 deg sun diameter + 0.300 deg atmospheric refraction)
        val sunriseAngle = -0.8333
        val sunriseHourAngle = hourAngle(latitude, declination, sunriseAngle)
        val sunsetHourAngle = sunriseHourAngle

        // Fajr Hour Angle
        val fajrHourAngle = hourAngle(latitude, declination, -method.fajrAngle)

        // Asr Hour Angle (positive solar altitude above horizon)
        val asrAngle = acot(juristicMethod.shadowFactor + tan(abs(latitude - declination) * DEG_TO_RAD)) * RAD_TO_DEG
        val asrHourAngle = hourAngle(latitude, declination, asrAngle)

        // Isha Hour Angle (if not interval-based)
        val ishaHourAngle = if (method.ishaIntervalMinutes == null) {
            hourAngle(latitude, declination, -method.ishaAngle)
        } else null

        // Initial Raw Decimal Hours
        var fajrRaw = dhuhrCivilHours - (fajrHourAngle / 15.0)
        val sunriseRaw = dhuhrCivilHours - (sunriseHourAngle / 15.0)
        val dhuhrRaw = dhuhrCivilHours
        val asrRaw = dhuhrCivilHours + (asrHourAngle / 15.0)
        val sunsetRaw = dhuhrCivilHours + (sunsetHourAngle / 15.0)
        val maghribRaw = sunsetRaw

        var ishaRaw = if (method.ishaIntervalMinutes != null) {
            maghribRaw + (method.ishaIntervalMinutes / 60.0)
        } else {
            dhuhrCivilHours + ((ishaHourAngle ?: 0.0) / 15.0)
        }

        // High Latitude Adjustments (e.g. northern latitudes in summer where night is too short)
        val nightDurationHours = (24.0 - sunsetRaw + fajrRaw).let { if (it < 0) it + 24.0 else it }
        if (nightDurationHours < 24.0) {
            when (highLatitudeRule) {
                HighLatitudeRule.MID_NIGHT -> {
                    val halfNight = nightDurationHours / 2.0
                    val minFajr = sunriseRaw - halfNight
                    if (fajrRaw < minFajr) fajrRaw = minFajr

                    val maxIsha = sunsetRaw + halfNight
                    if (ishaRaw > maxIsha) ishaRaw = maxIsha
                }
                HighLatitudeRule.ONE_SEVENTH -> {
                    val portion = nightDurationHours / 7.0
                    val minFajr = sunriseRaw - portion
                    if (fajrRaw < minFajr) fajrRaw = minFajr

                    val maxIsha = sunsetRaw + portion
                    if (ishaRaw > maxIsha) ishaRaw = maxIsha
                }
                HighLatitudeRule.ANGLE_BASED -> {
                    val portionFajr = (method.fajrAngle / 60.0) * nightDurationHours
                    val minFajr = sunriseRaw - portionFajr
                    if (fajrRaw < minFajr) fajrRaw = minFajr

                    val ishaAng = if (method.ishaIntervalMinutes != null) 18.0 else method.ishaAngle
                    val portionIsha = (ishaAng / 60.0) * nightDurationHours
                    val maxIsha = sunsetRaw + portionIsha
                    if (ishaRaw > maxIsha) ishaRaw = maxIsha
                }
                HighLatitudeRule.NONE -> { /* No adjustment */ }
            }
        }

        // Apply manual minute offsets if configured
        val fajrFinal = decimalHoursToLocalTime(fajrRaw + (manualOffsetsMinutes["Fajr"] ?: 0) / 60.0)
        val sunriseFinal = decimalHoursToLocalTime(sunriseRaw + (manualOffsetsMinutes["Sunrise"] ?: 0) / 60.0)
        val dhuhrFinal = decimalHoursToLocalTime(dhuhrRaw + (manualOffsetsMinutes["Dhuhr"] ?: 0) / 60.0)
        val asrFinal = decimalHoursToLocalTime(asrRaw + (manualOffsetsMinutes["Asr"] ?: 0) / 60.0)
        val sunsetFinal = decimalHoursToLocalTime(sunsetRaw)
        val maghribFinal = decimalHoursToLocalTime(maghribRaw + (manualOffsetsMinutes["Maghrib"] ?: 0) / 60.0)
        val ishaFinal = decimalHoursToLocalTime(ishaRaw + (manualOffsetsMinutes["Isha"] ?: 0) / 60.0)

        // Suggested Qiyam Al-Layl (Last third of the night)
        val nightDurationMinutes = java.time.Duration.between(maghribFinal, fajrFinal.plusHours(24)).toMinutes() % 1440
        val lastThirdMinutes = (nightDurationMinutes / 3)
        val qiyamTime = fajrFinal.minusMinutes(lastThirdMinutes)

        return CalculatedPrayerTimes(
            fajr = fajrFinal,
            sunrise = sunriseFinal,
            dhuhr = dhuhrFinal,
            asr = asrFinal,
            sunset = sunsetFinal,
            maghrib = maghribFinal,
            isha = ishaFinal,
            qiyamSuggested = qiyamTime
        )
    }

    // ─── Mathematical Helpers ───────────────────────────────────────────────

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun solarCoordinates(d: Double): Pair<Double, Double> {
        val q = fixAngle(280.459 + 0.98564736 * d)
        val g = fixAngle(357.529 + 0.98560028 * d)
        val l = fixAngle(q + 1.915 * sin(g * DEG_TO_RAD) + 0.020 * sin(2 * g * DEG_TO_RAD))

        val e = 23.439 - 0.00000036 * d
        val sinL = sin(l * DEG_TO_RAD)
        val declination = asin(sin(e * DEG_TO_RAD) * sinL) * RAD_TO_DEG

        val raDeg = atan2(cos(e * DEG_TO_RAD) * sinL, cos(l * DEG_TO_RAD)) * RAD_TO_DEG
        val raHours = fixHour(raDeg / 15.0)

        var eqTime = (q / 15.0) - raHours
        if (eqTime > 12.0) eqTime -= 24.0
        if (eqTime < -12.0) eqTime += 24.0

        return Pair(declination, eqTime * 60.0) // Declination (deg), EqTime (min)
    }

    private fun hourAngle(latitude: Double, declination: Double, angle: Double): Double {
        val latRad = latitude * DEG_TO_RAD
        val decRad = declination * DEG_TO_RAD
        val angRad = angle * DEG_TO_RAD

        val cosH = (sin(angRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val clampedCosH = cosH.coerceIn(-1.0, 1.0)
        return acos(clampedCosH) * RAD_TO_DEG
    }

    private fun decimalHoursToLocalTime(hoursDec: Double): LocalTime {
        var normalized = hoursDec % 24.0
        if (normalized < 0.0) normalized += 24.0

        // Round to the nearest whole minute according to official prayer clock standards
        val totalMinutes = kotlin.math.round(normalized * 60.0).toInt()
        val hour = (totalMinutes / 60) % 24
        val minute = totalMinutes % 60

        return LocalTime.of(hour, minute, 0)
    }

    private fun fixAngle(a: Double): Double {
        val res = a % 360.0
        return if (res < 0) res + 360.0 else res
    }

    private fun fixHour(h: Double): Double {
        val res = h % 24.0
        return if (res < 0) res + 24.0 else res
    }

    private fun acot(x: Double): Double {
        return atan(1.0 / x)
    }
}
