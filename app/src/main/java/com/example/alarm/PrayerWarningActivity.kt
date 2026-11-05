package com.example.alarm

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeMedium
import com.example.ui.theme.AlMuhasibTheme
import com.example.ui.theme.AppleAmber
import com.example.ui.theme.AppleCrimson
import com.example.ui.theme.AppleGold
import com.example.ui.theme.AppleGoldBright
import com.example.util.HapticFeedbackUtil
import kotlinx.coroutines.delay
import java.lang.ref.WeakReference

class PrayerWarningActivity : ComponentActivity() {

    private var vibrator: Vibrator? = null
    private var autoDismissJob: Job? = null

    companion object {
        private var activeInstance: WeakReference<PrayerWarningActivity>? = null

        fun dismissIfActive() {
            activeInstance?.get()?.let { activity ->
                activity.stopAlarm()
                activity.finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeInstance = WeakReference(this)

        // Safety: Auto-terminate emergency alarm and vibrator after 90 seconds to prevent infinite screen-on and motor battery drain
        autoDismissJob = lifecycleScope.launch {
            delay(90_000L)
            stopAlarm()
            finish()
        }

        onBackPressedDispatcher.addCallback(this) {
            // Prevent accidental dismissal by back press; must use action buttons
        }

        configureEmergencyWindow()

        val rawPrayerName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME) ?: "الصلاة"
        val isSnooze = intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_IS_SNOOZE, false)
        val prayerArabic = when (rawPrayerName) {
            "Fajr" -> "الفجر"
            "Dhuhr" -> "الظهر"
            "Asr" -> "العصر"
            "Maghrib" -> "المغرب"
            "Isha" -> "العشاء"
            else -> rawPrayerName
        }
        val minutesLeft = intent.getIntExtra(PrayerAlarmScheduler.EXTRA_MINUTES_LEFT, 0)

        startContinuousAlarmAudioAndVibration(prayerArabic)

        setContent {
            AlMuhasibTheme(darkTheme = true) {
                PrayerWarningContent(
                    prayerName = prayerArabic,
                    minutesLeft = minutesLeft,
                    isSnooze = isSnooze,
                    onSnooze = {
                        stopAlarm()
                        PrayerAlarmScheduler.scheduleSnoozeAlarm(this@PrayerWarningActivity, prayerArabic)
                        finish()
                    },
                    onVerifiedDismiss = {
                        stopAlarm()
                        finish()
                    }
                )
            }
        }
    }

    private fun configureEmergencyWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    private fun startContinuousAlarmAudioAndVibration(prayerName: String = "الصلاة") {
        try {
            // ponytail: avoid duplicate audio playback; leverage centralized PrayerAdhanPlayer
            if (!PrayerAdhanPlayer.isPlaying.value) {
                PrayerAdhanPlayer.play(this, prayerName)
            }

            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 800, 300, 800, 300, 1000)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
            } else {
                vibrator?.vibrate(longArrayOf(0, 800, 300, 800, 300, 1000), 0)
            }
        } catch (e: Exception) {
            // Ignore if sound hardware fails
        }
    }

    private fun stopAlarm() {
        try {
            autoDismissJob?.cancel()
            autoDismissJob = null
            PrayerAdhanPlayer.stop()
            vibrator?.cancel()
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(PrayerAlarmReceiver.NOTIFICATION_ID_PRAYER)
            notificationManager?.cancel(PrayerAlarmReceiver.NOTIFICATION_ID_PRAYER_WARN)
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onDestroy() {
        if (activeInstance?.get() == this) {
            activeInstance = null
        }
        stopAlarm()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent accidental dismissal; dispatcher callback absorbs event
        super.onBackPressed()
    }
}

@Composable
fun PrayerWarningContent(
    prayerName: String,
    minutesLeft: Int,
    isSnooze: Boolean = false,
    onSnooze: () -> Unit = {},
    onVerifiedDismiss: () -> Unit
) {
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF3B0007),
        targetValue = Color(0xFF1F0005),
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color_pulse"
    )

    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            while (holdProgress < 1f) {
                delay(50)
                holdProgress += 0.05f
                HapticFeedbackUtil.performLightTick(context)
            }
            HapticFeedbackUtil.performCompletionRumble(context)
            onVerifiedDismiss()
        } else {
            holdProgress = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        pulseColor,
                        Color(0xFF12141A),
                        Color(0xFF0E0F12)
                    )
                )
            )
            .padding(horizontal = 22.dp, vertical = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // HEADER: Emergency Crimson Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FF3B30))
                        .border(2.dp, AppleCrimson, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "تحذير الوعيد",
                        tint = AppleCrimson,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = "إِنْذَارُ الوَعِيدِ الحَاسِمِ",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppleCrimson
                    )
                )

                Text(
                    text = "وقت صلاة $prayerName يوشك على الانتهاء!",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "متبقي أقل من $minutesLeft دقيقة فقط قبل خروج الوقت وفوات الفريضة.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = Color(0xFFCBD5E1),
                        textAlign = TextAlign.Center
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AUTHENTIC HADITH & QURANIC WARNINGS
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Warning 1: Sunan al-Tirmidhi
                AppleGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = SquircleShapeMedium,
                    elevation = 4.dp,
                    accentGlow = AppleCrimson
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "« الْعَهْدُ الَّذِي بَيْنَنَا وَبَيْنَهُمُ الصَّلَاةُ فَمَنْ تَرَكَهَا فَقَدْ كَفَرَ »",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AppleGoldBright,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "رواه الترمذي (2621) وصححه الألباني",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Warning 2: Surah Al-Muddaththir
                AppleGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = SquircleShapeMedium,
                    elevation = 4.dp,
                    accentGlow = AppleCrimson
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "« مَا سَلَكَكُمْ فِي سَقَرَ • قَالُوا لَمْ نَكُ مِنَ الْمُصَلِّينَ »",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AppleGoldBright,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "سورة المدثر (42 - 43)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // VERIFICATION TASK & DISMISSAL (HOLD TO CONFIRM WUDU)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Tactile, elegant snooze button (dark green / crimson neumorphic)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(PillShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0E1E16), // Dark green neumorphic surface
                                    Color(0xFF1F1017)  // Deep crimson subtle undertone
                                )
                            )
                        )
                        .border(
                            1.2.dp,
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0x6610B981), // Emerald edge glow
                                    Color(0x66EF4444)  // Crimson edge glow
                                )
                            ),
                            PillShape
                        )
                        .clickable {
                            HapticFeedbackUtil.performLightTick(context)
                            onSnooze()
                        }
                        .testTag("snooze_10_min_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Alarm,
                            contentDescription = "تذكير بعد 10 دقائق ⏱️",
                            tint = AppleGoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "تذكير بعد 10 دقائق ⏱️",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        )
                    }
                }

                Text(
                    text = "لا يمكن إيقاف الإنذار إلا بالقسم على الوضوء والقيام",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppleGold
                    )
                )

                // Hold Button Container with Live Fill Progress
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(PillShape)
                        .background(Color(0xFF16181D))
                        .border(1.5.dp, if (isHolding) AppleGoldBright else AppleCrimson, PillShape)
                        .testTag("hold_to_confirm_wudu")
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isHolding = true
                                    tryAwaitRelease()
                                    isHolding = false
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Progress fill
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AppleGoldBright.copy(alpha = 0.85f), AppleAmber.copy(alpha = 0.85f))
                                )
                            )
                            .clip(PillShape)
                            .fillMaxWidth(fraction = holdProgress)
                            .align(Alignment.CenterStart)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "تأكيد",
                            tint = if (holdProgress > 0.5f) Color.Black else AppleCrimson,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isHolding) {
                                "جاري التثبت... ${(holdProgress * 100).toInt()}%"
                            } else {
                                "اضغط مطولاً 3 ثوانٍ: عاهدتُ الله أني قائمٌ للصلاة"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (holdProgress > 0.5f) Color.Black else Color.White
                            )
                        )
                    }
                }

                Text(
                    text = "استمر بالضغط لتأكيد العهد أمام الله وإسكات التنبيه",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
