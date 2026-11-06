package com.example.util

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import java.util.Calendar

/**
 * Classification tiers for every app installed on the user's phone.
 * Used by Al-Muhasib Home OS to replace mindless icon tapping with Niyyah (intentionality).
 */
enum class AppSanctuaryTier(
    val title: String,
    val arabicBadge: String,
    val requiresNiyyahGate: Boolean,
    val blockedDuringSalah: Boolean,
    val blockedDuringNightGuard: Boolean,
    val defaultDailyLimitMinutes: Int
) {
    SANCTUARY(
        title = "Sanctuary & Worship",
        arabicBadge = "المِحْرَاب",
        requiresNiyyahGate = false,
        blockedDuringSalah = false,
        blockedDuringNightGuard = false,
        defaultDailyLimitMinutes = 240
    ),
    ESSENTIAL_UTILITY(
        title = "Essential Life Tools",
        arabicBadge = "الضَّرُورِيَّات",
        requiresNiyyahGate = false,
        blockedDuringSalah = false,
        blockedDuringNightGuard = false,
        defaultDailyLimitMinutes = 120
    ),
    WORK_AND_PROVISION(
        title = "Work, Study & Comms",
        arabicBadge = "السَّعْيُ وَالعَمَل",
        requiresNiyyahGate = true,
        blockedDuringSalah = true,
        blockedDuringNightGuard = false,
        defaultDailyLimitMinutes = 90
    ),
    GUARDED_DOPAMINE(
        title = "Guarded / High-Distraction",
        arabicBadge = "مُقَيَّدٌ بِالنِّيَّة",
        requiresNiyyahGate = true,
        blockedDuringSalah = true,
        blockedDuringNightGuard = true,
        defaultDailyLimitMinutes = 20
    )
}

/**
 * Domain model representing an installed application inside the Intentional App Drawer.
 */
data class InstalledLauncherApp(
    val packageName: String,
    val label: String,
    val tier: AppSanctuaryTier,
    val usageMinutesToday: Int,
    val dailyLimitMinutes: Int,
    val isPinnedToDock: Boolean = false
) {
    val isOverDailyBudget: Boolean
        get() = usageMinutesToday >= dailyLimitMinutes
}

/**
 * Result of checking whether an app is permitted to launch right now.
 */
sealed class AppLaunchDecision {
    data class AllowedImmediately(val app: InstalledLauncherApp) : AppLaunchDecision()
    data class RequiresNiyyahGate(
        val app: InstalledLauncherApp,
        val reason: String,
        val suggestedTimeBoxMinutes: Int = 5
    ) : AppLaunchDecision()
    data class HardBlocked(
        val app: InstalledLauncherApp,
        val blockTitle: String,
        val arabicRule: String,
        val explanation: String
    ) : AppLaunchDecision()
}

object AppLauncherManager {

    private val DOPAMINE_PACKAGE_KEYWORDS = listOf(
        "youtube", "tiktok", "musically", "instagram", "facebook", "twitter",
        "reddit", "snapchat", "netflix", "twitch", "discord", "chrome", "browser",
        "opera", "brave", "firefox", "game", "pubg", "freefire", "roblox"
    )

    private val WORK_COMM_KEYWORDS = listOf(
        "whatsapp", "telegram", "gmail", "mail", "docs", "sheets", "slides",
        "drive", "slack", "notion", "obsidian", "github", "calendar", "zoom", "meet"
    )

    private val SANCTUARY_KEYWORDS = listOf(
        "quran", "muslim", "athan", "adhan", "prayer", "qibla", "hadith",
        "muhasib", "tarteel", "ayat", "hisn"
    )

    /**
     * Checks if PACKAGE_USAGE_STATS permission has been granted by the user.
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Opens the Android system screen where the user can grant Usage Access to Al-Muhasib OS.
     */
    fun openUsageAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    /**
     * Opens Android Default Home App picker so the user can set Al-Muhasib as their permanent Home screen.
     */
    fun openDefaultHomeSettings(context: Context) {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    /**
     * Queries today's foreground usage in minutes per package from UsageStatsManager.
     */
    fun queryTodayUsageMinutesByPackage(context: Context): Map<String, Int> {
        if (!hasUsageStatsPermission(context)) return emptyMap()
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyMap()

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val now = System.currentTimeMillis()

        val stats = usageStatsManager.queryAndAggregateUsageStats(startOfDay, now)
        val result = mutableMapOf<String, Int>()
        for ((pkg, usageStat) in stats) {
            val minutes = (usageStat.totalTimeInForeground / 60_000L).toInt()
            if (minutes > 0) {
                result[pkg] = minutes
            }
        }
        return result
    }

    /**
     * Returns total device screen time today in minutes.
     */
    fun getTotalScreenTimeMinutesToday(context: Context): Int {
        val map = queryTodayUsageMinutesByPackage(context)
        if (map.isEmpty()) return 38 // Fallback baseline when permission not yet granted
        return map.entries
            .filterNot { it.key == context.packageName || it.key.contains("systemui") }
            .sumOf { it.value }
    }

    /**
     * Loads all launchable apps on the device, classifies them into Sanctuary Tiers,
     * and attaches live daily usage minutes.
     */
    fun loadLaunchableApps(
        context: Context,
        customTierOverrides: Map<String, AppSanctuaryTier> = emptyMap(),
        customDailyLimits: Map<String, Int> = emptyMap()
    ): List<InstalledLauncherApp> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val usageMap = queryTodayUsageMinutesByPackage(context)
        val selfPkg = context.packageName

        val apps = resolveInfos
            .mapNotNull { info ->
                val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                if (pkg == selfPkg) return@mapNotNull null
                val label = info.loadLabel(pm)?.toString()?.trim().takeUnless { it.isNullOrEmpty() } ?: pkg
                val tier = customTierOverrides[pkg] ?: classifyPackage(pkg, label)
                val limit = customDailyLimits[pkg] ?: tier.defaultDailyLimitMinutes
                val usage = usageMap[pkg] ?: 0
                InstalledLauncherApp(
                    packageName = pkg,
                    label = label,
                    tier = tier,
                    usageMinutesToday = usage,
                    dailyLimitMinutes = limit,
                    isPinnedToDock = isDefaultDockCandidate(pkg, label)
                )
            }
            .distinctBy { it.packageName }
            .sortedWith(compareBy<InstalledLauncherApp> { it.tier.ordinal }.thenBy { it.label.lowercase() })

        return if (apps.isNotEmpty()) apps else getFallbackReferenceApps()
    }

    /**
     * Evaluates whether an app can be launched immediately, requires the Niyyah Intention Gate,
     * or is Hard-Blocked by Salah Convergence / Night Guard / Daily Budget.
     */
    fun evaluateLaunchAttempt(
        app: InstalledLauncherApp,
        currentPhase: SacredPhonePhase,
        salahLockActive: Boolean,
        nightGuardActive: Boolean,
        strictBudgetEnforcement: Boolean = true
    ): AppLaunchDecision {
        // 1. Salah Convergence Lock
        if ((currentPhase == SacredPhonePhase.SALAH_CONVERGENCE || salahLockActive) && app.tier.blockedDuringSalah) {
            return AppLaunchDecision.HardBlocked(
                app = app,
                blockTitle = "Paused for Salah Convergence",
                arabicRule = "الصَّلَاةُ أَوْلَى",
                explanation = "${app.label} is paused while the Mihrab Call is active. Pray and log your Salah first to restore access."
            )
        }

        // 2. Night Guard Lockdown (Post-Isha / Late Night)
        if ((currentPhase == SacredPhonePhase.NIGHT_GUARD_LOCKDOWN || nightGuardActive) && app.tier.blockedDuringNightGuard) {
            return AppLaunchDecision.HardBlocked(
                app = app,
                blockTitle = "Blocked by Night Guard Shield",
                arabicRule = "حِصْنُ اللَّيْل",
                explanation = "${app.label} is classified as a high-dopamine trigger and is locked after Isha to protect your purity streak and Fajr awakening."
            )
        }

        // 3. Fajr Dawn Protection
        if (currentPhase == SacredPhonePhase.FAJR_DAWN && app.tier == AppSanctuaryTier.GUARDED_DOPAMINE) {
            return AppLaunchDecision.HardBlocked(
                app = app,
                blockTitle = "Protected Fajr Barakah Window",
                arabicRule = "بَرَكَةُ البُكُور",
                explanation = "High-distraction feeds (${app.label}) remain sealed during the Fajr Dawn window until Morning Adhkar and your Daily Niyyah are complete."
            )
        }

        // 4. Daily Screen-Time Budget Exceeded on Guarded Apps
        if (strictBudgetEnforcement && app.tier == AppSanctuaryTier.GUARDED_DOPAMINE && app.isOverDailyBudget) {
            return AppLaunchDecision.HardBlocked(
                app = app,
                blockTitle = "Daily Time Budget Exhausted (${app.usageMinutesToday}m / ${app.dailyLimitMinutes}m)",
                arabicRule = "حِفْظُ الأَوْقَات",
                explanation = "You have used your full ${app.dailyLimitMinutes}-minute daily allocation for ${app.label}. Invest your remaining hours in Amal or Saleh reading."
            )
        }

        // 5. Niyyah Gate Required
        if (app.tier.requiresNiyyahGate || currentPhase.requiresNiyyahForAllNonEssential && app.tier != AppSanctuaryTier.SANCTUARY && app.tier != AppSanctuaryTier.ESSENTIAL_UTILITY) {
            val remaining = (app.dailyLimitMinutes - app.usageMinutesToday).coerceAtLeast(5)
            val suggested = remaining.coerceAtMost(10)
            return AppLaunchDecision.RequiresNiyyahGate(
                app = app,
                reason = "Declare your Niyyah (intention) and time-box before entering ${app.label}. Used today: ${app.usageMinutesToday}m / ${app.dailyLimitMinutes}m.",
                suggestedTimeBoxMinutes = suggested
            )
        }

        return AppLaunchDecision.AllowedImmediately(app)
    }

    /**
     * Launches the target package cleanly on the device.
     */
    fun executePackageLaunch(context: Context, packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(launchIntent)
            true
        }.getOrDefault(false)
    }

    private fun classifyPackage(packageName: String, label: String): AppSanctuaryTier {
        val combined = "${packageName.lowercase()} ${label.lowercase()}"
        return when {
            SANCTUARY_KEYWORDS.any { combined.contains(it) } -> AppSanctuaryTier.SANCTUARY
            DOPAMINE_PACKAGE_KEYWORDS.any { combined.contains(it) } -> AppSanctuaryTier.GUARDED_DOPAMINE
            WORK_COMM_KEYWORDS.any { combined.contains(it) } -> AppSanctuaryTier.WORK_AND_PROVISION
            else -> AppSanctuaryTier.ESSENTIAL_UTILITY
        }
    }

    private fun isDefaultDockCandidate(packageName: String, label: String): Boolean {
        val combined = "${packageName.lowercase()} ${label.lowercase()}"
        return combined.contains("dialer") ||
            combined.contains("phone") ||
            combined.contains("contacts") ||
            combined.contains("messaging") ||
            combined.contains("mms") ||
            combined.contains("camera") ||
            combined.contains("maps")
    }

    /**
     * Provides realistic default entries for preview/emulator environments.
     */
    fun getFallbackReferenceApps(): List<InstalledLauncherApp> = listOf(
        InstalledLauncherApp("com.android.dialer", "Phone & Calls", AppSanctuaryTier.ESSENTIAL_UTILITY, 6, 120, true),
        InstalledLauncherApp("com.google.android.apps.messaging", "Messages (SMS)", AppSanctuaryTier.ESSENTIAL_UTILITY, 4, 90, true),
        InstalledLauncherApp("com.google.android.apps.maps", "Maps & Navigation", AppSanctuaryTier.ESSENTIAL_UTILITY, 8, 120, true),
        InstalledLauncherApp("com.android.camera", "Camera", AppSanctuaryTier.ESSENTIAL_UTILITY, 2, 60, true),
        InstalledLauncherApp("com.whatsapp", "WhatsApp", AppSanctuaryTier.WORK_AND_PROVISION, 18, 45, false),
        InstalledLauncherApp("org.telegram.messenger", "Telegram", AppSanctuaryTier.WORK_AND_PROVISION, 12, 40, false),
        InstalledLauncherApp("com.google.android.gm", "Gmail", AppSanctuaryTier.WORK_AND_PROVISION, 9, 60, false),
        InstalledLauncherApp("md.obsidian", "Obsidian Notes", AppSanctuaryTier.WORK_AND_PROVISION, 15, 120, false),
        InstalledLauncherApp("com.google.android.youtube", "YouTube", AppSanctuaryTier.GUARDED_DOPAMINE, 16, 20, false),
        InstalledLauncherApp("com.android.chrome", "Chrome Browser", AppSanctuaryTier.GUARDED_DOPAMINE, 14, 25, false),
        InstalledLauncherApp("com.instagram.android", "Instagram", AppSanctuaryTier.GUARDED_DOPAMINE, 15, 15, false),
        InstalledLauncherApp("com.zhiliaoapp.musically", "TikTok", AppSanctuaryTier.GUARDED_DOPAMINE, 10, 10, false)
    )
}
