package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DailyLifeMission
import com.example.floating.FloatingDhikrService
import com.example.ui.components.GlassmorphicBadge
import com.example.ui.components.GlassmorphicButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GlassmorphicSurface
import com.example.util.AppLaunchDecision
import com.example.util.AppLauncherManager
import com.example.util.AppSanctuaryTier
import com.example.util.DirectiveActionType
import com.example.util.HapticFeedbackUtil
import com.example.util.InstalledLauncherApp
import com.example.util.MuhasibLifeDirective
import com.example.util.SacredPhonePhase
import com.example.util.SacredPhonePhaseEngine
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Display modes for the Sovereign Home Launcher screen:
 * - MINIMAL_PHONE: The clean, everyday Android Home Launcher with pinned phone apps, search, and clock.
 * - SANCTUARY_COCKPIT: The deep spiritual cockpit with 5-Salah orbital tracker, daily missions, and reform hub.
 */
enum class LauncherScreenMode {
    MINIMAL_PHONE,
    SANCTUARY_COCKPIT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MihrabHomeLauncherScreen(
    nextPrayerName: String = "Asr",
    nextPrayerTimeFormatted: String = "15:42",
    minutesToNextPrayer: Int = 24,
    hijriDateString: String = "26 Jumada al-Ula 1448 AH",
    cleanDaysStreak: Int = 14,
    istighfarTodayCount: Int = 140,
    activeBookTitle: String = "Al-Daa wa Al-Dawaa (Ibn Qayyim)",
    currentBookChunk: Int = 4,
    persistedPrayers: List<Pair<String, Boolean>>? = null,
    persistedMissions: List<DailyLifeMission> = emptyList(),
    onToggleMissionPersisted: ((Int, Boolean) -> Unit)? = null,
    onAddMissionPersisted: ((String, String, Int) -> Unit)? = null,
    onRecordNiyyahSessionPersisted: ((String, String, String, Int) -> Unit)? = null,
    expiredNiyyahAppAlert: String? = null,
    onDismissExpiredAlert: () -> Unit = {},
    onOpenQuran: () -> Unit = {},
    onOpenAdhkar: () -> Unit = {},
    onOpenSalehBooks: () -> Unit = {},
    onOpenRecovery: () -> Unit = {},
    onOpenDashboard: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenPanicShield: () -> Unit = {},
    onLaunchAodSleepMode: () -> Unit = {},
    onPrayerCompletedToggle: (String, Boolean) -> Unit = { _, _ -> },
    onIncrementIstighfar: () -> Unit = {}
) {
    val context = LocalContext.current

    var launcherMode by remember { mutableStateOf(LauncherScreenMode.MINIMAL_PHONE) }
    var isDefaultHome by remember { mutableStateOf(AppLauncherManager.isDefaultHomeLauncher(context)) }

    var selectedPhaseOverride by remember { mutableStateOf<SacredPhonePhase?>(null) }
    var salahLockEnabled by remember { mutableStateOf(true) }
    var nightGuardEnabled by remember { mutableStateOf(true) }
    var dailyScreenBudgetMinutes by remember { mutableIntStateOf(60) }
    var isDynamicIslandExpanded by remember { mutableStateOf(false) }

    val prayerStates = remember {
        mutableStateListOf(
            "Fajr" to (persistedPrayers?.find { it.first.equals("Fajr", ignoreCase = true) }?.second ?: true),
            "Dhuhr" to (persistedPrayers?.find { it.first.equals("Dhuhr", ignoreCase = true) }?.second ?: true),
            "Asr" to (persistedPrayers?.find { it.first.equals("Asr", ignoreCase = true) }?.second ?: false),
            "Maghrib" to (persistedPrayers?.find { it.first.equals("Maghrib", ignoreCase = true) }?.second ?: false),
            "Isha" to (persistedPrayers?.find { it.first.equals("Isha", ignoreCase = true) }?.second ?: false)
        )
    }

    LaunchedEffect(persistedPrayers) {
        if (persistedPrayers != null) {
            for (pair in persistedPrayers) {
                val idx = prayerStates.indexOfFirst { it.first.equals(pair.first, ignoreCase = true) }
                if (idx >= 0 && prayerStates[idx].second != pair.second) {
                    prayerStates[idx] = pair
                }
            }
        }
    }

    val completedPrayersCount = prayerStates.count { it.second }
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    val dailyMissions = remember { mutableStateListOf<DailyLifeMission>() }
    LaunchedEffect(persistedMissions) {
        if (persistedMissions.isNotEmpty()) {
            dailyMissions.clear()
            dailyMissions.addAll(persistedMissions)
        } else if (dailyMissions.isEmpty()) {
            dailyMissions.addAll(
                listOf(
                    DailyLifeMission(
                        id = 1,
                        dateString = todayDate,
                        title = "Read Chunk #$currentBookChunk of $activeBookTitle",
                        prayerAnchor = "POST_FAJR",
                        category = "DEVOTION",
                        estimatedMinutes = 15,
                        isCompleted = true
                    ),
                    DailyLifeMission(
                        id = 2,
                        dateString = todayDate,
                        title = "90m Deep Craft Block (Zero Feeds / Zero Browser)",
                        prayerAnchor = "DUHA_BLOCK",
                        category = "PROVISION",
                        estimatedMinutes = 90,
                        isCompleted = false
                    ),
                    DailyLifeMission(
                        id = 3,
                        dateString = todayDate,
                        title = "Physical Stewardship & Family Presence Before Maghrib",
                        prayerAnchor = "ASR_BLOCK",
                        category = "HEALTH",
                        estimatedMinutes = 45,
                        isCompleted = false
                    )
                )
            )
        }
    }

    var focusSecondsRemaining by remember { mutableIntStateOf(0) }
    var isFocusTimerRunning by remember { mutableStateOf(false) }
    var activeFocusMissionTitle by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isFocusTimerRunning, focusSecondsRemaining) {
        if (isFocusTimerRunning && focusSecondsRemaining > 0) {
            delay(1000L)
            focusSecondsRemaining--
            if (focusSecondsRemaining == 0) {
                isFocusTimerRunning = false
                HapticFeedbackUtil.performHeavyClick(context)
            }
        }
    }

    var currentTimeText by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            isDefaultHome = AppLauncherManager.isDefaultHomeLauncher(context)
            delay(15_000L)
        }
    }

    val installedApps = remember { AppLauncherManager.loadLaunchableApps(context) }
    val totalScreenMinutesToday = remember(installedApps) {
        AppLauncherManager.getTotalScreenTimeMinutesToday(context)
    }

    var showAppDrawer by remember { mutableStateOf(false) }
    var showMuhasabahLedger by remember { mutableStateOf(false) }
    var showAddMissionDialog by remember { mutableStateOf(false) }
    var muhasabahSealedToday by remember { mutableStateOf(false) }

    var pendingNiyyahGateDecision by remember {
        mutableStateOf<AppLaunchDecision.RequiresNiyyahGate?>(null)
    }
    var pendingHardBlockDecision by remember {
        mutableStateOf<AppLaunchDecision.HardBlocked?>(null)
    }

    val currentPhase = SacredPhonePhaseEngine.resolveCurrentPhase(
        overridePhase = selectedPhaseOverride,
        minutesToNextPrayer = minutesToNextPrayer,
        isCurrentPrayerLogged = completedPrayersCount >= 2,
        nightGuardEnabled = nightGuardEnabled
    )

    val phasePrimaryColor by animateColorAsState(
        targetValue = currentPhase.primaryColor,
        animationSpec = tween(700),
        label = "heavenly_primary"
    )
    val skyTopColor by animateColorAsState(
        targetValue = currentPhase.skyTopColor,
        animationSpec = tween(900),
        label = "sky_top"
    )
    val skyBottomColor by animateColorAsState(
        targetValue = currentPhase.skyBottomColor,
        animationSpec = tween(900),
        label = "sky_bottom"
    )

    val directive: MuhasibLifeDirective = SacredPhonePhaseEngine.generateLifeDirective(
        phase = currentPhase,
        nextPrayerName = nextPrayerName,
        minutesToNextPrayer = minutesToNextPrayer,
        completedPrayersCount = completedPrayersCount,
        pendingMissionsCount = dailyMissions.count { !it.isCompleted },
        activeMissionTitle = dailyMissions.firstOrNull { !it.isCompleted }?.title,
        screenTimeMinutesToday = totalScreenMinutesToday,
        dailyScreenBudgetMinutes = dailyScreenBudgetMinutes,
        activeBookTitle = activeBookTitle,
        currentBookChunk = currentBookChunk,
        muhasabahCompletedToday = muhasabahSealedToday
    )

    val handleAppLaunchRequest: (InstalledLauncherApp) -> Unit = { app ->
        HapticFeedbackUtil.performClick(context)
        val decision = AppLauncherManager.evaluateLaunchAttempt(
            app = app,
            currentPhase = currentPhase,
            salahLockActive = currentPhase == SacredPhonePhase.SALAH_CONVERGENCE && salahLockEnabled,
            nightGuardActive = currentPhase == SacredPhonePhase.NIGHT_GUARD_LOCKDOWN && nightGuardEnabled
        )
        when (decision) {
            is AppLaunchDecision.AllowedImmediately -> {
                showAppDrawer = false
                AppLauncherManager.executePackageLaunch(context, app.packageName)
            }
            is AppLaunchDecision.RequiresNiyyahGate -> {
                pendingNiyyahGateDecision = decision
            }
            is AppLaunchDecision.HardBlocked -> {
                pendingHardBlockDecision = decision
            }
        }
    }

    // Heavenly Multi-Stop Atmospheric Horizon Background with statusBarsPadding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        skyTopColor,
                        skyBottomColor,
                        Color(0xFF0A0D14)
                    )
                )
            )
    ) {
        // Celestial Horizon Ambient Glow Orb
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            phasePrimaryColor.copy(alpha = 0.22f),
                            phasePrimaryColor.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(540f, 260f),
                        radius = 780f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Section: Optional Set-Default-Launcher Banner & Dynamic Island
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // If app is not default launcher, show elegant invitation banner
                if (!isDefaultHome) {
                    SetDefaultLauncherBanner(
                        accentColor = phasePrimaryColor,
                        onClick = {
                            HapticFeedbackUtil.performClick(context)
                            AppLauncherManager.openDefaultHomeSettings(context)
                        }
                    )
                }

                // Interactive Dynamic Island Live Activity Capsule
                HeavenlyDynamicIslandCapsule(
                    isExpanded = isDynamicIslandExpanded,
                    nextPrayerName = nextPrayerName,
                    minutesToNextPrayer = minutesToNextPrayer,
                    isFocusTimerRunning = isFocusTimerRunning,
                    focusSecondsRemaining = focusSecondsRemaining,
                    istighfarCount = istighfarTodayCount,
                    accentColor = phasePrimaryColor,
                    onToggleExpand = {
                        HapticFeedbackUtil.performTick(context)
                        isDynamicIslandExpanded = !isDynamicIslandExpanded
                    },
                    onIncrementIstighfar = {
                        HapticFeedbackUtil.performTick(context)
                        onIncrementIstighfar()
                    }
                )

                if (!expiredNiyyahAppAlert.isNullOrBlank()) {
                    ExpiredNiyyahLiquidBanner(
                        appName = expiredNiyyahAppAlert,
                        accentColor = phasePrimaryColor,
                        onDismiss = onDismissExpiredAlert
                    )
                }

                // Launcher Mode Switcher Pill (Phone Home vs Sanctuary Cockpit)
                LauncherModeSegmentedToggle(
                    currentMode = launcherMode,
                    accentColor = phasePrimaryColor,
                    onSelectMode = { mode ->
                        HapticFeedbackUtil.performTick(context)
                        launcherMode = mode
                    }
                )
            }

            // Main Content Area depending on selected Launcher Mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (launcherMode == LauncherScreenMode.MINIMAL_PHONE) {
                    // 1. MINIMALIST PHONE LAUNCHER VIEW
                    MinimalistPhoneLauncherView(
                        currentTimeText = currentTimeText,
                        hijriDateString = hijriDateString,
                        nextPrayerName = nextPrayerName,
                        minutesToNextPrayer = minutesToNextPrayer,
                        activeMissionTitle = dailyMissions.firstOrNull { !it.isCompleted }?.title,
                        installedApps = installedApps,
                        accentColor = phasePrimaryColor,
                        onOpenSearchDrawer = {
                            HapticFeedbackUtil.performClick(context)
                            showAppDrawer = true
                        },
                        onLaunchApp = handleAppLaunchRequest,
                        onOpenQuran = onOpenQuran,
                        onOpenAdhkar = onOpenAdhkar,
                        onOpenSalehBooks = onOpenSalehBooks,
                        onOpenSettings = onOpenSettings,
                        onOpenMuhasabah = {
                            HapticFeedbackUtil.performClick(context)
                            showMuhasabahLedger = true
                        }
                    )
                } else {
                    // 2. SANCTUARY COCKPIT VIEW
                    SanctuaryCockpitView(
                        currentTimeText = currentTimeText,
                        hijriDateString = hijriDateString,
                        cleanDaysStreak = cleanDaysStreak,
                        phase = currentPhase,
                        phaseColor = phasePrimaryColor,
                        selectedPhaseOverride = selectedPhaseOverride,
                        onSelectAutoPhase = {
                            HapticFeedbackUtil.performTick(context)
                            selectedPhaseOverride = null
                        },
                        onSelectPhase = { phase ->
                            HapticFeedbackUtil.performClick(context)
                            selectedPhaseOverride = phase
                        },
                        nextPrayerName = nextPrayerName,
                        nextPrayerTimeFormatted = nextPrayerTimeFormatted,
                        minutesToNextPrayer = minutesToNextPrayer,
                        prayerStates = prayerStates,
                        onTogglePrayer = { index ->
                            HapticFeedbackUtil.performClick(context)
                            val current = prayerStates[index]
                            val updated = current.first to !current.second
                            prayerStates[index] = updated
                            onPrayerCompletedToggle(updated.first, updated.second)
                        },
                        directive = directive,
                        onExecuteDirective = {
                            HapticFeedbackUtil.performHeavyClick(context)
                            when (directive.ctaActionType) {
                                DirectiveActionType.LOG_PRAYER -> {
                                    val idx = prayerStates.indexOfFirst { !it.second }
                                    if (idx >= 0) {
                                        prayerStates[idx] = prayerStates[idx].first to true
                                    }
                                }
                                DirectiveActionType.OPEN_ADHKAR -> onOpenAdhkar()
                                DirectiveActionType.START_FOCUS_MISSION -> {
                                    val nextMission = dailyMissions.firstOrNull { !it.isCompleted }
                                    activeFocusMissionTitle = nextMission?.title ?: "Deep Craft Session"
                                    focusSecondsRemaining = 25 * 60
                                    isFocusTimerRunning = true
                                }
                                DirectiveActionType.READ_SALEH_CHUNK -> onOpenSalehBooks()
                                DirectiveActionType.OPEN_MUHASABAH_LEDGER -> showMuhasabahLedger = true
                                DirectiveActionType.ENTER_AOD_SLEEP -> onLaunchAodSleepMode()
                                DirectiveActionType.OPEN_PANIC_SHIELD -> onOpenPanicShield()
                            }
                        },
                        dailyMissions = dailyMissions,
                        isFocusTimerRunning = isFocusTimerRunning,
                        focusSecondsRemaining = focusSecondsRemaining,
                        activeFocusMissionTitle = activeFocusMissionTitle,
                        onToggleMission = { missionId ->
                            HapticFeedbackUtil.performClick(context)
                            val idx = dailyMissions.indexOfFirst { it.id == missionId }
                            if (idx >= 0) {
                                val item = dailyMissions[idx]
                                val updated = !item.isCompleted
                                dailyMissions[idx] = item.copy(isCompleted = updated)
                                onToggleMissionPersisted?.invoke(missionId, updated)
                            }
                        },
                        onStartFocusForMission = { mission ->
                            HapticFeedbackUtil.performHeavyClick(context)
                            activeFocusMissionTitle = mission.title
                            focusSecondsRemaining = mission.estimatedMinutes * 60
                            isFocusTimerRunning = true
                        },
                        onStopFocusTimer = {
                            HapticFeedbackUtil.performClick(context)
                            isFocusTimerRunning = false
                        },
                        onAddMissionClick = {
                            HapticFeedbackUtil.performClick(context)
                            showAddMissionDialog = true
                        },
                        screenMinutesUsed = totalScreenMinutesToday,
                        screenBudgetMinutes = dailyScreenBudgetMinutes,
                        istighfarTodayCount = istighfarTodayCount,
                        salahLockEnabled = salahLockEnabled,
                        nightGuardEnabled = nightGuardEnabled,
                        onIncrementIstighfar = {
                            HapticFeedbackUtil.performTick(context)
                            onIncrementIstighfar()
                        },
                        onToggleSalahLock = { salahLockEnabled = it },
                        onToggleNightGuard = { nightGuardEnabled = it },
                        onGrantUsageAccess = { AppLauncherManager.openUsageAccessSettings(context) },
                        onOpenQuran = onOpenQuran,
                        onOpenSalehBooks = onOpenSalehBooks,
                        onOpenPanicShield = onOpenPanicShield,
                        onLaunchAodSleepMode = onLaunchAodSleepMode,
                        onOpenAppDrawer = {
                            HapticFeedbackUtil.performClick(context)
                            showAppDrawer = true
                        },
                        onOpenMuhasabahLedger = {
                            HapticFeedbackUtil.performClick(context)
                            showMuhasabahLedger = true
                        }
                    )
                }
            }

            // Pinned Core Phone Launcher Dock (Bottom of Home Screen)
            PinnedPhoneBottomDock(
                accentColor = phasePrimaryColor,
                onOpenDialer = {
                    HapticFeedbackUtil.performClick(context)
                    AppLauncherManager.launchDialer(context)
                },
                onOpenMessaging = {
                    HapticFeedbackUtil.performClick(context)
                    AppLauncherManager.launchMessaging(context)
                },
                onToggleSanctuary = {
                    HapticFeedbackUtil.performClick(context)
                    launcherMode = if (launcherMode == LauncherScreenMode.MINIMAL_PHONE) {
                        LauncherScreenMode.SANCTUARY_COCKPIT
                    } else {
                        LauncherScreenMode.MINIMAL_PHONE
                    }
                },
                onOpenCamera = {
                    HapticFeedbackUtil.performClick(context)
                    AppLauncherManager.launchCamera(context)
                },
                onOpenAppDrawer = {
                    HapticFeedbackUtil.performClick(context)
                    showAppDrawer = true
                }
            )
        }

        // Full Searchable Intentional App Drawer Sheet
        if (showAppDrawer) {
            ModalBottomSheet(
                onDismissRequest = { showAppDrawer = false },
                containerColor = Color(0xF2101520),
                contentColor = Color(0xFFF5F2EB),
                shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)
            ) {
                IntentionalAppDrawerSheet(
                    apps = installedApps,
                    currentPhase = currentPhase,
                    accentColor = phasePrimaryColor,
                    onSelectApp = handleAppLaunchRequest,
                    onClose = { showAppDrawer = false }
                )
            }
        }

        // Evening Muhasabah Reckoning Sheet
        if (showMuhasabahLedger) {
            ModalBottomSheet(
                onDismissRequest = { showMuhasabahLedger = false },
                containerColor = Color(0xF2121020),
                contentColor = Color(0xFFF5F2EB),
                shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)
            ) {
                EveningMuhasabahLedgerSheet(
                    completedPrayersCount = completedPrayersCount,
                    completedMissionsCount = dailyMissions.count { it.isCompleted },
                    totalMissionsCount = dailyMissions.size,
                    screenMinutesUsed = totalScreenMinutesToday,
                    screenBudgetMinutes = dailyScreenBudgetMinutes,
                    istighfarCount = istighfarTodayCount,
                    cleanDaysStreak = cleanDaysStreak,
                    onSealDailyLedger = {
                        HapticFeedbackUtil.performHeavyClick(context)
                        muhasabahSealedToday = true
                        showMuhasabahLedger = false
                    },
                    onEnterAodNow = {
                        showMuhasabahLedger = false
                        onLaunchAodSleepMode()
                    }
                )
            }
        }

        if (showAddMissionDialog) {
            AddAmalMissionDialog(
                accentColor = phasePrimaryColor,
                onDismiss = { showAddMissionDialog = false },
                onAddMission = { title, anchor, minutes ->
                    val newMission = DailyLifeMission(
                        id = (dailyMissions.maxOfOrNull { it.id } ?: 0) + 1,
                        dateString = todayDate,
                        title = title,
                        prayerAnchor = anchor,
                        category = "PROVISION",
                        estimatedMinutes = minutes,
                        isCompleted = false
                    )
                    dailyMissions.add(newMission)
                    onAddMissionPersisted?.invoke(title, anchor, minutes)
                    showAddMissionDialog = false
                }
            )
        }

        pendingNiyyahGateDecision?.let { gateDecision ->
            NiyyahGateInterceptorDialog(
                decision = gateDecision,
                accentColor = phasePrimaryColor,
                onCancel = { pendingNiyyahGateDecision = null },
                onRedirectToQuranOrBooks = {
                    pendingNiyyahGateDecision = null
                    showAppDrawer = false
                    onOpenSalehBooks()
                },
                onConfirmNiyyahAndLaunch = { statedNiyyah, allottedMinutes ->
                    pendingNiyyahGateDecision = null
                    showAppDrawer = false
                    onRecordNiyyahSessionPersisted?.invoke(
                        gateDecision.app.packageName,
                        gateDecision.app.label,
                        statedNiyyah,
                        allottedMinutes
                    )
                    val hudIntent = Intent(context, FloatingDhikrService::class.java).apply {
                        action = FloatingDhikrService.ACTION_START_NIYYAH_TIMER
                        putExtra(FloatingDhikrService.EXTRA_APP_LABEL, gateDecision.app.label)
                        putExtra(FloatingDhikrService.EXTRA_NIYYAH_TEXT, statedNiyyah)
                        putExtra(FloatingDhikrService.EXTRA_DURATION_MINUTES, allottedMinutes)
                    }
                    runCatching {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(hudIntent)
                        } else {
                            context.startService(hudIntent)
                        }
                    }
                    AppLauncherManager.executePackageLaunch(context, gateDecision.app.packageName)
                }
            )
        }

        pendingHardBlockDecision?.let { blockDecision ->
            HardBlockedAppModal(
                decision = blockDecision,
                accentColor = phasePrimaryColor,
                onDismiss = { pendingHardBlockDecision = null },
                onOpenSalehBooksInstead = {
                    pendingHardBlockDecision = null
                    showAppDrawer = false
                    onOpenSalehBooks()
                },
                onOpenPanicShieldInstead = {
                    pendingHardBlockDecision = null
                    showAppDrawer = false
                    onOpenPanicShield()
                }
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 1. MINIMALIST PHONE LAUNCHER VIEW
// -------------------------------------------------------------------------------------------------
@Composable
private fun MinimalistPhoneLauncherView(
    currentTimeText: String,
    hijriDateString: String,
    nextPrayerName: String,
    minutesToNextPrayer: Int,
    activeMissionTitle: String?,
    installedApps: List<InstalledLauncherApp>,
    accentColor: Color,
    onOpenSearchDrawer: () -> Unit,
    onLaunchApp: (InstalledLauncherApp) -> Unit,
    onOpenQuran: () -> Unit,
    onOpenAdhkar: () -> Unit,
    onOpenSalehBooks: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMuhasabah: () -> Unit
) {
    val context = LocalContext.current

    // Quick favorites from installed apps
    val favoriteApps = remember(installedApps) {
        val candidates = listOf("whatsapp", "telegram", "camera", "chrome", "obsidian", "gmail", "maps", "youtube")
        val matched = candidates.mapNotNull { key ->
            installedApps.firstOrNull { it.packageName.lowercase().contains(key) || it.label.lowercase().contains(key) }
        }.distinctBy { it.packageName }
        if (matched.isNotEmpty()) matched.take(8) else installedApps.take(8)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // Large Clock & Date Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = hijriDateString.uppercase(),
                    color = Color(0xFFF5F2EB).copy(alpha = 0.72f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.8.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentTimeText,
                    color = Color(0xFFFBF9F5),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = (-2).sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Next Prayer & Focus Target Pill
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassmorphicBadge(
                        text = "🕌 $nextPrayerName • ${minutesToNextPrayer}m left",
                        color = accentColor,
                        modifier = Modifier.clickable { onOpenMuhasabah() }
                    )
                    activeMissionTitle?.let { title ->
                        GlassmorphicBadge(
                            text = "🎯 ${title.take(22)}...",
                            color = Color(0xFFF5F2EB),
                            modifier = Modifier.clickable { onOpenSalehBooks() }
                        )
                    }
                }
            }
        }

        // Quick Search Bar / App Launcher trigger
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSearchDrawer() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = accentColor, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Search phone applications...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = "${installedApps.size} apps",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Spiritual Shortcuts Bento
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickAppLauncherTile(
                    modifier = Modifier.weight(1f),
                    title = "المصحف",
                    subtitle = "Warsh & Hafs",
                    icon = Icons.Default.MenuBook,
                    accentColor = Color(0xFF10B981),
                    onClick = onOpenQuran
                )
                QuickAppLauncherTile(
                    modifier = Modifier.weight(1f),
                    title = "الأذكار",
                    subtitle = "Hisn al-Muslim",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = Color(0xFF38BDF8),
                    onClick = onOpenAdhkar
                )
                QuickAppLauncherTile(
                    modifier = Modifier.weight(1f),
                    title = "الكتب",
                    subtitle = "Saleh Reform",
                    icon = Icons.Default.Book,
                    accentColor = Color(0xFFE5C185),
                    onClick = onOpenSalehBooks
                )
            }
        }

        // Pinned Favorite Apps Grid (with Real Native App Icons)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FAVORITE APPLICATIONS",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "View All →",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { onOpenSearchDrawer() }
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false
                ) {
                    items(favoriteApps, key = { it.packageName }) { app ->
                        AppIconLauncherCell(
                            app = app,
                            accentColor = accentColor,
                            onClick = { onLaunchApp(app) }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 2. SANCTUARY COCKPIT VIEW
// -------------------------------------------------------------------------------------------------
@Composable
private fun SanctuaryCockpitView(
    currentTimeText: String,
    hijriDateString: String,
    cleanDaysStreak: Int,
    phase: SacredPhonePhase,
    phaseColor: Color,
    selectedPhaseOverride: SacredPhonePhase?,
    onSelectAutoPhase: () -> Unit,
    onSelectPhase: (SacredPhonePhase) -> Unit,
    nextPrayerName: String,
    nextPrayerTimeFormatted: String,
    minutesToNextPrayer: Int,
    prayerStates: List<Pair<String, Boolean>>,
    onTogglePrayer: (Int) -> Unit,
    directive: MuhasibLifeDirective,
    onExecuteDirective: () -> Unit,
    dailyMissions: List<DailyLifeMission>,
    isFocusTimerRunning: Boolean,
    focusSecondsRemaining: Int,
    activeFocusMissionTitle: String?,
    onToggleMission: (Int) -> Unit,
    onStartFocusForMission: (DailyLifeMission) -> Unit,
    onStopFocusTimer: () -> Unit,
    onAddMissionClick: () -> Unit,
    screenMinutesUsed: Int,
    screenBudgetMinutes: Int,
    istighfarTodayCount: Int,
    salahLockEnabled: Boolean,
    nightGuardEnabled: Boolean,
    onIncrementIstighfar: () -> Unit,
    onToggleSalahLock: (Boolean) -> Unit,
    onToggleNightGuard: (Boolean) -> Unit,
    onGrantUsageAccess: () -> Unit,
    onOpenQuran: () -> Unit,
    onOpenSalehBooks: () -> Unit,
    onOpenPanicShield: () -> Unit,
    onLaunchAodSleepMode: () -> Unit,
    onOpenAppDrawer: () -> Unit,
    onOpenMuhasabahLedger: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Editorial Lock-Screen Time & Sanctuary Header
        item {
            HeavenlyEditorialHeader(
                currentTimeText = currentTimeText,
                hijriDateString = hijriDateString,
                cleanDaysStreak = cleanDaysStreak,
                phaseColor = phaseColor,
                onSetDefaultHome = {
                    HapticFeedbackUtil.performClick(context)
                    AppLauncherManager.openDefaultHomeSettings(context)
                },
                onOpenMuhasabahLedger = onOpenMuhasabahLedger
            )
        }

        // Smooth Liquid Glass Phase Orbit Selector
        item {
            HeavenlyPhaseOrbitBar(
                activePhase = phase,
                isAutoMode = selectedPhaseOverride == null,
                onSelectAuto = onSelectAutoPhase,
                onSelectPhase = onSelectPhase
            )
        }

        // Primary Liquid Glass Mihrab Sanctuary Card + 5-Salah Orbital Pills
        item {
            HeavenlyMihrabSanctuaryCard(
                phase = phase,
                phaseColor = phaseColor,
                nextPrayerName = nextPrayerName,
                nextPrayerTimeFormatted = nextPrayerTimeFormatted,
                minutesToNextPrayer = minutesToNextPrayer,
                prayerStates = prayerStates,
                onTogglePrayer = onTogglePrayer
            )
        }

        // Contextual Life Advisor Card
        item {
            HeavenlyLifeAdvisorCard(
                directive = directive,
                accentColor = phaseColor,
                onExecuteDirective = onExecuteDirective
            )
        }

        // Daily Amal Missions & Barakah Focus Card
        item {
            HeavenlyAmalMissionsCard(
                missions = dailyMissions,
                accentColor = phaseColor,
                isFocusTimerRunning = isFocusTimerRunning,
                focusSecondsRemaining = focusSecondsRemaining,
                activeFocusMissionTitle = activeFocusMissionTitle,
                onToggleMission = onToggleMission,
                onStartFocusForMission = onStartFocusForMission,
                onStopFocusTimer = onStopFocusTimer,
                onAddMissionClick = onAddMissionClick
            )
        }

        // Screen-Time Stewardship & Guardian Controls Card
        item {
            HeavenlyStewardshipCard(
                screenMinutesUsed = screenMinutesUsed,
                screenBudgetMinutes = screenBudgetMinutes,
                istighfarTodayCount = istighfarTodayCount,
                salahLockEnabled = salahLockEnabled,
                nightGuardEnabled = nightGuardEnabled,
                accentColor = phaseColor,
                onIncrementIstighfar = onIncrementIstighfar,
                onToggleSalahLock = onToggleSalahLock,
                onToggleNightGuard = onToggleNightGuard,
                onGrantUsageAccess = onGrantUsageAccess
            )
        }

        // Bento Quick Access Section
        item {
            HeavenlyBentoTilesRow(
                accentColor = phaseColor,
                onOpenQuran = onOpenQuran,
                onOpenSalehBooks = onOpenSalehBooks,
                onOpenPanicShield = onOpenPanicShield,
                onLaunchAodSleepMode = onLaunchAodSleepMode,
                onOpenFullAppDrawer = onOpenAppDrawer
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// PINNED PHONE DOCK (Permanently at the bottom of the Home Launcher)
// -------------------------------------------------------------------------------------------------
@Composable
private fun PinnedPhoneBottomDock(
    accentColor: Color,
    onOpenDialer: () -> Unit,
    onOpenMessaging: () -> Unit,
    onToggleSanctuary: () -> Unit,
    onOpenCamera: () -> Unit,
    onOpenAppDrawer: () -> Unit
) {
    val dockShape = RoundedCornerShape(32.dp)
    val champagne = Color(0xFFE5C185)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .shadow(
                elevation = 24.dp,
                shape = dockShape,
                ambientColor = Color.Black,
                spotColor = champagne.copy(alpha = 0.35f)
            ),
        shape = dockShape,
        color = Color(0xE8101522),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.38f),
                    champagne.copy(alpha = 0.28f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Phone
            DockIconButton(
                icon = Icons.Default.Phone,
                label = "Phone",
                tint = Color(0xFF10B981),
                onClick = onOpenDialer
            )
            // 2. Messaging
            DockIconButton(
                icon = Icons.Default.Message,
                label = "Messages",
                tint = Color(0xFF38BDF8),
                onClick = onOpenMessaging
            )
            // 3. Central Sanctuary Orbit Toggle
            Surface(
                shape = CircleShape,
                color = champagne.copy(alpha = 0.25f),
                border = BorderStroke(1.5.dp, champagne),
                modifier = Modifier
                    .size(50.dp)
                    .clickable { onToggleSanctuary() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Sanctuary",
                        tint = champagne,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            // 4. Camera
            DockIconButton(
                icon = Icons.Default.CameraAlt,
                label = "Camera",
                tint = Color(0xFFDFB2A9),
                onClick = onOpenCamera
            )
            // 5. App Drawer
            DockIconButton(
                icon = Icons.Default.Apps,
                label = "Apps",
                tint = Color(0xFFF5F2EB),
                onClick = onOpenAppDrawer
            )
        }
    }
}

@Composable
private fun DockIconButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier
            .size(44.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENTS: Segmented Mode Switcher & Banner
// -------------------------------------------------------------------------------------------------
@Composable
private fun LauncherModeSegmentedToggle(
    currentMode: LauncherScreenMode,
    accentColor: Color,
    onSelectMode: (LauncherScreenMode) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.07f),
        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.14f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val modes = listOf(
                LauncherScreenMode.MINIMAL_PHONE to "📱 واجهة الهاتف (Phone Launcher)",
                LauncherScreenMode.SANCTUARY_COCKPIT to "🕌 محراب الحارس (Spiritual Sanctuary)"
            )
            modes.forEach { (mode, title) ->
                val isSelected = currentMode == mode
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSelected) accentColor.copy(alpha = 0.24f) else Color.Transparent,
                    border = if (isSelected) BorderStroke(0.8.dp, accentColor.copy(alpha = 0.5f)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectMode(mode) }
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) Color(0xFFFBF9F5) else Color.White.copy(alpha = 0.55f),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SetDefaultLauncherBanner(
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0x33E5C185),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("⚡", fontSize = 16.sp)
                Column {
                    Text(
                        text = "تعيين المحاسب واجهة هاتفك الرئيسية",
                        color = Color(0xFFFBF9F5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Set as Default Home Launcher",
                        color = accentColor,
                        fontSize = 10.sp
                    )
                }
            }
            GlassmorphicBadge(text = "تفعيل الآن", color = accentColor)
        }
    }
}

@Composable
private fun QuickAppLauncherTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = Color(0xFFFBF9F5), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
        }
    }
}

@Composable
private fun AppIconLauncherCell(
    app: InstalledLauncherApp,
    accentColor: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val iconBitmap = remember(app.packageName) {
        AppLauncherManager.getAppIconBitmap(context, app.packageName, 128)?.asImageBitmap()
    }

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.08f),
            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.18f)),
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = app.label,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Text(
                        text = app.label.take(1).uppercase(),
                        color = accentColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.label,
            color = Color(0xFFF5F2EB),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

// -------------------------------------------------------------------------------------------------
// COCKPIT SUB-COMPONENTS
// -------------------------------------------------------------------------------------------------
@Composable
private fun HeavenlyDynamicIslandCapsule(
    isExpanded: Boolean,
    nextPrayerName: String,
    minutesToNextPrayer: Int,
    isFocusTimerRunning: Boolean,
    focusSecondsRemaining: Int,
    istighfarCount: Int,
    accentColor: Color,
    onToggleExpand: () -> Unit,
    onIncrementIstighfar: () -> Unit
) {
    val islandHeight by animateDpAsState(
        targetValue = if (isExpanded) 104.dp else 40.dp,
        animationSpec = spring(dampingRatio = 0.74f, stiffness = Spring.StiffnessMediumLow),
        label = "dynamic_island_height"
    )
    val shape = RoundedCornerShape(if (isExpanded) 28.dp else 999.dp)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(if (isExpanded) 0.98f else 0.88f)
                .height(islandHeight)
                .shadow(16.dp, shape = shape, spotColor = Color.Black.copy(alpha = 0.85f))
                .clickable { onToggleExpand() },
            shape = shape,
            color = Color(0xF50D111A),
            border = BorderStroke(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        accentColor.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
            )
        ) {
            if (!isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(accentColor, CircleShape)
                        )
                        Text(
                            text = "$nextPrayerName in ${minutesToNextPrayer}m",
                            color = Color(0xFFFBF9F5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "أستغفر الله • $istighfarCount",
                        color = accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE SANCTUARY ACTIVITY",
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp
                        )
                        Text(
                            text = "Tap to collapse",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                                color = Color(0xFFF5F2EB),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Next Covenant: $nextPrayerName (-${minutesToNextPrayer}m)",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = accentColor.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)),
                            modifier = Modifier.clickable { onIncrementIstighfar() }
                        ) {
                            Text(
                                text = "+1 Tasbih ($istighfarCount)",
                                color = accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpiredNiyyahLiquidBanner(
    appName: String,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    GlassmorphicCard(
        backgroundColor = Color(0x28DFB2A9),
        borderColor = Color(0x88DFB2A9)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NIYYAH WINDOW COMPLETED • $appName",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Your time-boxed session concluded gently. Welcome back to your sanctuary.",
                    color = Color(0xFFF5F2EB).copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFFF5F2EB))
            }
        }
    }
}

@Composable
private fun HeavenlyEditorialHeader(
    currentTimeText: String,
    hijriDateString: String,
    cleanDaysStreak: Int,
    phaseColor: Color,
    onSetDefaultHome: () -> Unit,
    onOpenMuhasabahLedger: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = hijriDateString.uppercase(),
            color = Color(0xFFF5F2EB).copy(alpha = 0.72f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.8.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = currentTimeText,
            color = Color(0xFFFBF9F5),
            fontSize = 68.sp,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Serif,
            letterSpacing = (-1.5).sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassmorphicBadge(
                text = "$cleanDaysStreak Days Serene • Evening Ledger",
                color = phaseColor,
                modifier = Modifier.clickable { onOpenMuhasabahLedger() }
            )
            GlassmorphicBadge(
                text = "Default Home OS",
                color = Color(0xFFF5F2EB),
                modifier = Modifier.clickable { onSetDefaultHome() }
            )
        }
    }
}

@Composable
private fun HeavenlyPhaseOrbitBar(
    activePhase: SacredPhonePhase,
    isAutoMode: Boolean,
    onSelectAuto: () -> Unit,
    onSelectPhase: (SacredPhonePhase) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = isAutoMode,
                onClick = onSelectAuto,
                shape = RoundedCornerShape(999.dp),
                label = { Text("Solar Orbit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = activePhase.primaryColor.copy(alpha = 0.24f),
                    selectedLabelColor = Color(0xFFFBF9F5)
                )
            )
        }
        items(SacredPhonePhase.entries) { phase ->
            val isSelected = !isAutoMode && activePhase == phase
            FilterChip(
                selected = isSelected,
                onClick = { onSelectPhase(phase) },
                shape = RoundedCornerShape(999.dp),
                label = {
                    Text(
                        text = phase.englishTitle,
                        fontSize = 11.sp,
                        fontWeight = if (activePhase == phase) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = phase.primaryColor.copy(alpha = 0.24f),
                    selectedLabelColor = phase.primaryColor
                )
            )
        }
    }
}

@Composable
private fun HeavenlyMihrabSanctuaryCard(
    phase: SacredPhonePhase,
    phaseColor: Color,
    nextPrayerName: String,
    nextPrayerTimeFormatted: String,
    minutesToNextPrayer: Int,
    prayerStates: List<Pair<String, Boolean>>,
    onTogglePrayer: (Int) -> Unit
) {
    GlassmorphicCard(borderColor = phaseColor.copy(alpha = 0.5f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NEXT PRAYER COVENANT",
                    color = phaseColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp
                )
                Text(
                    text = "صلاة $nextPrayerName",
                    color = Color(0xFFFBF9F5),
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-${minutesToNextPrayer}m",
                    color = phaseColor,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Adhan: $nextPrayerTimeFormatted",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            prayerStates.forEachIndexed { idx, pair ->
                val isDone = pair.second
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDone) phaseColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(0.8.dp, if (isDone) phaseColor else Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTogglePrayer(idx) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isDone) "●" else "○",
                            color = if (isDone) phaseColor else Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = pair.first,
                            color = Color(0xFFFBF9F5),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeavenlyLifeAdvisorCard(
    directive: MuhasibLifeDirective,
    accentColor: Color,
    onExecuteDirective: () -> Unit
) {
    GlassmorphicCard(borderColor = accentColor.copy(alpha = 0.5f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = directive.arabicBadge,
                color = accentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            GlassmorphicBadge(text = "Solar Directive", color = accentColor)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = directive.headline,
            color = Color(0xFFFBF9F5),
            fontSize = 17.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = directive.detailText,
            color = Color(0xFFF5F2EB).copy(alpha = 0.78f),
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))
        GlassmorphicButton(
            onClick = onExecuteDirective,
            modifier = Modifier.fillMaxWidth(),
            accentColor = accentColor
        ) {
            Text(directive.primaryCtaLabel, color = Color(0xFF0A0D14), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun HeavenlyAmalMissionsCard(
    missions: List<DailyLifeMission>,
    accentColor: Color,
    isFocusTimerRunning: Boolean,
    focusSecondsRemaining: Int,
    activeFocusMissionTitle: String?,
    onToggleMission: (Int) -> Unit,
    onStartFocusForMission: (DailyLifeMission) -> Unit,
    onStopFocusTimer: () -> Unit,
    onAddMissionClick: () -> Unit
) {
    GlassmorphicCard(borderColor = accentColor.copy(alpha = 0.45f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DAILY AMAL MISSIONS",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp
                )
                val completed = missions.count { it.isCompleted }
                Text(
                    text = "$completed of ${missions.size} Blocks Sealed",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onAddMissionClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Mission", tint = accentColor)
            }
        }

        if (isFocusTimerRunning && activeFocusMissionTitle != null) {
            val mins = focusSecondsRemaining / 60
            val secs = focusSecondsRemaining % 60
            val formatted = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = accentColor.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ACTIVE FOCUS SESSION", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(activeFocusMissionTitle, color = Color(0xFFFBF9F5), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatted, color = accentColor, fontSize = 20.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onStopFocusTimer) {
                            Icon(Icons.Default.Close, contentDescription = "Stop", tint = Color(0xFFDFB2A9))
                        }
                    }
                }
            }
        }

        missions.forEach { mission ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onToggleMission(mission.id) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (mission.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (mission.isCompleted) accentColor else Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = mission.title,
                                color = if (mission.isCompleted) Color.White.copy(alpha = 0.45f) else Color(0xFFFBF9F5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${mission.prayerAnchor} • ${mission.estimatedMinutes}m",
                                color = accentColor.copy(alpha = 0.75f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    if (!mission.isCompleted) {
                        OutlinedButton(
                            onClick = { onStartFocusForMission(mission) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Focus", fontSize = 10.sp, color = accentColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeavenlyStewardshipCard(
    screenMinutesUsed: Int,
    screenBudgetMinutes: Int,
    istighfarTodayCount: Int,
    salahLockEnabled: Boolean,
    nightGuardEnabled: Boolean,
    accentColor: Color,
    onIncrementIstighfar: () -> Unit,
    onToggleSalahLock: (Boolean) -> Unit,
    onToggleNightGuard: (Boolean) -> Unit,
    onGrantUsageAccess: () -> Unit
) {
    val progress = (screenMinutesUsed.toFloat() / screenBudgetMinutes.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val barColor = if (screenMinutesUsed > screenBudgetMinutes) Color(0xFFDFB2A9) else accentColor

    GlassmorphicCard(borderColor = accentColor.copy(alpha = 0.4f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SCREEN-TIME STEWARDSHIP",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp
                )
                Text(
                    text = "$screenMinutesUsed of ${screenBudgetMinutes}m Used Today",
                    color = Color(0xFFFBF9F5),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            GlassmorphicBadge(
                text = "أستغفر الله • $istighfarTodayCount",
                color = accentColor,
                modifier = Modifier.clickable { onIncrementIstighfar() }
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = barColor,
            trackColor = Color.White.copy(alpha = 0.1f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = salahLockEnabled,
                    onCheckedChange = onToggleSalahLock,
                    colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Salah Stillness", color = Color(0xFFF5F2EB), fontSize = 11.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = nightGuardEnabled,
                    onCheckedChange = onToggleNightGuard,
                    colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Velvet Night", color = Color(0xFFF5F2EB), fontSize = 11.sp)
            }

            Text(
                text = "Sync Usage",
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onGrantUsageAccess() }
            )
        }
    }
}

@Composable
private fun HeavenlyBentoTilesRow(
    accentColor: Color,
    onOpenQuran: () -> Unit,
    onOpenSalehBooks: () -> Unit,
    onOpenPanicShield: () -> Unit,
    onLaunchAodSleepMode: () -> Unit,
    onOpenFullAppDrawer: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HeavenlyBentoTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.MenuBook,
                label = "Quran Wird",
                accentColor = accentColor,
                onClick = onOpenQuran
            )
            HeavenlyBentoTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Book,
                label = "Saleh Library",
                accentColor = accentColor,
                onClick = onOpenSalehBooks
            )
            HeavenlyBentoTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Security,
                label = "Serenity Shield",
                accentColor = Color(0xFFDFB2A9),
                onClick = onOpenPanicShield
            )
            HeavenlyBentoTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Bedtime,
                label = "Starlight AOD",
                accentColor = Color(0xFFE5C185),
                onClick = onLaunchAodSleepMode
            )
        }

        GlassmorphicButton(
            onClick = onOpenFullAppDrawer,
            modifier = Modifier.fillMaxWidth(),
            accentColor = accentColor
        ) {
            Icon(Icons.Default.Apps, contentDescription = null, tint = Color(0xFF0A0D14), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("فتح درج التطبيقات الكامل (App Drawer)", color = Color(0xFF0A0D14), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HeavenlyBentoTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        borderColor = accentColor.copy(alpha = 0.42f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = accentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = Color(0xFFFBF9F5),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 3. FULL SEARCHABLE APP DRAWER SHEET (With Real Icons & Niyyah Gate)
// -------------------------------------------------------------------------------------------------
@Composable
fun IntentionalAppDrawerSheet(
    apps: List<InstalledLauncherApp>,
    currentPhase: SacredPhonePhase,
    accentColor: Color,
    onSelectApp: (InstalledLauncherApp) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTierFilter by remember { mutableStateOf<AppSanctuaryTier?>(null) }

    val filteredApps = remember(apps, searchQuery, selectedTierFilter) {
        apps.filter { app ->
            val matchesQuery = searchQuery.isBlank() || app.label.contains(searchQuery, ignoreCase = true) || app.packageName.contains(searchQuery, ignoreCase = true)
            val matchesTier = selectedTierFilter == null || app.tier == selectedTierFilter
            matchesQuery && matchesTier
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "INTENTIONAL APP SANCTUARY • بَوَّابَةُ النِّيَّة",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp
                )
                Text(
                    text = "${currentPhase.englishTitle} • Mindful Niyyah Gate Active",
                    color = Color(0xFFF5F2EB).copy(alpha = 0.65f),
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFF5F2EB))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search applications with intention...", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentColor) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
                focusedTextColor = Color(0xFFFBF9F5),
                unfocusedTextColor = Color(0xFFFBF9F5)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedTierFilter == null,
                    onClick = { selectedTierFilter = null },
                    shape = RoundedCornerShape(999.dp),
                    label = { Text("All (${apps.size})", fontSize = 11.sp) }
                )
            }
            items(AppSanctuaryTier.entries) { tier ->
                FilterChip(
                    selected = selectedTierFilter == tier,
                    onClick = { selectedTierFilter = tier },
                    shape = RoundedCornerShape(999.dp),
                    label = { Text(tier.title, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                val isGuarded = app.tier == AppSanctuaryTier.GUARDED_DOPAMINE
                val badgeColor = when {
                    app.isOverDailyBudget -> Color(0xFFDFB2A9)
                    isGuarded -> Color(0xFFE5C185)
                    else -> accentColor
                }

                val iconBitmap = remember(app.packageName) {
                    AppLauncherManager.getAppIconBitmap(context, app.packageName, 128)?.asImageBitmap()
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(0.7.dp, badgeColor.copy(alpha = 0.32f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectApp(app) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Real App Icon or Letter Fallback
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (iconBitmap != null) {
                                        Image(
                                            bitmap = iconBitmap,
                                            contentDescription = app.label,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    } else {
                                        Text(
                                            text = app.label.take(1).uppercase(),
                                            color = badgeColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = app.label,
                                        color = Color(0xFFFBF9F5),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = app.tier.arabicBadge,
                                        color = badgeColor,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Serif
                                    )
                                }
                                Text(
                                    text = "${app.tier.title} • ${app.usageMinutesToday}m / ${app.dailyLimitMinutes}m",
                                    color = Color.White.copy(alpha = 0.58f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        GlassmorphicBadge(
                            text = if (app.tier.requiresNiyyahGate) "Niyyah Gate" else "Direct",
                            color = badgeColor
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// DIALOGS: Niyyah Gate, Hard Block, Muhasabah Ledger, Add Mission
// -------------------------------------------------------------------------------------------------
@Composable
fun NiyyahGateInterceptorDialog(
    decision: AppLaunchDecision.RequiresNiyyahGate,
    accentColor: Color,
    onCancel: () -> Unit,
    onRedirectToQuranOrBooks: () -> Unit,
    onConfirmNiyyahAndLaunch: (String, Int) -> Unit
) {
    val context = LocalContext.current
    val presetIntentions = listOf(
        "Replying to a specific person or client",
        "Checking a required study or work reference",
        "Publishing or uploading scheduled work"
    )
    var selectedIntention by remember { mutableStateOf(presetIntentions.first()) }
    var selectedMinutes by remember { mutableIntStateOf(decision.suggestedTimeBoxMinutes) }
    var tasbihTapsRemaining by remember { mutableIntStateOf(3) }

    Dialog(onDismissRequest = onCancel) {
        GlassmorphicCard(
            backgroundColor = Color(0xEE121622),
            borderColor = accentColor.copy(alpha = 0.65f),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text(
                text = "بَوَّابَةُ النِّيَّة • MINDFUL NIYYAH GATE",
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )
            Text(
                text = "Entering ${decision.app.label}",
                color = Color(0xFFFBF9F5),
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = decision.reason,
                color = Color(0xFFF5F2EB).copy(alpha = 0.75f),
                fontSize = 12.sp
            )

            Text(
                text = "1. Declare your purposeful Niyyah:",
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            presetIntentions.forEach { preset ->
                val isPicked = selectedIntention == preset
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isPicked) accentColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(0.8.dp, if (isPicked) accentColor else Color.White.copy(alpha = 0.14f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedIntention = preset }
                ) {
                    Text(
                        text = preset,
                        color = Color(0xFFFBF9F5),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Text(
                text = "2. Choose your time-box (Dynamic Island timer):",
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15).forEach { mins ->
                    FilterChip(
                        selected = selectedMinutes == mins,
                        onClick = { selectedMinutes = mins },
                        shape = RoundedCornerShape(999.dp),
                        label = { Text("${mins}m Window", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            if (tasbihTapsRemaining > 0) {
                GlassmorphicButton(
                    onClick = {
                        HapticFeedbackUtil.performTick(context)
                        tasbihTapsRemaining--
                    },
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = accentColor
                ) {
                    Text(
                        text = "Mindful Pause: سُبْحَانَ اللَّهِ ($tasbihTapsRemaining remaining)",
                        color = Color(0xFF0A0D14),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else {
                GlassmorphicButton(
                    onClick = {
                        HapticFeedbackUtil.performHeavyClick(context)
                        onConfirmNiyyahAndLaunch(selectedIntention, selectedMinutes)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = accentColor
                ) {
                    Text(
                        text = "Enter ${decision.app.label} for ${selectedMinutes}m",
                        color = Color(0xFF0A0D14),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onRedirectToQuranOrBooks, shape = RoundedCornerShape(14.dp)) {
                    Text("Read Saleh Book", color = accentColor, fontSize = 11.sp)
                }
                OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(14.dp)) {
                    Text("Remain in Sanctuary", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun HardBlockedAppModal(
    decision: AppLaunchDecision.HardBlocked,
    accentColor: Color,
    onDismiss: () -> Unit,
    onOpenSalehBooksInstead: () -> Unit,
    onOpenPanicShieldInstead: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassmorphicCard(
            backgroundColor = Color(0xEE17121A),
            borderColor = Color(0xFFDFB2A9),
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFFDFB2A9),
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = decision.arabicRule,
                    color = accentColor,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = decision.blockTitle,
                    color = Color(0xFFFBF9F5),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = decision.explanation,
                    color = Color(0xFFF5F2EB).copy(alpha = 0.76f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                GlassmorphicButton(
                    onClick = onOpenSalehBooksInstead,
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = accentColor
                ) {
                    Text("Continue Saleh Reading Wird Instead", color = Color(0xFF0A0D14), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenPanicShieldInstead,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(0.8.dp, Color(0xFFDFB2A9))
                ) {
                    Text("Open Serenity Shield", color = Color(0xFFDFB2A9))
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Return to Sanctuary", color = Color(0xFFF5F2EB))
                }
            }
        }
    }
}

@Composable
fun EveningMuhasabahLedgerSheet(
    completedPrayersCount: Int,
    completedMissionsCount: Int,
    totalMissionsCount: Int,
    screenMinutesUsed: Int,
    screenBudgetMinutes: Int,
    istighfarCount: Int,
    cleanDaysStreak: Int,
    onSealDailyLedger: () -> Unit,
    onEnterAodNow: () -> Unit
) {
    var guardedGazeAndTongue by remember { mutableStateOf(true) }
    var forgaveOthersBeforeSleep by remember { mutableStateOf(true) }

    val prayerScore = (completedPrayersCount * 20).coerceIn(0, 100)
    val amalScore = if (totalMissionsCount > 0) (completedMissionsCount * 100) / totalMissionsCount else 100
    val timeScore = if (screenMinutesUsed <= screenBudgetMinutes) 100 else (100 - (screenMinutesUsed - screenBudgetMinutes)).coerceAtLeast(30)
    val overallMuhasabahScore = ((prayerScore * 0.45f) + (amalScore * 0.25f) + (timeScore * 0.15f) + (if (guardedGazeAndTongue) 15f else 0f)).toInt()
    val champagne = Color(0xFFE5C185)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "خَلْوَةُ المُحَاسَبَة • EVENING MUHASABAH LEDGER",
            color = champagne,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp
        )
        Text(
            text = "«حَاسِبُوا أَنْفُسَكُمْ قَبْلَ أَنْ تُحَاسَبُوا، وَزِنُوا أَعْمَالَكُمْ قَبْلَ أَنْ تُوزَنَ عَلَيْكُمْ»",
            color = Color(0xFFFBF9F5),
            fontSize = 17.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
        )

        GlassmorphicCard(borderColor = champagne.copy(alpha = 0.5f)) {
            Text("Today's Sanctuary Integrity", color = Color.White.copy(alpha = 0.68f), fontSize = 12.sp)
            Text(
                text = "$overallMuhasabahScore% Muhasabah Harmony",
                color = Color(0xFFFBF9F5),
                fontSize = 24.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Salah: $completedPrayersCount/5 • Amal: $completedMissionsCount/$totalMissionsCount • Istighfar: $istighfarCount • Serenity: ${cleanDaysStreak}d",
                color = champagne,
                fontSize = 12.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Guarded gaze, tongue & digital serenity today?", color = Color(0xFFF5F2EB), fontSize = 13.sp)
            Switch(checked = guardedGazeAndTongue, onCheckedChange = { guardedGazeAndTongue = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Released rancor & renewed Tawbah before rest?", color = Color(0xFFF5F2EB), fontSize = 13.sp)
            Switch(checked = forgaveOthersBeforeSleep, onCheckedChange = { forgaveOthersBeforeSleep = it })
        }

        GlassmorphicButton(
            onClick = onSealDailyLedger,
            modifier = Modifier.fillMaxWidth(),
            accentColor = champagne
        ) {
            Text("Seal Evening Muhasabah Ledger", color = Color(0xFF0A0D14), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        OutlinedButton(
            onClick = onEnterAodNow,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(0.8.dp, champagne.copy(alpha = 0.5f))
        ) {
            Text("Seal & Enter 6% Starlight Always-On Display", color = champagne, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AddAmalMissionDialog(
    accentColor: Color,
    onDismiss: () -> Unit,
    onAddMission: (String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedAnchor by remember { mutableStateOf("DUHA_BLOCK") }
    var minutes by remember { mutableIntStateOf(25) }

    Dialog(onDismissRequest = onDismiss) {
        GlassmorphicCard(
            backgroundColor = Color(0xEE121622),
            borderColor = accentColor,
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("New Intentional Amal Block", color = Color(0xFFFBF9F5), fontSize = 18.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("e.g., Memorize 5 Ayahs / Deep Work Deliverable") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("POST_FAJR", "DUHA_BLOCK", "ASR_BLOCK").forEach { anchor ->
                    FilterChip(
                        selected = selectedAnchor == anchor,
                        onClick = { selectedAnchor = anchor },
                        shape = RoundedCornerShape(999.dp),
                        label = { Text(anchor.replace("_", " "), fontSize = 10.sp) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(15, 25, 45, 90).forEach { m ->
                    FilterChip(
                        selected = minutes == m,
                        onClick = { minutes = m },
                        shape = RoundedCornerShape(999.dp),
                        label = { Text("${m}m", fontSize = 11.sp) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(14.dp)) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onAddMission(title.trim(), selectedAnchor, minutes)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color(0xFF0A0D14))
                ) {
                    Text("Save Block", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
