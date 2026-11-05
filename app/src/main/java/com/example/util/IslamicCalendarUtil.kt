package com.example.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object IslamicCalendarUtil {

    data class PrayerSchedule(
        val key: String,
        val arabicName: String,
        val time: LocalTime,
        val timeFormatted: String,
        val isPassed: Boolean,
        val isNext: Boolean,
        val timeRemainingFormatted: String
    )

    /**
     * Keeps numbers in clean English digits (0, 1, 2, 3...) per design requirement
     */
    fun toArabicDigits(number: Any): String {
        return number.toString()
    }

    /**
     * Returns formatted Hijri date string, e.g. "14 ربيع الأول 1448 هـ"
     */
    fun getFormattedHijriDate(): String {
        return try {
            val offsetDays = runCatching { com.example.util.prayer.PrayerPreferences.getSettings().hijriOffsetDays.toLong() }.getOrDefault(0L)
            val hijrahDate = HijrahDate.now().plus(offsetDays, java.time.temporal.ChronoUnit.DAYS)
            val day = hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
            val month = hijrahDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
            val year = hijrahDate.get(java.time.temporal.ChronoField.YEAR)

            val monthNames = arrayOf(
                "المُحَرَّم", "صَفَر", "رَبِيع الأَوَّل", "رَبِيع الآخِر",
                "جُمَادَى الأُولَى", "جُمَادَى الآخِرَة", "رَجَب", "شَعْبَان",
                "رَمَضَان", "شَوَّال", "ذُو القَعْدَة", "ذُو الحِجَّة"
            )
            val monthName = monthNames.getOrElse(month - 1) { "هجري" }
            "$day $monthName $year هـ"
        } catch (e: Exception) {
            "14 ربيع الأول 1448 هـ"
        }
    }

    fun getFormattedArabicGregorianDate(): String {
        val now = LocalDate.now()
        val daysOfWeek = mapOf(
            "MONDAY" to "الإثنين",
            "TUESDAY" to "الثلاثاء",
            "WEDNESDAY" to "الأربعاء",
            "THURSDAY" to "الخميس",
            "FRIDAY" to "الجمعة",
            "SATURDAY" to "السبت",
            "SUNDAY" to "الأحد"
        )
        val months = arrayOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        )
        val dayName = daysOfWeek[now.dayOfWeek.name] ?: "اليوم"
        val monthName = months[now.monthValue - 1]
        return "$dayName، ${now.dayOfMonth} $monthName"
    }

    fun getDailyPrayerSchedule(): List<PrayerSchedule> {
        val now = LocalTime.now()

        val settings = runCatching { com.example.util.prayer.PrayerPreferences.getSettings() }.getOrNull()
        val scheduleList = if (settings != null) {
            // ponytail: use verified cached AlAdhan API timings, fallback to SolarCalculator
            val appCtx = runCatching { com.example.MuhasibApplication.instance }.getOrNull()
            val cachedApiTimes = appCtx?.let { com.example.util.prayer.PrayerPreferences.getCachedPrayerTimes(it) }
            if (cachedApiTimes == null && appCtx != null) {
                com.example.util.prayer.PrayerPreferences.syncPrayerTimesAsync(appCtx)
            }

            val calc = cachedApiTimes ?: run {
                val tzOffset = com.example.util.prayer.PrayerPreferences.getTimezoneOffsetHours(settings.timezoneId)
                com.example.util.prayer.SolarCalculator.calculate(
                    latitude = settings.latitude,
                    longitude = settings.longitude,
                    timezoneOffsetHours = tzOffset,
                    date = LocalDate.now(),
                    method = settings.calculationMethod,
                    juristicMethod = settings.juristicMethod,
                    highLatitudeRule = settings.highLatitudeRule
                )
            }
            listOf(
                Triple("Fajr", "الفجر", calc.fajr),
                Triple("Dhuhr", "الظهر", calc.dhuhr),
                Triple("Asr", "العصر", calc.asr),
                Triple("Maghrib", "المغرب", calc.maghrib),
                Triple("Isha", "العشاء", calc.isha)
            )
        } else {
            listOf(
                Triple("Fajr", "الفجر", LocalTime.of(5, 12)),
                Triple("Dhuhr", "الظهر", LocalTime.of(12, 35)),
                Triple("Asr", "العصر", LocalTime.of(15, 58)),
                Triple("Maghrib", "المغرب", LocalTime.of(18, 22)),
                Triple("Isha", "العشاء", LocalTime.of(19, 45))
            )
        }

        val nextIndex = scheduleList.indexOfFirst { it.third.isAfter(now) }
        val effectiveNextIndex = if (nextIndex != -1) nextIndex else 0

        return scheduleList.mapIndexed { index, (key, arabicName, time) ->
            val isPassed = now.isAfter(time)
            val isNext = index == effectiveNextIndex

            val minutesRemaining = if (time.isAfter(now)) {
                java.time.Duration.between(now, time).toMinutes()
            } else {
                val untilMidnight = java.time.Duration.between(now, LocalTime.MAX).toMinutes()
                val fromMidnight = java.time.Duration.between(LocalTime.MIN, time).toMinutes()
                untilMidnight + fromMidnight
            }

            val hours = minutesRemaining / 60
            val mins = minutesRemaining % 60
            val timeRemainingFormatted = if (hours > 0) {
                "$hours س و $mins د"
            } else {
                "$mins دقيقة"
            }

            val hour12 = if (time.hour % 12 == 0) 12 else time.hour % 12
            val amPm = if (time.hour < 12) "ص" else "م"
            val timeFormatted = "$hour12:${String.format(Locale.US, "%02d", time.minute)} $amPm"

            PrayerSchedule(
                key = key,
                arabicName = arabicName,
                time = time,
                timeFormatted = timeFormatted,
                isPassed = isPassed,
                isNext = isNext,
                timeRemainingFormatted = timeRemainingFormatted
            )
        }
    }

    fun getNextPrayer(): PrayerSchedule {
        val schedule = getDailyPrayerSchedule()
        return schedule.firstOrNull { it.isNext } ?: schedule.first()
    }

    // ponytail: time-of-day category determination for context-aware Adhkar
    fun getCurrentTimeDhikrCategory(): com.example.data.model.DhikrCategory {
        val now = LocalTime.now()
        val hour = now.hour
        val minute = now.minute
        val timeMinutes = hour * 60 + minute

        return when {
            // Fajr (~04:30) to Dhuhr (~12:30): Morning Adhkar
            timeMinutes in (4 * 60 + 30)..(12 * 60 + 30) -> com.example.data.model.DhikrCategory.MORNING
            // Dhuhr to Asr (~12:31 to 15:58): Post-Salah Adhkar
            timeMinutes in (12 * 60 + 31)..(15 * 60 + 58) -> com.example.data.model.DhikrCategory.POST_SALAH
            // Asr to Isha (~15:59 to 20:30): Evening Adhkar
            timeMinutes in (15 * 60 + 59)..(20 * 60 + 30) -> com.example.data.model.DhikrCategory.EVENING
            // Night: Sleep Adhkar
            else -> com.example.data.model.DhikrCategory.SLEEP
        }
    }

    // ponytail: Jumu'ah Friday check
    fun isJumuah(date: LocalDate = LocalDate.now()): Boolean {
        return date.dayOfWeek == java.time.DayOfWeek.FRIDAY
    }
}
