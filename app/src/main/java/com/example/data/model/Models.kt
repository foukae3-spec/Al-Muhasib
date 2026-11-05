package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DhikrCategory(val displayName: String) {
    MORNING("أذكار الصباح"),
    EVENING("أذكار المساء"),
    WAKING("الاستيقاظ"),
    SLEEP("أذكار النوم"),
    HOME("المنزل"),
    MOSQUE("المسجد"),
    RESTROOM("الخلاء والوضوء"),
    POST_SALAH("دبر الصلوات"),
    PROTECTION("التحصين والرقية")
}

@Entity(tableName = "dhikr_items")
data class DhikrItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val benefit: String,
    val targetCount: Int = 1,
    val currentCount: Int = 0,
    val isCompleted: Boolean = false
)

@Entity(tableName = "prayer_logs")
data class PrayerLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dateString: String, // e.g. "2026-09-24"
    val fajrDone: Boolean = false,
    val dhuhrDone: Boolean = false,
    val asrDone: Boolean = false,
    val maghribDone: Boolean = false,
    val ishaDone: Boolean = false
) {
    fun completedCount(): Int {
        var count = 0
        if (fajrDone) count++
        if (dhuhrDone) count++
        if (asrDone) count++
        if (maghribDone) count++
        if (ishaDone) count++
        return count
    }

    fun completionRatio(): Float = completedCount() / 5f
}

@Entity(tableName = "sobriety_state")
data class SobrietyLog(
    @PrimaryKey
    val id: Int = 1,
    val purityStartEpochMs: Long = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000), // Default 14 days clean baseline
    val cleanDaysCount: Int = 14,
    val workoutCompletedToday: Boolean = false,
    val coldShowerToday: Boolean = false,
    val relapsesCount: Int = 0,
    val istighfarTodayCount: Int = 120,
    val istighfarDailyTarget: Int = 500,
    // ponytail: separate tasbih counters — completely decoupled so each dhikr has its own independent misbaha
    val tasbi7TodayCount: Int = 0,
    val tasbi7DailyTarget: Int = 200,
    val subhanAllahTodayCount: Int = 0,
    val alhamdulillahTodayCount: Int = 0,
    val allahuAkbarTodayCount: Int = 0,
    val laIlahaIllallahTodayCount: Int = 0
)


@Entity(tableName = "muhasabah_history")
data class MuhasabahScore(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dateString: String,
    val scorePercentage: Int,
    val salahScore: Int,
    val dhikrScore: Int,
    val purityScore: Int,
    val warningTriggered: Boolean = false
)

@Entity(tableName = "islamic_books")
data class IslamicBook(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String,
    val category: String, // "ADDICTION_RECOVERY", "SALAH_PRESERVATION", "TAZKIYAH_SOUL"
    val description: String,
    val totalChunks: Int,
    val currentChunkCursor: Int = 0,
    val isDownloaded: Boolean = false,
    val lastReadTimestamp: Long = 0L,
    val coverAccentHex: String = "#10B981"
)

@Entity(tableName = "book_chunks")
data class BookChunk(
    @PrimaryKey
    val id: String, // "${bookId}_chunk_${chunkIndex}"
    val bookId: String,
    val chunkIndex: Int,
    val pageNumber: Int,
    val title: String,
    val content: String,
    val reflectionQuestion: String? = null,
    val isRead: Boolean = false,
    val readTimestamp: Long? = null
)

@Entity(tableName = "recovery_tracks")
data class RecoveryTrack(
    @PrimaryKey
    val trackType: String, // "SALAH", "CANNABIS", "PORNOGRAPHY", "SMOKING"
    val title: String,
    val startDateTimestamp: Long = System.currentTimeMillis(),
    val cleanStreakSeconds: Long = 0L,
    val relapseCount: Int = 0,
    val moneySavedAmount: Double = 0.0, // In Algerian Dinars (DZD)
    val cigarettesAvoided: Int = 0,
    val highestStreakSeconds: Long = 0L,
    val nightGuardEnabled: Boolean = false,
    val salahLockEnabled: Boolean = false,
    val lastCheckInTimestamp: Long = System.currentTimeMillis()
)
