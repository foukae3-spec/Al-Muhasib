package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AuroraBackground
import com.example.ui.components.FloatingPillNavigationBar
import com.example.ui.components.InAppAdhanBanner
import com.example.ui.components.NavigationTab
import com.example.ui.screens.AdhkarScreen
import com.example.ui.screens.AodSettingsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.OnboardingDialog
import com.example.ui.screens.PanicSheet
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.RecoveryScreen
import com.example.ui.theme.AlMuhasibTheme
import com.example.ui.theme.LocalAppColors
import com.example.ui.viewmodel.MuhasibViewModel
import com.example.ui.viewmodel.QuranViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MuhasibViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            var isDarkMode by remember { mutableStateOf(true) }

            AlMuhasibTheme(
                appTheme = uiState.currentTheme,
                darkTheme = isDarkMode
            ) {
                MainAppContent(
                    isDarkMode = isDarkMode,
                    onToggleTheme = { isDarkMode = !isDarkMode },
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("EXTRA_TRIGGER_PANIC", false) == true) {
            viewModel.setPanicSheetVisible(true)
        }
    }
}

@Composable
fun MainAppContent(
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: MuhasibViewModel
) {
    val appColors = LocalAppColors.current
    var currentTab by remember { mutableStateOf(NavigationTab.GUARDIAN) }
    val quranViewModel: QuranViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val todayPrayerLog by viewModel.todayPrayerLog.collectAsStateWithLifecycle()
    val sobrietyLog by viewModel.sobrietyState.collectAsStateWithLifecycle()
    val allDhikr by viewModel.allDhikr.collectAsStateWithLifecycle()
    val muhasabahHistory by viewModel.muhasabahHistory.collectAsStateWithLifecycle()

    // ponytail: observe active Adhan audio state across all screens
    val isAdhanPlaying by viewModel.isAdhanPlaying.collectAsStateWithLifecycle()
    val currentAdhanPrayer by viewModel.currentAdhanPrayer.collectAsStateWithLifecycle()
    val isFullscreenReaderActive by quranViewModel.isFullscreenReaderActive.collectAsStateWithLifecycle()

    // Request Notification permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (!isFullscreenReaderActive) {
                FloatingPillNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) { innerPadding ->
        AuroraBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // High-contrast in-app controller banner for active Adhan
                InAppAdhanBanner(
                    visible = isAdhanPlaying,
                    prayerName = currentAdhanPrayer ?: "الصلاة",
                    onStop = { viewModel.stopAdhan() },
                    onSnooze = { viewModel.snoozeAdhan() }
                )

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Main Tab Switcher
                    when (currentTab) {
                        NavigationTab.GUARDIAN -> {
                            DashboardScreen(
                                prayerLog = todayPrayerLog,
                                sobrietyLog = sobrietyLog,
                                muhasabahHistory = muhasabahHistory,
                                isDarkMode = isDarkMode,
                                onToggleTheme = onToggleTheme,
                                onTogglePrayer = { name, done -> viewModel.togglePrayer(name, done) },
                                onToggleWorkout = { done -> viewModel.toggleWorkout(done) },
                                onToggleColdShower = { done -> viewModel.toggleColdShower(done) },
                                onRecordRelapse = { viewModel.recordRelapse() },
                                onTriggerPanic = { viewModel.setPanicSheetVisible(true) },
                                onTestAlarm = { viewModel.testEmergencyAlarm() },
                                onOpenSettings = { currentTab = NavigationTab.AOD }
                            )
                        }

                        NavigationTab.QURAN -> {
                            QuranScreen(viewModel = quranViewModel)
                        }

                        NavigationTab.ADHKAR -> {
                            AdhkarScreen(
                                dhikrList = allDhikr,
                                selectedCategory = uiState.selectedDhikrCategory,
                                onCategorySelected = { viewModel.setSelectedDhikrCategory(it) },
                                onIncrementDhikr = { viewModel.incrementDhikr(it) },
                                onResetAll = { viewModel.resetAllDhikr() }
                            )
                        }

                        NavigationTab.RECOVERY -> {
                            RecoveryScreen(
                                sobrietyLog = sobrietyLog,
                                onToggleWorkout = { viewModel.toggleWorkout(it) },
                                onToggleColdShower = { viewModel.toggleColdShower(it) },
                                onRecordRelapse = { viewModel.recordRelapse() },
                                onTriggerPanic = { viewModel.setPanicSheetVisible(true) }
                            )
                        }

                        NavigationTab.AOD -> {
                            AodSettingsScreen(
                                currentTheme = uiState.currentTheme,
                                onThemeChanged = { viewModel.setAppTheme(it) },
                                isAodEnabled = uiState.isAodEnabled,
                                onToggleAod = { viewModel.setAodEnabled(it) },
                                isBurnInProtectionEnabled = uiState.isBurnInProtectionEnabled,
                                onToggleBurnIn = { viewModel.setBurnInProtectionEnabled(it) },
                                brightness = uiState.aodBrightness,
                                onBrightnessChanged = { viewModel.setAodBrightness(it) },
                                isAutoBrightnessEnabled = uiState.isAutoBrightnessEnabled,
                                onToggleAutoBrightness = { viewModel.setAutoBrightnessEnabled(it) },
                                aodDisplayMode = uiState.aodDisplayMode,
                                onDisplayModeChanged = { viewModel.setAodDisplayMode(it) },
                                isBatterySaverCutoffEnabled = uiState.isBatterySaverCutoffEnabled,
                                onToggleBatterySaverCutoff = { viewModel.setBatterySaverCutoffEnabled(it) },
                                misbahaHapticProfile = uiState.misbahaHapticProfile,
                                onHapticProfileChanged = { viewModel.setMisbahaHapticProfile(it) },
                                isPocketModeEnabled = uiState.isPocketModeEnabled,
                                onTogglePocketMode = { viewModel.setPocketModeEnabled(it) },
                                aodClockStyle = uiState.aodClockStyle,
                                onClockStyleChanged = { viewModel.setAodClockStyle(it) },
                                aodAccentColor = uiState.aodAccentColor,
                                onAccentColorChanged = { viewModel.setAodAccentColor(it) },
                                aodBatteryStyle = uiState.aodBatteryStyle,
                                onBatteryStyleChanged = { viewModel.setAodBatteryStyle(it) },
                                isNotificationPrivacy = uiState.isNotificationPrivacy,
                                onToggleNotificationPrivacy = { viewModel.setNotificationPrivacy(it) },
                                isFloatingDhikrEnabled = uiState.isFloatingDhikrEnabled,
                                onToggleFloatingDhikr = { viewModel.setFloatingDhikrEnabled(it) },
                                onTestAlarm = { viewModel.testEmergencyAlarm() },
                                onReplayOnboarding = { viewModel.replayOnboarding() }
                            )
                        }

                        NavigationTab.LEADERBOARD -> {
                            LeaderboardScreen(
                                sobrietyLog = sobrietyLog ?: com.example.data.model.SobrietyLog(),
                                todayPrayerLog = todayPrayerLog,
                                allDhikr = allDhikr
                            )
                        }
                    }
                }
            }

            // Emergency Panic Craving Overlay Sheet
            AnimatedVisibility(
                visible = uiState.showPanicSheet,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                PanicSheet(
                    onDismiss = { viewModel.setPanicSheetVisible(false) },
                    onIncrementIstighfar = { viewModel.incrementIstighfar() }
                )
            }

            // First-Time Onboarding Experience Dialog
            AnimatedVisibility(
                visible = uiState.showOnboardingDialog,
                enter = fadeIn(tween(350)) + slideInVertically(initialOffsetY = { 60 }),
                exit = fadeOut(tween(250)) + slideOutVertically(targetOffsetY = { 60 })
            ) {
                OnboardingDialog(
                    onDismiss = { viewModel.completeOnboarding() },
                    onComplete = { viewModel.completeOnboarding() }
                )
            }
        }
    }
}
