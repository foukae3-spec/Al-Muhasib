package com.example

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarm.PrayerAdhanPlayer
import com.example.alarm.PrayerAlarmScheduler
import com.example.alarm.PrayerWarningActivity
import com.example.aod.AodActivity
import com.example.data.model.DhikrCategory
import com.example.ui.components.InAppAdhanBanner
import com.example.ui.screens.AdhkarScreen
import com.example.ui.screens.AodSettingsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MihrabHomeLauncherScreen
import com.example.ui.screens.OnboardingDialog
import com.example.ui.screens.PanicSheet
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.RecoveryScreen
import com.example.ui.screens.SalehReformHubScreen
import com.example.ui.theme.MuhasibSalehTheme
import com.example.ui.theme.SacredAccentPalette
import com.example.ui.viewmodel.MuhasibViewModel
import com.example.ui.viewmodel.QuranViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.IslamicCalendarUtil
import com.example.util.QuotePreferences
import java.time.Duration
import java.time.LocalTime

enum class SovereignOsDestination(
    val title: String,
    val arabicLabel: String,
    val icon: ImageVector
) {
    MIHRAB_HOME("Sanctuary", "المِحْرَاب", Icons.Default.Home),
    QURAN_WIRD("Quran", "القُرْآن", Icons.Default.MenuBook),
    ADHKAR_HISN("Adhkar", "الأَذْكَار", Icons.Default.SelfImprovement),
    SALEH_REFORM("Library", "صَالِح", Icons.Default.AutoStories),
    RECOVERY_GUARD("Serenity", "الحِصْن", Icons.Default.Shield),
    CLASSIC_DASHBOARD("Ledger", "الإِحْصَاء", Icons.Default.Dashboard),
    OS_SETTINGS("Settings", "الإِعْدَادَات", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MuhasibViewModel by viewModels()
    private var expiredNiyyahAppName by mutableStateOf<String?>(null)
    private var currentDestination by mutableStateOf(SovereignOsDestination.MIHRAB_HOME)

    private val homeRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* Role selection handled by Android OS */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIncomingIntentExtras(intent)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentDestination != SovereignOsDestination.MIHRAB_HOME) {
                    currentDestination = SovereignOsDestination.MIHRAB_HOME
                }
            }
        })

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val todayPrayerLog by viewModel.todayPrayerLog.collectAsStateWithLifecycle()
            val sobrietyLog by viewModel.sobrietyState.collectAsStateWithLifecycle()
            val allDhikr by viewModel.allDhikr.collectAsStateWithLifecycle()
            val dailyMissions by viewModel.dailyMissions.collectAsStateWithLifecycle()
            val isAdhanPlaying by viewModel.isAdhanPlaying.collectAsStateWithLifecycle()
            val currentAdhanPrayer by viewModel.currentAdhanPrayer.collectAsStateWithLifecycle()
            val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()

            val quranViewModel: QuranViewModel = viewModel()
            val sacredPalette = SacredAccentPalette.fromAodAccent(uiState.aodAccentColor)

            val prayerSchedule = remember { IslamicCalendarUtil.getDailyPrayerSchedule() }
            val nextPrayer = prayerSchedule.firstOrNull { it.isNext } ?: prayerSchedule.firstOrNull()
            val nextPrayerName = nextPrayer?.key ?: "Asr"
            val nextPrayerTimeFormatted = nextPrayer?.timeFormatted ?: "15:42"
            val minutesToNextPrayer = remember(nextPrayer) {
                nextPrayer?.let {
                    val now = LocalTime.now()
                    val diff = Duration.between(now, it.time).toMinutes()
                    if (diff < 0) (diff + 1440).toInt() else diff.toInt()
                } ?: 24
            }
            val hijriDateString = remember { IslamicCalendarUtil.getFormattedHijriDate() }
            val istighfarTodayCount = sobrietyLog.istighfarTodayCount
            val cleanStreakDays = sobrietyLog.cleanDaysCount
            val activeBook = allBooks.firstOrNull { it.id == QuotePreferences.getActiveBookId(this@MainActivity) } ?: allBooks.firstOrNull()
            val activeBookTitle = activeBook?.title ?: "الداء والدواء (ابن القيم)"
            val currentBookChunk = (activeBook?.currentChunkCursor ?: 0) + 1

            val persistedPrayers = remember(todayPrayerLog) {
                listOf(
                    "Fajr" to todayPrayerLog.fajrDone,
                    "Dhuhr" to todayPrayerLog.dhuhrDone,
                    "Asr" to todayPrayerLog.asrDone,
                    "Maghrib" to todayPrayerLog.maghribDone,
                    "Isha" to todayPrayerLog.ishaDone
                )
            }

            MuhasibSalehTheme(
                themeMode = uiState.muhasibThemeMode,
                palette = sacredPalette
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0A0D14))
                ) {
                    when (currentDestination) {
                        SovereignOsDestination.MIHRAB_HOME -> {
                            MihrabHomeLauncherScreen(
                                nextPrayerName = nextPrayerName,
                                nextPrayerTimeFormatted = nextPrayerTimeFormatted,
                                minutesToNextPrayer = minutesToNextPrayer,
                                hijriDateString = hijriDateString,
                                cleanDaysStreak = cleanStreakDays,
                                istighfarTodayCount = istighfarTodayCount,
                                activeBookTitle = activeBookTitle,
                                currentBookChunk = currentBookChunk,
                                persistedPrayers = persistedPrayers,
                                persistedMissions = dailyMissions,
                                onToggleMissionPersisted = { id, done -> viewModel.toggleDailyMission(id, done) },
                                onAddMissionPersisted = { title, anchor, mins -> viewModel.addDailyMission(title, anchor, mins) },
                                onRecordNiyyahSessionPersisted = { pkg, label, niyyah, mins -> viewModel.recordNiyyahSession(pkg, label, niyyah, mins) },
                                expiredNiyyahAppAlert = expiredNiyyahAppName,
                                onDismissExpiredAlert = { expiredNiyyahAppName = null },
                                onOpenQuran = { currentDestination = SovereignOsDestination.QURAN_WIRD },
                                onOpenAdhkar = { currentDestination = SovereignOsDestination.ADHKAR_HISN },
                                onOpenSalehBooks = { currentDestination = SovereignOsDestination.SALEH_REFORM },
                                onOpenPanicShield = { viewModel.setPanicSheetVisible(true) },
                                onLaunchAodSleepMode = { launchCustomOledAod() },
                                onPrayerCompletedToggle = { prayer, done ->
                                    viewModel.togglePrayer(prayer, done)
                                },
                                onIncrementIstighfar = {
                                    viewModel.incrementIstighfar()
                                }
                            )
                        }
                        SovereignOsDestination.QURAN_WIRD -> {
                            QuranScreen(viewModel = quranViewModel)
                        }
                        SovereignOsDestination.ADHKAR_HISN -> {
                            AdhkarScreen(viewModel = viewModel)
                        }
                        SovereignOsDestination.SALEH_REFORM -> {
                            SalehReformHubScreen(viewModel = viewModel)
                        }
                        SovereignOsDestination.RECOVERY_GUARD -> {
                            RecoveryScreen(viewModel = viewModel)
                        }
                        SovereignOsDestination.CLASSIC_DASHBOARD -> {
                            DashboardScreen(viewModel = viewModel)
                        }
                        SovereignOsDestination.OS_SETTINGS -> {
                            AodSettingsScreen(viewModel = viewModel)
                        }
                    }

                    // In-App Adhan Banner
                    InAppAdhanBanner(
                        visible = isAdhanPlaying,
                        prayerName = currentAdhanPrayer ?: "الصلاة",
                        onStop = { PrayerAdhanPlayer.stop() },
                        onSnooze = {
                            PrayerAlarmScheduler.scheduleSnoozeAlarm(this@MainActivity, currentAdhanPrayer ?: "الصلاة")
                            PrayerAdhanPlayer.stop()
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    if (uiState.showPanicSheet) {
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

                    HeavenlyLiquidGlassDock(
                        currentDestination = currentDestination,
                        onSelectDestination = { destination ->
                            HapticFeedbackUtil.performTick(this@MainActivity)
                            currentDestination = destination
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntentExtras(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) {
            currentDestination = SovereignOsDestination.MIHRAB_HOME
        }
    }

    private fun handleIncomingIntentExtras(intent: Intent?) {
        if (intent == null) return
        val completedPrayer = intent.getStringExtra("extra_mark_prayer_completed")
        if (!completedPrayer.isNullOrBlank()) {
            viewModel.togglePrayer(completedPrayer, true)
            currentDestination = SovereignOsDestination.MIHRAB_HOME
        }
        val expiredApp = intent.getStringExtra("extra_niyyah_expired_app")
        if (!expiredApp.isNullOrBlank()) {
            expiredNiyyahAppName = expiredApp
            currentDestination = SovereignOsDestination.MIHRAB_HOME
        }
        if (intent.getBooleanExtra("EXTRA_TRIGGER_PANIC", false)) {
            viewModel.setPanicSheetVisible(true)
        }
    }

    private fun launchCustomOledAod() {
        val aodIntent = Intent(this, AodActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        runCatching { startActivity(aodIntent) }
    }

    fun requestDefaultHomeLauncherRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME) && !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                homeRoleLauncher.launch(intent)
            }
        }
    }
}

@Composable
private fun HeavenlyLiquidGlassDock(
    currentDestination: SovereignOsDestination,
    onSelectDestination: (SovereignOsDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val champagne = Color(0xFFE5C185)
    val alabaster = Color(0xFFFBF9F5)
    val dockShape = RoundedCornerShape(32.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 28.dp,
                shape = dockShape,
                ambientColor = Color.Black,
                spotColor = champagne.copy(alpha = 0.35f)
            ),
        shape = dockShape,
        color = Color(0xD9101522),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.42f),
                    champagne.copy(alpha = 0.28f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SovereignOsDestination.entries.forEach { dest ->
                val isSelected = currentDestination == dest
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (isSelected) champagne.copy(alpha = 0.22f) else Color.Transparent,
                    border = if (isSelected) BorderStroke(0.8.dp, champagne.copy(alpha = 0.55f)) else null,
                    modifier = Modifier.clickable { onSelectDestination(dest) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = if (isSelected) 8.dp else 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = dest.icon,
                            contentDescription = dest.title,
                            tint = if (isSelected) champagne else alabaster.copy(alpha = 0.55f),
                            modifier = Modifier.size(18.dp)
                        )
                        if (isSelected) {
                            Text(
                                text = " ${dest.title}",
                                color = alabaster,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
