package com.example.ui.screens

import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DailyLifeMission
import com.example.floating.FloatingDhikrService
import com.example.ui.components.GlassmorphicBadge
import com.example.ui.components.GlassmorphicButton
import com.example.ui.components.GlassmorphicCard
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
 * MihrabHomeLauncherScreen — Heavenly iOS 26 Liquid Glass & Apple HIG Edition.
 *
 * Crafted following `emilkowalski/skills` (apple-design) and `Leonxlnx/taste-skill` (design-taste-frontend):
 * - Zero harsh neon green (#00E676) or emoji clutter.
 * - Multi-stop celestial horizon shader background with warm champagne gold (#E5C185),
 *   pearlescent alabaster (#F5F2EB), dawn silk rose (#DFB2A9), and twilight mist (#9BBCD6).
 * - Interactive Dynamic Island Live Activity capsule at the top of the screen.
 * - Refractive Liquid Glass cards (`GlassmorphicCard`) with 42% specular top-left rim lighting,
 *   24dp ambient shadows, and physical spring press states (`dampingRatio = 0.74f`).
 */
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
    onOpenPanicShield: () -> Unit = {},
    onLaunchAodSleepMode: () -> Unit = {},
    onPrayerCompletedToggle: (String, Boolean) -> Unit = { _, _ -> },
    onIncrementIstighfar: () -> Unit = {}
) {
    val context = LocalContext.current

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
    val dailyMissions = remember {
        mutableStateListOf<DailyLifeMission>()
    }

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
                        title = "Read Chunk #4 of Al-Daa wa Al-Dawaa & Reflect",
                        prayerAnchor = "POST_FAJR",
                        category = "DEEN",
                        estimatedMinutes = 15,
                        isCompleted = false
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

    // Heavenly Multi-Stop Atmospheric Horizon Background
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
        // Subtle Celestial Horizon Glow Orb behind the Liquid Glass layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            phasePrimaryColor.copy(alpha = 0.24f),
                            phasePrimaryColor.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(540f, 260f),
                        radius = 780f
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 0. Interactive iPhone-Style Dynamic Island Live Activity Capsule
            item {
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
            }

            if (!expiredNiyyahAppAlert.isNullOrBlank()) {
                item {
                    ExpiredNiyyahLiquidBanner(
                        appName = expiredNiyyahAppAlert,
                        accentColor = phasePrimaryColor,
                        onDismiss = onDismissExpiredAlert
                    )
                }
            }

            // 1. Editorial Lock-Screen / Home OS Time & Sanctuary Header
            item {
                HeavenlyEditorialHeader(
                    currentTimeText = currentTimeText,
                    hijriDateString = hijriDateString,
                    cleanDaysStreak = cleanDaysStreak,
                    phaseColor = phasePrimaryColor,
                    onSetDefaultHome = {
                        HapticFeedbackUtil.performClick(context)
                        AppLauncherManager.openDefaultHomeSettings(context)
                    },
                    onOpenMuhasabahLedger = {
                        HapticFeedbackUtil.performClick(context)
                        showMuhasabahLedger = true
                    }
                )
            }

            // 2. Smooth Liquid Glass Phase Orbit Selector
            item {
                HeavenlyPhaseOrbitBar(
                    activePhase = currentPhase,
                    isAutoMode = selectedPhaseOverride == null,
                    onSelectAuto = {
                        HapticFeedbackUtil.performTick(context)
                        selectedPhaseOverride = null
                    },
                    onSelectPhase = { phase ->
                        HapticFeedbackUtil.performClick(context)
                        selectedPhaseOverride = phase
                    }
                )
            }

            // 3. Primary Liquid Glass Mihrab Sanctuary Card + 5-Salah Orbital Pills
            item {
                HeavenlyMihrabSanctuaryCard(
                    phase = currentPhase,
                    phaseColor = phasePrimaryColor,
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
                    }
                )
            }

            // 4. Contextual Life Advisor Card (Refractive Specular Glass)
            item {
                HeavenlyLifeAdvisorCard(
                    directive = directive,
                    accentColor = phasePrimaryColor,
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
                    }
                )
            }

            // 5. Daily Amal Missions & Barakah Focus Card
            item {
                HeavenlyAmalMissionsCard(
                    missions = dailyMissions,
                    accentColor = phasePrimaryColor,
                    isFocusTimerRunning = isFocusTimerRunning,
                    focusSecondsRemaining = focusSecondsRemaining,
                    activeFocusMissionTitle = activeFocusMissionTitle,
                    onToggleMission = { missionId ->
                        HapticFeedbackUtil.performClick(context)
                        val idx = dailyMissions.indexOfFirst { it.id == missionId }
                        if (idx >= 0) {
                            val item = dailyMissions[idx]
                            val updatedCompleted = !item.isCompleted
                            dailyMissions[idx] = item.copy(isCompleted = updatedCompleted)
                            onToggleMissionPersisted?.invoke(missionId, updatedCompleted)
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
                    }
                )
            }

            // 6. Screen-Time Stewardship & Guardian Controls Card
            item {
                HeavenlyStewardshipCard(
                    screenMinutesUsed = totalScreenMinutesToday,
                    screenBudgetMinutes = dailyScreenBudgetMinutes,
                    istighfarTodayCount = istighfarTodayCount,
                    salahLockEnabled = salahLockEnabled,
                    nightGuardEnabled = nightGuardEnabled,
                    accentColor = phasePrimaryColor,
                    onIncrementIstighfar = {
                        HapticFeedbackUtil.performTick(context)
                        onIncrementIstighfar()
                    },
                    onToggleSalahLock = { salahLockEnabled = it },
                    onToggleNightGuard = { nightGuardEnabled = it },
                    onGrantUsageAccess = {
                        AppLauncherManager.openUsageAccessSettings(context)
                    }
                )
            }

            // 7. iOS-Style Bento Sanctuary Dock + Intentional App Library Trigger
            item {
                HeavenlyBentoDockSection(
                    accentColor = phasePrimaryColor,
                    installedApps = installedApps,
                    onLaunchApp = handleAppLaunchRequest,
                    onOpenQuran = onOpenQuran,
                    onOpenSalehBooks = onOpenSalehBooks,
                    onOpenPanicShield = onOpenPanicShield,
                    onLaunchAodSleepMode = onLaunchAodSleepMode,
                    onOpenFullAppDrawer = {
                        HapticFeedbackUtil.performClick(context)
                        showAppDrawer = true
                    }
                )
            }
        }

        // Intentional App Drawer Sheet
        if (showAppDrawer) {
            ModalBottomSheet(
                onDismissRequest = { showAppDrawer = false },
                containerColor = Color(0xF2101520),
                contentColor = Color(0xFFF5F2EB),
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
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
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
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
        targetValue = if (isExpanded) 108.dp else 40.dp,
        animationSpec = spring(dampingRatio = 0.74f, stiffness = Spring.StiffnessMediumLow),
        label = "dynamic_island_height"
    )
    val shape = RoundedCornerShape(if (isExpanded) 30.dp else 999.dp)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(if (isExpanded) 1f else 0.78f)
                .height(islandHeight)
                .shadow(
                    elevation = 24.dp,
                    shape = shape,
                    ambientColor = Color.Black,
                    spotColor = accentColor.copy(alpha = 0.35f)
                )
                .clip(shape)
                .clickable { onToggleExpand() },
            color = Color(0xEB080B11),
            border = BorderStroke(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.38f),
                        accentColor.copy(alpha = 0.28f),
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFocusTimerRunning) {
                                "Focus %02d:%02d".format(focusSecondsRemaining / 60, focusSecondsRemaining % 60)
                            } else {
                                "$nextPrayerName in ${minutesToNextPrayer}m"
                            },
                            color = Color(0xFFF5F2EB),
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
                        .padding(horizontal = 20.dp, vertical = 14.dp),
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
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Next Covenant: $nextPrayerName (-${minutesToNextPrayer}m)",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
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
                Spacer(modifier = Modifier.height(4.dp))
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
            letterSpacing = 2.sp
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
        Spacer(modifier = Modifier.height(8.dp))

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
                text = "Set Default Home OS",
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
    GlassmorphicCard(
        borderColor = phaseColor.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = phase.arabicTitle,
                    color = phaseColor,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = phase.englishTitle,
                    color = Color(0xFFFBF9F5),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            GlassmorphicBadge(
                text = "$nextPrayerName • $nextPrayerTimeFormatted (-${minutesToNextPrayer}m)",
                color = phaseColor
            )
        }

        Text(
            text = phase.premiseSubtitle,
            color = Color(0xFFF5F2EB).copy(alpha = 0.78f),
            fontSize = 13.sp,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 5 Daily Prayers Liquid Glass Capsules
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            prayerStates.forEachIndexed { idx, (name, isDone) ->
                val capsuleShape = RoundedCornerShape(18.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(capsuleShape)
                        .background(
                            if (isDone) phaseColor.copy(alpha = 0.22f)
                            else Color.White.copy(alpha = 0.06f)
                        )
                        .border(
                            BorderStroke(
                                0.8.dp,
                                if (isDone) phaseColor.copy(alpha = 0.65f)
                                else Color.White.copy(alpha = 0.16f)
                            ),
                            capsuleShape
                        )
                        .clickable { onTogglePrayer(idx) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = name,
                            tint = if (isDone) phaseColor else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            color = if (isDone) Color(0xFFFBF9F5) else Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = if (isDone) FontWeight.Bold else FontWeight.Medium
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
    GlassmorphicCard(
        borderColor = accentColor.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SANCTUARY GUIDANCE",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )
            }
            Text(
                text = directive.arabicBadge,
                color = accentColor,
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = directive.headline,
            color = Color(0xFFFBF9F5),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = directive.detailText,
            color = Color(0xFFF5F2EB).copy(alpha = 0.76f),
            fontSize = 13.sp,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        GlassmorphicButton(
            onClick = onExecuteDirective,
            modifier = Modifier.fillMaxWidth(),
            accentColor = accentColor
        ) {
            Text(
                text = directive.primaryCtaLabel,
                color = Color(0xFF0A0D14),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
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
    GlassmorphicCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DAILY AMAL MISSIONS • الأَعْمَال",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp
                )
                Text(
                    text = "${missions.count { it.isCompleted }} of ${missions.size} Intentional Blocks Completed",
                    color = Color(0xFFF5F2EB).copy(alpha = 0.68f),
                    fontSize = 12.sp
                )
            }

            GlassmorphicBadge(
                text = "+ New Amal",
                color = accentColor,
                modifier = Modifier.clickable { onAddMissionClick() }
            )
        }

        AnimatedVisibility(visible = isFocusTimerRunning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(accentColor.copy(alpha = 0.18f))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DEEP CRAFT SESSION ACTIVE",
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = activeFocusMissionTitle ?: "Barakah Focus Block",
                            color = Color(0xFFFBF9F5),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "%02d:%02d".format(focusSecondsRemaining / 60, focusSecondsRemaining % 60),
                        color = Color(0xFFFBF9F5),
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    OutlinedButton(
                        onClick = onStopFocusTimer,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Conclude", color = Color(0xFFFBF9F5), fontSize = 11.sp)
                    }
                }
            }
        }

        missions.forEach { mission ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(0.6.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                    .clickable { onToggleMission(mission.id) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (mission.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (mission.isCompleted) accentColor else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = mission.title,
                            color = if (mission.isCompleted) Color.White.copy(alpha = 0.45f) else Color(0xFFFBF9F5),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textDecoration = if (mission.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Text(
                            text = "${mission.prayerAnchor.replace("_", " ")} • ${mission.estimatedMinutes}m",
                            color = accentColor.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }

                if (!mission.isCompleted) {
                    IconButton(
                        onClick = { onStartFocusForMission(mission) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Begin Focus",
                            tint = accentColor
                        )
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
    val progress = (screenMinutesUsed.toFloat() / screenBudgetMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)
    val barColor = if (progress >= 0.9f) Color(0xFFDFB2A9) else accentColor

    GlassmorphicCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TIME STEWARDSHIP & STILLNESS",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp
                )
                Text(
                    text = "Screen Presence Today: ${screenMinutesUsed}m of ${screenBudgetMinutes}m",
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
                Text("Salah Stillness", color = Color(0xFFF5F2EB), fontSize = 12.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = nightGuardEnabled,
                    onCheckedChange = onToggleNightGuard,
                    colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Velvet Night", color = Color(0xFFF5F2EB), fontSize = 12.sp)
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
private fun HeavenlyBentoDockSection(
    accentColor: Color,
    installedApps: List<InstalledLauncherApp>,
    onLaunchApp: (InstalledLauncherApp) -> Unit,
    onOpenQuran: () -> Unit,
    onOpenSalehBooks: () -> Unit,
    onOpenPanicShield: () -> Unit,
    onLaunchAodSleepMode: () -> Unit,
    onOpenFullAppDrawer: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val dialerApp = installedApps.firstOrNull { it.packageName.contains("dialer") || it.label.contains("Phone", true) }
            val smsApp = installedApps.firstOrNull { it.packageName.contains("messaging") || it.label.contains("Message", true) }

            OutlinedButton(
                onClick = { dialerApp?.let(onLaunchApp) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.24f))
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFFF5F2EB), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Phone", color = Color(0xFFF5F2EB), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            OutlinedButton(
                onClick = { smsApp?.let(onLaunchApp) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.24f))
            ) {
                Icon(Icons.Default.Message, contentDescription = null, tint = Color(0xFFF5F2EB), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Messages", color = Color(0xFFF5F2EB), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            GlassmorphicButton(
                onClick = onOpenFullAppDrawer,
                modifier = Modifier
                    .weight(1.6f)
                    .height(50.dp),
                accentColor = accentColor
            ) {
                Icon(Icons.Default.Apps, contentDescription = null, tint = Color(0xFF0A0D14), modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("App Sanctuary", color = Color(0xFF0A0D14), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
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
        shape = RoundedCornerShape(22.dp),
        borderColor = accentColor.copy(alpha = 0.42f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = accentColor, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(6.dp))
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

@Composable
fun IntentionalAppDrawerSheet(
    apps: List<InstalledLauncherApp>,
    currentPhase: SacredPhonePhase,
    accentColor: Color,
    onSelectApp: (InstalledLauncherApp) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTierFilter by remember { mutableStateOf<AppSanctuaryTier?>(null) }

    val filteredApps = remember(apps, searchQuery, selectedTierFilter) {
        apps.filter { app ->
            val matchesQuery = searchQuery.isBlank() || app.label.contains(searchQuery, ignoreCase = true)
            val matchesTier = selectedTierFilter == null || app.tier == selectedTierFilter
            matchesQuery && matchesTier
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 12.dp)
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
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFF5F2EB))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search applications with intention...", color = Color.White.copy(alpha = 0.4f)) },
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

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                val isGuarded = app.tier == AppSanctuaryTier.GUARDED_DOPAMINE
                val badgeColor = when {
                    app.isOverDailyBudget -> Color(0xFFDFB2A9)
                    isGuarded -> Color(0xFFE5C185)
                    else -> accentColor
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.32f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectApp(app) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = app.label,
                                    color = Color(0xFFFBF9F5),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = app.tier.arabicBadge,
                                    color = badgeColor,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Serif
                                )
                            }
                            Text(
                                text = "${app.tier.title} • ${app.usageMinutesToday}m of ${app.dailyLimitMinutes}m today",
                                color = Color.White.copy(alpha = 0.58f),
                                fontSize = 11.sp
                            )
                        }

                        GlassmorphicBadge(
                            text = if (app.tier.requiresNiyyahGate) "Niyyah Gate" else "Direct Open",
                            color = badgeColor
                        )
                    }
                }
            }
        }
    }
}

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