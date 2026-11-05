package com.example.util.prayer

import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalTime
import org.json.JSONObject

// ponytail: lightweight aladhan.com REST client, stdlib-only, no OkHttp
object AladhanApiClient {

    private const val BASE_URL = "https://api.aladhan.com/v1/timings"

    private var cachedTimes: CalculatedPrayerTimes? = null
    private var cacheKey: String = ""

    fun clearCache() {
        cachedTimes = null
        cacheKey = ""
    }

    private fun methodToApiId(method: CalculationMethod): Int = when (method) {
        CalculationMethod.UMM_AL_QURA -> 4
        CalculationMethod.EGYPTIAN -> 5
        CalculationMethod.KARACHI -> 1
        CalculationMethod.ISNA -> 2
        CalculationMethod.MUSLIM_WORLD_LEAGUE -> 3
        CalculationMethod.DIYANET -> 13
        CalculationMethod.GULF -> 8
        CalculationMethod.ALGERIA -> 19 // Algerian Ministry of Religious Affairs and Wakfs (18° Fajr / 17° Isha)
    }

    // ponytail: in-memory daily cache keyed on lat/lng/method/date
    fun getCachedOrFetch(
        latitude: Double,
        longitude: Double,
        method: CalculationMethod = CalculationMethod.UMM_AL_QURA
    ): CalculatedPrayerTimes? {
        val key = "${latitude.toInt()}_${longitude.toInt()}_${method.id}_${LocalDate.now()}"
        if (key == cacheKey && cachedTimes != null) return cachedTimes
        val result = fetchPrayerTimesDirect(latitude, longitude, method)
        if (result != null) {
            cachedTimes = result
            cacheKey = key
        }
        return result
    }

    fun fetchPrayerTimesDirect(
        latitude: Double,
        longitude: Double,
        method: CalculationMethod,
        timezoneId: String = java.time.ZoneId.systemDefault().id
    ): CalculatedPrayerTimes? {
        return try {
            val timestamp = System.currentTimeMillis() / 1000
            val tz = if (timezoneId.isNotBlank()) timezoneId else java.time.ZoneId.systemDefault().id
            val url = URL("$BASE_URL/$timestamp?latitude=$latitude&longitude=$longitude&method=${methodToApiId(method)}&timezonestring=$tz&school=0")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"

            if (conn.responseCode != 200) {
                conn.disconnect()
                return null
            }

            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val timings = JSONObject(body).getJSONObject("data").getJSONObject("timings")

            CalculatedPrayerTimes(
                fajr = parseTime(timings.getString("Fajr")),
                sunrise = parseTime(timings.getString("Sunrise")),
                dhuhr = parseTime(timings.getString("Dhuhr")),
                asr = parseTime(timings.getString("Asr")),
                sunset = parseTime(timings.getString("Sunset")),
                maghrib = parseTime(timings.getString("Maghrib")),
                isha = parseTime(timings.getString("Isha")),
                qiyamSuggested = parseTime(timings.optString("Lastthird", "03:00"))
            )
        } catch (_: Exception) {
            null
        }
    }

    // ponytail: parse "HH:mm" or "HH:mm (EET)" format from aladhan response
    private fun parseTime(timeStr: String): LocalTime {
        val clean = timeStr.split(" ").first().trim()
        val parts = clean.split(":")
        return LocalTime.of(parts[0].toInt(), parts[1].toInt())
    }
}
