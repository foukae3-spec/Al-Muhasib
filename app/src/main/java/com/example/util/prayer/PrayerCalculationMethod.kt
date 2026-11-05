package com.example.util.prayer

import java.time.LocalTime

/**
 * Calculation conventions recognized by major Islamic authorities worldwide.
 */
enum class CalculationMethod(
    val id: String,
    val arabicName: String,
    val englishName: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int? = null // If not null, Isha is fixed minutes after Maghrib
) {
    EGYPTIAN(
        id = "EGYPTIAN",
        arabicName = "الهيئة المصرية العامة للمساحة",
        englishName = "Egyptian General Authority of Survey",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    UMM_AL_QURA(
        id = "UMM_AL_QURA",
        arabicName = "جامعة أم القرى - مكة المكرمة",
        englishName = "Umm Al-Qura University, Makkah",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaIntervalMinutes = 90
    ),
    MUSLIM_WORLD_LEAGUE(
        id = "MWL",
        arabicName = "رابطة العالم الإسلامي",
        englishName = "Muslim World League (MWL)",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    ISNA(
        id = "ISNA",
        arabicName = "الجمعية الإسلامية لأمريكا الشمالية",
        englishName = "Islamic Society of North America (ISNA)",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    ),
    KARACHI(
        id = "KARACHI",
        arabicName = "جامعة العلوم الإسلامية بكراتشي",
        englishName = "University of Islamic Sciences, Karachi",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    GULF(
        id = "GULF",
        arabicName = "دائرة الشؤون الإسلامية - دبي والخليج",
        englishName = "Gulf & Dubai Islamic Affairs",
        fajrAngle = 18.2,
        ishaAngle = 0.0,
        ishaIntervalMinutes = 90
    ),
    DIYANET(
        id = "DIYANET",
        arabicName = "رئاسة الشؤون الدينية التركية (Diyanet)",
        englishName = "Presidency of Religious Affairs, Turkey",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    ALGERIA(
        id = "ALGERIA",
        arabicName = "وزارة الشؤون الدينية - الجزائر والمغرب العربي",
        englishName = "Ministry of Religious Affairs (North Africa)",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    );

    companion object {
        fun fromId(id: String): CalculationMethod {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: EGYPTIAN
        }
    }
}

/**
 * Juristic Method for determining Asr prayer shadow length.
 */
enum class JuristicMethod(val arabicName: String, val shadowFactor: Double) {
    SHAFI_MALIKI_HANBALI("الشافعي والمالكي والحنبلي (مِثْل واحد)", 1.0),
    HANAFI("الحنفي (مِثْلان)", 2.0)
}

/**
 * High Latitudes adjustment rule for locations above 48° North/South during summer.
 */
enum class HighLatitudeRule(val arabicName: String) {
    NONE("بدون تعديل"),
    MID_NIGHT("منتصف الليل (Middle of Night)"),
    ONE_SEVENTH("سُبُع الليل (One Seventh)"),
    ANGLE_BASED("النسبة الزاوية (Angle-Based)")
}

data class CalculatedPrayerTimes(
    val fajr: LocalTime,
    val sunrise: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val sunset: LocalTime,
    val maghrib: LocalTime,
    val isha: LocalTime,
    val qiyamSuggested: LocalTime
)
