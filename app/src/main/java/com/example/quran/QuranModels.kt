package com.example.quran

import androidx.compose.runtime.Immutable

/**
 * Data model for a Quran Reciter (القارئ) with their high-definition CDN server URL.
 */
@Immutable
data class Reciter(
    val id: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val serverUrl: String,
    val rewayaArabic: String = "حفص عن عاصم",
    val description: String = ""
)

/**
 * Data model for a Quran Surah (السورة) out of the 114 Surahs.
 */
@Immutable
data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val ayahCount: Int,
    val isMeccan: Boolean
) {
    val formattedNumber: String
        get() = number.toString().padStart(3, '0')

    val revelationTypeArabic: String
        get() = if (isMeccan) "مكية" else "مدنية"
}

/**
 * Playback repeat modes.
 */
enum class RepeatMode(val labelArabic: String) {
    OFF("إيقاف التكرار"),
    ALL("تكرار الكل"),
    ONE("تكرار السورة الحالية")
}

/**
 * Sleep timer options.
 */
enum class SleepTimerDuration(val minutes: Int, val labelArabic: String) {
    MIN_15(15, "15 دقيقة"),
    MIN_30(30, "30 دقيقة"),
    MIN_45(45, "45 دقيقة"),
    MIN_60(60, "ساعة واحدة"),
    END_OF_SURAH(0, "نهاية السورة الحالية")
}

/**
 * Download status for a surah.
 */
sealed interface SurahDownloadState {
    data object NotDownloaded : SurahDownloadState
    data class Downloading(val progress: Float) : SurahDownloadState
    data object Downloaded : SurahDownloadState
    data class Failed(val reason: String) : SurahDownloadState
}

/**
 * Quran Audio Player Full State.
 */
@Immutable
data class QuranPlayerState(
    val currentReciter: Reciter = ReciterCatalog.DEFAULT_RECITER,
    val currentSurah: Surah? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val sleepTimerMinutesLeft: Int? = null,
    val sleepTimerSecondsRemaining: Long? = null,
    val isSleepTimerActive: Boolean = false,
    val sleepAtEndOfSurah: Boolean = false,
    val isLocalFile: Boolean = false,
    val errorMessage: String? = null
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
