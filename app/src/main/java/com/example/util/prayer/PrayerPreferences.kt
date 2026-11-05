package com.example.util.prayer

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.ZoneId
import java.time.ZonedDateTime

data class UserPrayerSettings(
    val cityId: String,
    val cityName: String,
    val countryName: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val isAutoGps: Boolean,
    val calculationMethod: CalculationMethod,
    val juristicMethod: JuristicMethod,
    val highLatitudeRule: HighLatitudeRule,
    val hijriOffsetDays: Int,
    val preAdhanMinutes: Int, // 0 for disabled, 10 or 15
    val adhanToneId: String // "abdulbasit", "makkah", "madinah", "alaqsa", "egypt"
)

data class SunniAdhanOption(
    val id: String,
    val arabicName: String,
    val reciterName: String,
    val rawResId: Int
)

val SUNNI_ADHANS = listOf(
    SunniAdhanOption("abdulbasit", "أذان الشيخ عبد الباسط عبد الصمد", "عبد الباسط عبد الصمد", com.example.R.raw.adhan_abdulbasit),
    SunniAdhanOption("makkah", "أذان الحرم المكي الشريف", "مكة المكرمة", com.example.R.raw.adhan_makkah),
    SunniAdhanOption("madinah", "أذان المسجد النبوي الشريف", "المدينة المنورة", com.example.R.raw.adhan_madinah),
    SunniAdhanOption("alaqsa", "أذان المسجد الأقصى المبارك", "القدس الشريف", com.example.R.raw.adhan_alaqsa),
    SunniAdhanOption("egypt", "أذان جمهورية مصر العربية", "القاهرة", com.example.R.raw.adhan_egypt)
)

object PrayerPreferences {

    private const val PREFS_NAME = "al_muhasib_prayer_prefs"

    private const val KEY_CITY_ID = "pref_prayer_city_id"
    private const val KEY_CITY_NAME = "pref_prayer_city_name"
    private const val KEY_COUNTRY_NAME = "pref_prayer_country_name"
    private const val KEY_LATITUDE = "pref_prayer_lat"
    private const val KEY_LONGITUDE = "pref_prayer_lng"
    private const val KEY_TIMEZONE_ID = "pref_prayer_tz"
    private const val KEY_IS_AUTO_GPS = "pref_prayer_is_auto_gps"
    private const val KEY_CALC_METHOD = "pref_prayer_calc_method"
    private const val KEY_JURISTIC_METHOD = "pref_prayer_juristic"
    private const val KEY_HIGH_LAT_RULE = "pref_prayer_high_lat"
    private const val KEY_HIJRI_OFFSET = "pref_prayer_hijri_offset"
    private const val KEY_PRE_ADHAN_MINUTES = "pref_prayer_pre_adhan"
    private const val KEY_ADHAN_TONE = "pref_prayer_adhan_tone"

    // Prayer times cache keys
    private const val KEY_CACHE_DATE = "pref_cached_timings_date"
    private const val KEY_CACHE_FAJR = "pref_cached_fajr"
    private const val KEY_CACHE_SUNRISE = "pref_cached_sunrise"
    private const val KEY_CACHE_DHUHR = "pref_cached_dhuhr"
    private const val KEY_CACHE_ASR = "pref_cached_asr"
    private const val KEY_CACHE_SUNSET = "pref_cached_sunset"
    private const val KEY_CACHE_MAGHRIB = "pref_cached_maghrib"
    private const val KEY_CACHE_ISHA = "pref_cached_isha"
    private const val KEY_CACHE_QIYAM = "pref_cached_qiyam"

    private var sharedPrefs: SharedPreferences? = null
    private val _settingsFlow = MutableStateFlow(getDefaultSettings())
    val settingsFlow: StateFlow<UserPrayerSettings> = _settingsFlow.asStateFlow()

    fun init(context: Context) {
        if (sharedPrefs == null) {
            sharedPrefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            _settingsFlow.value = loadSettings()
        }
    }

    private fun getPrefs(context: Context? = null): SharedPreferences {
        return sharedPrefs ?: run {
            val ctx = context ?: throw IllegalStateException("PrayerPreferences not initialized")
            ctx.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).also {
                sharedPrefs = it
            }
        }
    }

    fun getSettings(context: Context? = null): UserPrayerSettings {
        return sharedPrefs?.let { loadSettings() } ?: run {
            context?.let { init(it); loadSettings() } ?: getDefaultSettings()
        }
    }

    private fun loadSettings(): UserPrayerSettings {
        val prefs = sharedPrefs ?: return getDefaultSettings()
        val defaultCity = CityCatalog.getDefaultCity()

        val cityId = prefs.getString(KEY_CITY_ID, defaultCity.id) ?: defaultCity.id
        val cityName = prefs.getString(KEY_CITY_NAME, defaultCity.arabicName) ?: defaultCity.arabicName
        val countryName = prefs.getString(KEY_COUNTRY_NAME, defaultCity.countryArabic) ?: defaultCity.countryArabic
        val lat = prefs.getString(KEY_LATITUDE, defaultCity.latitude.toString())?.toDoubleOrNull() ?: defaultCity.latitude
        val lng = prefs.getString(KEY_LONGITUDE, defaultCity.longitude.toString())?.toDoubleOrNull() ?: defaultCity.longitude
        val tz = prefs.getString(KEY_TIMEZONE_ID, defaultCity.timezoneId) ?: defaultCity.timezoneId
        val autoGps = prefs.getBoolean(KEY_IS_AUTO_GPS, false)
        val methodId = prefs.getString(KEY_CALC_METHOD, defaultCity.defaultMethod.id) ?: defaultCity.defaultMethod.id
        val juristicName = prefs.getString(KEY_JURISTIC_METHOD, JuristicMethod.SHAFI_MALIKI_HANBALI.name) ?: JuristicMethod.SHAFI_MALIKI_HANBALI.name
        val highLatName = prefs.getString(KEY_HIGH_LAT_RULE, HighLatitudeRule.MID_NIGHT.name) ?: HighLatitudeRule.MID_NIGHT.name
        val hijriOffset = prefs.getInt(KEY_HIJRI_OFFSET, 0)
        val preAdhan = prefs.getInt(KEY_PRE_ADHAN_MINUTES, 15)
        val adhanTone = prefs.getString(KEY_ADHAN_TONE, "abdulbasit") ?: "abdulbasit"

        val method = CalculationMethod.fromId(methodId)
        val juristic = runCatching { JuristicMethod.valueOf(juristicName) }.getOrDefault(JuristicMethod.SHAFI_MALIKI_HANBALI)
        val highLat = runCatching { HighLatitudeRule.valueOf(highLatName) }.getOrDefault(HighLatitudeRule.MID_NIGHT)

        return UserPrayerSettings(
            cityId = cityId,
            cityName = cityName,
            countryName = countryName,
            latitude = lat,
            longitude = lng,
            timezoneId = tz,
            isAutoGps = autoGps,
            calculationMethod = method,
            juristicMethod = juristic,
            highLatitudeRule = highLat,
            hijriOffsetDays = hijriOffset,
            preAdhanMinutes = preAdhan,
            adhanToneId = adhanTone
        )
    }

    fun selectCity(context: Context, city: CityLocation) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putString(KEY_CITY_ID, city.id)
            .putString(KEY_CITY_NAME, city.arabicName)
            .putString(KEY_COUNTRY_NAME, city.countryArabic)
            .putString(KEY_LATITUDE, city.latitude.toString())
            .putString(KEY_LONGITUDE, city.longitude.toString())
            .putString(KEY_TIMEZONE_ID, city.timezoneId)
            .putBoolean(KEY_IS_AUTO_GPS, false)
            .putString(KEY_CALC_METHOD, city.defaultMethod.id)
            .remove(KEY_CACHE_DATE)
            .apply()

        AladhanApiClient.clearCache()
        _settingsFlow.value = loadSettings()
        syncPrayerTimesAsync(context)
        try {
            com.example.widget.WidgetUpdateHelper.updateAllWidgets(context)
            com.example.alarm.PrayerAlarmScheduler.scheduleNextPrayerAlarm(context)
        } catch (_: Exception) {}
    }

    fun setGpsLocation(context: Context, latitude: Double, longitude: Double, resolvedCityName: String? = null) {
        val prefs = getPrefs(context)
        val tzId = ZoneId.systemDefault().id
        val autoMethod = OnlineCitySearcher.resolveCalculationMethod(latitude, longitude, resolvedCityName)
        prefs.edit()
            .putString(KEY_CITY_ID, "GPS_CUSTOM")
            .putString(KEY_CITY_NAME, resolvedCityName ?: "موقعي الحالي")
            .putString(KEY_COUNTRY_NAME, "تحديد تلقائي (GPS)")
            .putString(KEY_LATITUDE, latitude.toString())
            .putString(KEY_LONGITUDE, longitude.toString())
            .putString(KEY_TIMEZONE_ID, tzId)
            .putBoolean(KEY_IS_AUTO_GPS, true)
            .putString(KEY_CALC_METHOD, autoMethod.id)
            .remove(KEY_CACHE_DATE)
            .apply()

        AladhanApiClient.clearCache()
        _settingsFlow.value = loadSettings()
        syncPrayerTimesAsync(context)
        try {
            com.example.widget.WidgetUpdateHelper.updateAllWidgets(context)
            com.example.alarm.PrayerAlarmScheduler.scheduleNextPrayerAlarm(context)
        } catch (_: Exception) {}
    }

    fun updateCalculationMethod(context: Context, method: CalculationMethod) {
        getPrefs(context).edit().putString(KEY_CALC_METHOD, method.id).apply()
        _settingsFlow.value = loadSettings()
        syncPrayerTimesAsync(context)
        try {
            com.example.alarm.PrayerAlarmScheduler.scheduleNextPrayerAlarm(context)
        } catch (_: Exception) {}
    }

    fun updateJuristicMethod(context: Context, method: JuristicMethod) {
        getPrefs(context).edit().putString(KEY_JURISTIC_METHOD, method.name).apply()
        _settingsFlow.value = loadSettings()
    }

    fun updateHijriOffset(context: Context, offsetDays: Int) {
        getPrefs(context).edit().putInt(KEY_HIJRI_OFFSET, offsetDays.coerceIn(-2, 2)).apply()
        _settingsFlow.value = loadSettings()
    }

    fun updateAdhanTone(context: Context, toneId: String) {
        getPrefs(context).edit().putString(KEY_ADHAN_TONE, toneId).apply()
        _settingsFlow.value = loadSettings()
    }

    fun updatePreAdhanMinutes(context: Context, minutes: Int) {
        getPrefs(context).edit().putInt(KEY_PRE_ADHAN_MINUTES, minutes).apply()
        _settingsFlow.value = loadSettings()
    }

    // ponytail: save verified AlAdhan API prayer times to local storage
    fun saveCachedPrayerTimes(context: Context, times: CalculatedPrayerTimes, date: java.time.LocalDate = java.time.LocalDate.now()) {
        getPrefs(context).edit()
            .putString(KEY_CACHE_DATE, date.toString())
            .putString(KEY_CACHE_FAJR, times.fajr.toString())
            .putString(KEY_CACHE_SUNRISE, times.sunrise.toString())
            .putString(KEY_CACHE_DHUHR, times.dhuhr.toString())
            .putString(KEY_CACHE_ASR, times.asr.toString())
            .putString(KEY_CACHE_SUNSET, times.sunset.toString())
            .putString(KEY_CACHE_MAGHRIB, times.maghrib.toString())
            .putString(KEY_CACHE_ISHA, times.isha.toString())
            .putString(KEY_CACHE_QIYAM, times.qiyamSuggested.toString())
            .apply()
    }

    // ponytail: retrieve cached prayer times synchronously without any network I/O
    fun getCachedPrayerTimes(context: Context, date: java.time.LocalDate = java.time.LocalDate.now()): CalculatedPrayerTimes? {
        val prefs = getPrefs(context)
        val cachedDate = prefs.getString(KEY_CACHE_DATE, null) ?: return null
        if (cachedDate != date.toString()) return null

        return try {
            CalculatedPrayerTimes(
                fajr = java.time.LocalTime.parse(prefs.getString(KEY_CACHE_FAJR, null) ?: return null),
                sunrise = java.time.LocalTime.parse(prefs.getString(KEY_CACHE_SUNRISE, null) ?: return null),
                dhuhr = java.time.LocalTime.parse(prefs.getString(KEY_CACHE_DHUHR, null) ?: return null),
                asr = java.time.LocalTime.parse(prefs.getString(KEY_CACHE_ASR, null) ?: return null),
                sunset = java.time.LocalTime.parse(prefs.getString(KEY_CACHE_SUNSET, null) ?: return null),
                maghrib = java.time.LocalTime.parse(prefs.getString(KEY_CACHE_MAGHRIB, null) ?: return null),
                isha = java.time.LocalTime.parse(prefs.getString(KEY_CACHE_ISHA, null) ?: return null),
                qiyamSuggested = java.time.LocalTime.parse(prefs.getString(KEY_CACHE_QIYAM, "03:00") ?: "03:00")
            )
        } catch (_: Exception) {
            null
        }
    }

    // ponytail: asynchronous background prayer times sync via Dispatchers.IO
    fun syncPrayerTimesAsync(context: Context) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val settings = getSettings(context)
                val fetched = AladhanApiClient.fetchPrayerTimesDirect(
                    latitude = settings.latitude,
                    longitude = settings.longitude,
                    method = settings.calculationMethod,
                    timezoneId = settings.timezoneId
                )
                if (fetched != null) {
                    saveCachedPrayerTimes(context, fetched)
                    try {
                        com.example.widget.WidgetUpdateHelper.updateAllWidgets(context)
                    } catch (_: Exception) {}
                    try {
                        com.example.alarm.PrayerAlarmScheduler.scheduleNextPrayerAlarm(context)
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }

    fun getTimezoneOffsetHours(timezoneId: String): Double {
        return try {
            val zone = if (timezoneId.isNotBlank()) ZoneId.of(timezoneId) else ZoneId.systemDefault()
            val zdt = ZonedDateTime.now(zone)
            zdt.offset.totalSeconds / 3600.0
        } catch (e: Exception) {
            try {
                ZonedDateTime.now(ZoneId.systemDefault()).offset.totalSeconds / 3600.0
            } catch (_: Exception) {
                1.0
            }
        }
    }

    private fun getDefaultSettings(): UserPrayerSettings {
        val makkah = CityCatalog.getDefaultCity()
        return UserPrayerSettings(
            cityId = makkah.id,
            cityName = makkah.arabicName,
            countryName = makkah.countryArabic,
            latitude = makkah.latitude,
            longitude = makkah.longitude,
            timezoneId = makkah.timezoneId,
            isAutoGps = false,
            calculationMethod = makkah.defaultMethod,
            juristicMethod = JuristicMethod.SHAFI_MALIKI_HANBALI,
            highLatitudeRule = HighLatitudeRule.MID_NIGHT,
            hijriOffsetDays = 0,
            preAdhanMinutes = 15,
            adhanToneId = "abdulbasit"
        )
    }
}
