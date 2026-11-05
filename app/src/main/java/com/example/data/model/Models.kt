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
