package com.example.util

import androidx.compose.ui.graphics.Color
import java.util.Calendar

/**
 * SacredPhonePhaseEngine — Heavenly iOS 26 Liquid Glass Edition.
 *
 * Shifts the entire phone's atmospheric sky gradient, warm champagne/alabaster glass tint,
 * allowed app tiers, and contextual life advisor across 6 Sacred Daily Phases.
 */
enum class SacredPhonePhase(
    val id: String,
    val arabicTitle: String,
    val englishTitle: String,
    val premiseSubtitle: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val skyTopHex: Long,
    val skyBottomHex: Long,
    val locksDopamineAppsByDefault: Boolean,
    val requiresNiyyahForAllNonEssential: Boolean,
    val recommendedWirdLabel: String
) {
    FAJR_DAWN(
        id = "FAJR_DAWN",
        arabicTitle = "فَجْرُ البَرَكَة وَالبُكُور",
        englishTitle = "Fajr Dawn Sanctuary",
        premiseSubtitle = "First light over stillness. Feeds remain sealed until Morning Adhkar and your Daily Niyyah are complete.",
        primaryColorHex = 0xFF9BBCD6,   // Ethereal Dawn Mist Blue
        secondaryColorHex = 0xFFE5C185, // Warm Horizon Gold
        skyTopHex = 0xFF101B2B,
        skyBottomHex = 0xFF1F293B,
        locksDopamineAppsByDefault = true,
        requiresNiyyahForAllNonEssential = true,
        recommendedWirdLabel = "Seal Morning Adhkar & 10 Ayahs"
    ),
    DUHA_DEEP_WORK(
        id = "DUHA_DEEP_WORK",
        arabicTitle = "سَعْيُ الضُّحَى وَالإِتْقَان",
        englishTitle = "Duha Alabaster Focus",
        premiseSubtitle = "Clarity and purposeful craft. Distraction apps require a mindful Niyyah time-box before opening.",
        primaryColorHex = 0xFFE5C185,   // Sunlit Champagne Gold
        secondaryColorHex = 0xFFF5F2EB, // Warm Pearl Alabaster
        skyTopHex = 0xFF1B1924,
        skyBottomHex = 0xFF28222B,
        locksDopamineAppsByDefault = false,
        requiresNiyyahForAllNonEssential = true,
        recommendedWirdLabel = "Execute Top 3 Daily Amal Missions"
    ),
    SALAH_CONVERGENCE(
        id = "SALAH_CONVERGENCE",
        arabicTitle = "نداءُ المِحْرَاب — حَيَّ عَلَى الصَّلَاة",
        englishTitle = "Mihrab Sanctuary Convergence",
        premiseSubtitle = "The phone stills itself for the call of Allah. Worldly apps pause gently until Salah is logged.",
        primaryColorHex = 0xFFF2D6A2,   // Celestial Sanctuary Gold
        secondaryColorHex = 0xFF9CC5B5, // Soft Sage Pearl
        skyTopHex = 0xFF141E24,
        skyBottomHex = 0xFF0B1116,
        locksDopamineAppsByDefault = true,
        requiresNiyyahForAllNonEssential = true,
        recommendedWirdLabel = "Perform Wudu, Pray On Time & Log Salah"
    ),
    ASR_SANCTUARY(
        id = "ASR_SANCTUARY",
        arabicTitle = "عَصْرُ السَّكِينَة وَالعَافِيَة",
        englishTitle = "Asr Golden Hour",
        premiseSubtitle = "Evening Adhkar, physical stewardship, and unhurried presence with family before sunset.",
        primaryColorHex = 0xFFDFB2A9,   // Golden Hour Silk Rose
        secondaryColorHex = 0xFFE5C185, // Champagne Gold
        skyTopHex = 0xFF1E1722,
        skyBottomHex = 0xFF2B2028,
        locksDopamineAppsByDefault = false,
        requiresNiyyahForAllNonEssential = true,
        recommendedWirdLabel = "Evening Adhkar + Physical Training + Saleh Reading"
    ),
    ISHA_MUHASABAH(
        id = "ISHA_MUHASABAH",
        arabicTitle = "خَلْوَةُ المُحَاسَبَة اليَوْمِيَّة",
        englishTitle = "Evening Muhasabah Reckoning",
        premiseSubtitle = "حاسبوا أنفسكم قبل أن تحاسبوا — Quietly weigh today's prayers, focus missions, and screen stewardship.",
        primaryColorHex = 0xFFC3A6E8,   // Twilight Lavender Pearl
        secondaryColorHex = 0xFFE5C185, // Starlight Gold
        skyTopHex = 0xFF121024,
        skyBottomHex = 0xFF1B1730,
        locksDopamineAppsByDefault = true,
        requiresNiyyahForAllNonEssential = true,
        recommendedWirdLabel = "Complete Evening Muhasabah Ledger & Book Wird"
    ),
    NIGHT_GUARD_LOCKDOWN(
        id = "NIGHT_GUARD_LOCKDOWN",
        arabicTitle = "حِصْنُ اللَّيْل وَالسُّبَات",
        englishTitle = "Velvet Night Sanctuary",
        premiseSubtitle = "6% OLED starlight stillness. High-dopamine triggers rest until Fajr Dawn.",
        primaryColorHex = 0xFFB8C4D4,   // Moonlit Silver Pearl
        secondaryColorHex = 0xFF8A99AD,
        skyTopHex = 0xFF070910,
        skyBottomHex = 0xFF0E121C,
        locksDopamineAppsByDefault = true,
        requiresNiyyahForAllNonEssential = true,
        recommendedWirdLabel = "Recite Surah Al-Mulk, Sleep Adhkar & Enter AOD"
    );

    val primaryColor: Color get() = Color(primaryColorHex)
    val secondaryColor: Color get() = Color(secondaryColorHex)
    val skyTopColor: Color get() = Color(skyTopHex)
    val skyBottomColor: Color get() = Color(skyBottomHex)
}

data class MuhasibLifeDirective(
    val headline: String,
    val arabicBadge: String,
    val detailText: String,
    val primaryCtaLabel: String,
    val ctaActionType: DirectiveActionType,
    val urgencyLevel: Int
)

enum class DirectiveActionType {
    LOG_PRAYER,
    OPEN_ADHKAR,
    START_FOCUS_MISSION,
    READ_SALEH_CHUNK,
    OPEN_MUHASABAH_LEDGER,
    ENTER_AOD_SLEEP,
    OPEN_PANIC_SHIELD
}

object SacredPhonePhaseEngine {

    fun resolveCurrentPhase(
        overridePhase: SacredPhonePhase? = null,
        minutesToNextPrayer: Int = 45,
        isCurrentPrayerLogged: Boolean = true,
        nightGuardEnabled: Boolean = true,
        calendar: Calendar = Calendar.getInstance()
    ): SacredPhonePhase {
        if (overridePhase != null) return overridePhase
        if (minutesToNextPrayer in 0..12 || (!isCurrentPrayerLogged && minutesToNextPrayer > 240)) {
            return SacredPhonePhase.SALAH_CONVERGENCE
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 4..6 -> SacredPhonePhase.FAJR_DAWN
            hour in 7..14 -> SacredPhonePhase.DUHA_DEEP_WORK
            hour in 15..18 -> SacredPhonePhase.ASR_SANCTUARY
            hour in 19..21 -> SacredPhonePhase.ISHA_MUHASABAH
            else -> if (nightGuardEnabled) SacredPhonePhase.NIGHT_GUARD_LOCKDOWN else SacredPhonePhase.ISHA_MUHASABAH
        }
    }

    fun generateLifeDirective(
        phase: SacredPhonePhase,
        nextPrayerName: String,
        minutesToNextPrayer: Int,
        completedPrayersCount: Int,
        pendingMissionsCount: Int,
        activeMissionTitle: String?,
        screenTimeMinutesToday: Int,
        dailyScreenBudgetMinutes: Int,
        activeBookTitle: String,
        currentBookChunk: Int,
        muhasabahCompletedToday: Boolean
    ): MuhasibLifeDirective {
        if (phase == SacredPhonePhase.SALAH_CONVERGENCE || minutesToNextPrayer <= 15) {
            return MuhasibLifeDirective(
                headline = "$nextPrayerName Sanctuary in ${minutesToNextPrayer.coerceAtLeast(0)}m",
                arabicBadge = "حَيَّ عَلَى الصَّلَاة",
                detailText = "Set down worldly tasks, perform Wudu, and enter stillness. Distraction apps rest until $nextPrayerName is logged.",
                primaryCtaLabel = "Complete $nextPrayerName & Restore Flow",
                ctaActionType = DirectiveActionType.LOG_PRAYER,
                urgencyLevel = 3
            )
        }

        if (screenTimeMinutesToday >= dailyScreenBudgetMinutes) {
            return MuhasibLifeDirective(
                headline = "Daily Screen Stewardship Reached (${screenTimeMinutesToday}m / ${dailyScreenBudgetMinutes}m)",
                arabicBadge = "تَنْبِيهُ العُمُر",
                detailText = "Preserve the serenity of your heart. Transition to Chunk #${currentBookChunk + 1} of $activeBookTitle.",
                primaryCtaLabel = "Open $activeBookTitle (Chunk #${currentBookChunk + 1})",
                ctaActionType = DirectiveActionType.READ_SALEH_CHUNK,
                urgencyLevel = 3
            )
        }

        return when (phase) {
            SacredPhonePhase.FAJR_DAWN -> MuhasibLifeDirective(
                headline = "Seal Your Morning Sanctuary",
                arabicBadge = "بَرَكَةُ البُكُور",
                detailText = "Begin in remembrance before the world speaks to you. Complete Morning Adhkar to unlock your day.",
                primaryCtaLabel = "Begin Morning Adhkar",
                ctaActionType = DirectiveActionType.OPEN_ADHKAR,
                urgencyLevel = 2
            )

            SacredPhonePhase.DUHA_DEEP_WORK -> {
                val missionPrompt = if (!activeMissionTitle.isNullOrBlank()) {
                    "Current Focus: \"$activeMissionTitle\" ($pendingMissionsCount remaining)."
                } else {
                    "$pendingMissionsCount purposeful missions queued for today's Barakah block."
                }
                MuhasibLifeDirective(
                    headline = "Deep Craft & Barakah Focus",
                    arabicBadge = "إِتْقَانُ العَمَل",
                    detailText = "$missionPrompt Screen stewardship: ${screenTimeMinutesToday}m of ${dailyScreenBudgetMinutes}m.",
                    primaryCtaLabel = "Begin 25m Focus Session",
                    ctaActionType = DirectiveActionType.START_FOCUS_MISSION,
                    urgencyLevel = 2
                )
            }

            SacredPhonePhase.ASR_SANCTUARY -> MuhasibLifeDirective(
                headline = "Golden Hour Reflection & Wird",
                arabicBadge = "زَادُ الرُّوح",
                detailText = "Read Chunk #${currentBookChunk + 1} of $activeBookTitle and step away from the screen before Maghrib.",
                primaryCtaLabel = "Continue $activeBookTitle",
                ctaActionType = DirectiveActionType.READ_SALEH_CHUNK,
                urgencyLevel = 1
            )

            SacredPhonePhase.ISHA_MUHASABAH -> {
                if (!muhasabahCompletedToday) {
                    MuhasibLifeDirective(
                        headline = "Evening Muhasabah Reflection",
                        arabicBadge = "حَاسِبُوا أَنْفُسَكُم",
                        detailText = "Salah logged: $completedPrayersCount/5 • Screen time: ${screenTimeMinutesToday}m. Seal your evening ledger in peace.",
                        primaryCtaLabel = "Open Evening Ledger",
                        ctaActionType = DirectiveActionType.OPEN_MUHASABAH_LEDGER,
                        urgencyLevel = 2
                    )
                } else {
                    MuhasibLifeDirective(
                        headline = "Evening Ledger Sealed in Peace",
                        arabicBadge = "تَمَّتِ المُحَاسَبَة",
                        detailText = "Your day is accounted for. Rest in 6% OLED Starlight mode for Fajr readiness.",
                        primaryCtaLabel = "Enter Starlight AOD",
                        ctaActionType = DirectiveActionType.ENTER_AOD_SLEEP,
                        urgencyLevel = 1
                    )
                }
            }

            SacredPhonePhase.NIGHT_GUARD_LOCKDOWN -> MuhasibLifeDirective(
                headline = "Velvet Night Sanctuary Active",
                arabicBadge = "حِصْنُ اللَّيْل",
                detailText = "Protect the stillness of your night and your Fajr awakening. Transition into 6% OLED Starlight mode.",
                primaryCtaLabel = "Rest in Starlight AOD",
                ctaActionType = DirectiveActionType.ENTER_AOD_SLEEP,
                urgencyLevel = 3
            )

            SacredPhonePhase.SALAH_CONVERGENCE -> MuhasibLifeDirective(
                headline = "Answer the Call of $nextPrayerName",
                arabicBadge = "الصَّلَاةُ خَيْرٌ",
                detailText = "Everything on this device waits for your covenant with Allah.",
                primaryCtaLabel = "Complete $nextPrayerName",
                ctaActionType = DirectiveActionType.LOG_PRAYER,
                urgencyLevel = 3
            )
        }
    }
}
