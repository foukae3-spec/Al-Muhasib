package com.example.util

// ponytail: helper for 30-second relapse reflection friction gate and sobriety milestone badges
object RelapseFrictionHelper {
    const val COUNTDOWN_SECONDS = 30

    fun canConfirmRelapse(secondsRemaining: Int): Boolean {
        return secondsRemaining <= 0
    }

    fun getAyahText(): String {
        return "﴿وَلَمَن صَبَرَ وَغَفَرَ إِنَّ ذَٰلِكَ لَمِنْ عَزْمِ الْأُمُورِ﴾"
    }

    fun getPromptText(cleanDays: Int): String {
        return "لحظة من فضلك... خذ نفساً عميقاً... هل ترغب حقاً في تصفير عداد صبرك بعد $cleanDays يوماً؟"
    }

    data class Milestone(
        val days: Int,
        val title: String,
        val emoji: String,
        val subtitle: String
    )

    val MILESTONES = listOf(
        Milestone(1, "بذرة التوبة", "🌱", "اليوم الأول"),
        Milestone(3, "عهد الصبر", "🌿", "3 أيام"),
        Milestone(7, "أسبوع الثبات", "🛡️", "7 أيام"),
        Milestone(14, "عزيمة الفارس", "⚔️", "14 يوماً"),
        Milestone(30, "نور الطهارة", "🌟", "30 يوماً"),
        Milestone(90, "الحرية واليقين", "👑", "90 يوماً")
    )

    fun isMilestoneUnlocked(cleanDays: Int, milestoneDays: Int): Boolean {
        return cleanDays >= milestoneDays
    }
}
