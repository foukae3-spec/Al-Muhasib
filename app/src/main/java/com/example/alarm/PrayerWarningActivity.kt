package com.example.alarm

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.MainActivity
import com.example.util.HapticFeedbackUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * PrayerWarningActivity — Full-Screen Salah Convergence & Emergency Prayer Warning Activity.
 *
 * Completes the interrupted harmonization task in AGENT_HANDOVER.md (lines 240–450):
 * 1. Displays over the lock screen (setShowWhenLocked(true), setTurnScreenOn(true)).
 * 2. Harmonizes the 10-minute snooze button (ACTION_SNOOZE_10_MIN) directly with
 *    PrayerAlarmReceiver and stops PrayerAdhanPlayer cleanly without race conditions.
 * 3. Integrates with the Whole-Phone Salah Convergence state so tapping "Entering Salah"
 *    marks the prayer completed in MainActivity.
 * 4. Incorporates a 90-second auto-dismiss timeout to preserve AMOLED battery and prevent motor drain.
 */
class PrayerWarningActivity : ComponentActivity() {

    private var autoDismissJob: Job? = null

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_ARABIC = "extra_prayer_arabic"
        const val EXTRA_IS_SNOOZE_REMINDER = "extra_is_snooze_reminder"
        const val ACTION_SNOOZE_10_MIN = "com.example.alarm.ACTION_SNOOZE_10_MIN"
        const val ACTION_DISMISS_PRAYER_WARNING = "com.example.alarm.ACTION_DISMISS_PRAYER_WARNING"
        const val NOTIFICATION_ID_PRAYER_ALARM = 4010
        private const val SNOOZE_DURATION_MS = 10 * 60 * 1000L

        private var activeInstance: WeakReference<PrayerWarningActivity>? = null

        fun dismissIfActive() {
            activeInstance?.get()?.let { activity ->
                activity.stopAdhanPlaybackSafely()
                activity.finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeInstance = WeakReference(this)
        configureLockScreenWindow()

        // Battery Optimization: Auto-dismiss after 90 seconds to prevent screen-on drain if unattended
        autoDismissJob = lifecycleScope.launch {
            delay(90_000L)
            stopAdhanPlaybackSafely()
            finish()
        }

        onBackPressedDispatcher.addCallback(this) {
            // Prevent accidental dismissal by back press; must use action buttons
        }

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "Salah"
        val prayerArabic = intent?.getStringExtra(EXTRA_PRAYER_ARABIC) ?: resolveArabicPrayerName(prayerName)
        val isSnoozeReminder = intent?.getBooleanExtra(EXTRA_IS_SNOOZE_REMINDER, false) ?: false

        setContent {
            PrayerConvergenceFullScreen(
                prayerName = prayerName,
                prayerArabic = prayerArabic,
                isSnoozeReminder = isSnoozeReminder,
                onSilenceAdhanOnly = {
                    HapticFeedbackUtil.performTick(this)
                    stopAdhanPlaybackSafely()
                },
                onPrayNowAndSeal = {
                    HapticFeedbackUtil.performHeavyClick(this)
                    stopAdhanPlaybackSafely()
                    cancelActivePrayerNotification()
                    markPrayerCompletedAndReturnHome(prayerName)
                },
                onSnoozeTenMinutes = {
                    HapticFeedbackUtil.performClick(this)
                    triggerHarmonizedTenMinuteSnooze(prayerName, prayerArabic)
                },
                onDismissWarning = {
                    HapticFeedbackUtil.performClick(this)
                    stopAdhanPlaybackSafely()
                    cancelActivePrayerNotification()
                    finish()
                }
            )
        }
    }

    override fun onDestroy() {
        autoDismissJob?.cancel()
        if (activeInstance?.get() === this) {
            activeInstance = null
        }
        super.onDestroy()
    }

    private fun configureLockScreenWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    /**
     * Stops PrayerAdhanPlayer cleanly and immediately.
     */
    private fun stopAdhanPlaybackSafely() {
        runCatching {
            PrayerAdhanPlayer.stop()
        }
    }

    private fun cancelActivePrayerNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.cancel(NOTIFICATION_ID_PRAYER_ALARM)
        nm?.cancel(PrayerAlarmReceiver.NOTIFICATION_ID_PRAYER)
        nm?.cancel(PrayerAlarmReceiver.NOTIFICATION_ID_PRE_ADHAN)
    }

    /**
     * Harmonized 10-Minute Snooze (Resolves AGENT_HANDOVER.md lines 240-450):
     * 1. Stops Adhan audio immediately.
     * 2. Dispatches broadcast ACTION_SNOOZE_10_MIN to PrayerAlarmReceiver.
     * 3. Guarantees top-priority AlarmClockInfo exact wake-up +10 minutes via PrayerAlarmScheduler.scheduleSnoozeAlarm.
     * 4. Cancels active notifications and finishes the Activity.
     */
    private fun triggerHarmonizedTenMinuteSnooze(prayerName: String, prayerArabic: String) {
        stopAdhanPlaybackSafely()
        cancelActivePrayerNotification()

        // 1. Broadcast to PrayerAlarmReceiver so notification state is updated and handled
        val receiverIntent = Intent(this, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmScheduler.ACTION_SNOOZE_10_MIN
            putExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_PRAYER_ARABIC, prayerArabic)
            putExtra(EXTRA_IS_SNOOZE_REMINDER, true)
        }
        runCatching { sendBroadcast(receiverIntent) }

        // 2. Direct exact schedule via PrayerAlarmScheduler (uses AlarmClock API, requestCode 1003)
        PrayerAlarmScheduler.scheduleSnoozeAlarm(this, prayerName)

        finish()
    }

    private fun markPrayerCompletedAndReturnHome(prayerName: String) {
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("extra_mark_prayer_completed", prayerName)
        }
        runCatching { startActivity(homeIntent) }
        finish()
    }

    private fun resolveArabicPrayerName(prayerName: String): String = when (prayerName.lowercase()) {
        "fajr" -> "صَلَاةُ الفَجْر"
        "dhuhr", "zuhr" -> "صَلَاةُ الظُّهْر"
        "asr" -> "صَلَاةُ العَصْر"
        "maghrib" -> "صَلَاةُ المَغْرِب"
        "isha" -> "صَلَاةُ العِشَاء"
        else -> "نداءُ الصَّلَاة"
    }
}

@Composable
private fun PrayerConvergenceFullScreen(
    prayerName: String,
    prayerArabic: String,
    isSnoozeReminder: Boolean,
    onSilenceAdhanOnly: () -> Unit,
    onPrayNowAndSeal: () -> Unit,
    onSnoozeTenMinutes: () -> Unit,
    onDismissWarning: () -> Unit
) {
    var adhanSilenced by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            secondsElapsed++
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val sacredEmerald = Color(0xFFE5C185) // Heavenly Champagne Gold accent
    val sacredGold = Color(0xFFF3DEC0)
    val deepCanvas = Color(0xFF07090F)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF141926),
                        deepCanvas,
                        Color(0xFF04060A)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Pill
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = sacredEmerald.copy(alpha = 0.16f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    sacredEmerald.copy(alpha = 0.35f)
                ),
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Text(
                    text = if (isSnoozeReminder) "تَذْكِيرُ الصَّلَاة • SNOOZE 10M EXPIRED" else "نِدَاءُ المِحْرَاب • MIHRAB SANCTUARY",
                    color = sacredGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Central Sacred Arch Orb
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.scale(pulseScale)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(190.dp)
                        .border(1.5.dp, sacredEmerald.copy(alpha = 0.45f), CircleShape)
                        .background(sacredEmerald.copy(alpha = 0.08f), CircleShape)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = prayerArabic,
                            color = Color(0xFFFDFBF7),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = prayerName.uppercase(),
                            color = sacredGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "+%02d:%02d".format(secondsElapsed / 60, secondsElapsed % 60),
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Text(
                    text = "﴿ إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَّوْقُوتًا ﴾",
                    color = sacredGold.copy(alpha = 0.95f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = "Set down worldly tasks, perform Wudu, and fulfill your covenant. Distraction apps remain resting.",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Action Docks (Apple HIG compliant)
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color(0x2BFFFFFF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Primary Action: Enter Salah Now
                    Button(
                        onClick = onPrayNowAndSeal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = sacredEmerald,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Entering $prayerName Now (Mark Completed)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Secondary Row: Snooze 10 Min + Silence Adhan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSnoozeTenMinutes,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                sacredGold.copy(alpha = 0.6f)
                            )
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = sacredGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Snooze 10m",
                                color = sacredGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                adhanSilenced = true
                                onSilenceAdhanOnly()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.3f)
                            )
                        ) {
                            Icon(
                                if (adhanSilenced) Icons.Default.NotificationsOff else Icons.Default.VolumeOff,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (adhanSilenced) "Adhan Muted" else "Mute Adhan",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Subtle Return to Mihrab Home link
                    OutlinedButton(
                        onClick = onDismissWarning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.14f)
                        )
                    ) {
                        Text(
                            text = "Return to Mihrab Home (Keep Salah Lock Active)",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
