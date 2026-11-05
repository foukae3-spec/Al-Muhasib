package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.aod.AodManager
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
import android.media.MediaPlayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MuhasibApplication
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeLarge
import com.example.ui.components.SquircleShapeMedium
import com.example.ui.theme.AppleCrimson
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.SpiritualAmber
import com.example.ui.theme.SpiritualCyan
import com.example.ui.theme.SpiritualCyanBright
import com.example.ui.theme.SpiritualEmerald
import com.example.ui.theme.SpiritualEmeraldBright
import com.example.util.HapticFeedbackUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BreathingPhase(val phaseNameArabic: String, val seconds: Int, val targetScale: Float) {
    INHALE("شَهِيقٌ عَمِيق (4 ثوانٍ)", 4, 1.4f),
    HOLD("احْبِسِ النَّفَس (7 ثوانٍ)", 7, 1.4f),
    EXHALE("زَفِيرٌ مُمْتَدٌّ (8 ثوانٍ)", 8, 0.8f)
}

@Composable
fun PanicSheet(
    onDismiss: () -> Unit,
    onIncrementIstighfar: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current

    var currentPhase by remember { mutableStateOf(BreathingPhase.INHALE) }
    var secondsRemaining by remember { mutableIntStateOf(4) }
    val ringScale = remember { Animatable(1f) }

    // ponytail: 15-minute urge-surfing timer (900 seconds)
    var urgeSecondsRemaining by remember { mutableIntStateOf(15 * 60) }
    var isUrgeTimerRunning by remember { mutableStateOf(true) }

    // ponytail: tactile grounding Istighfar session counter
    var sessionIstighfarCount by remember { mutableIntStateOf(0) }

    // ponytail: authentic Ayat al-Kursi recitation audio anchor
    var isAudioPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.let {
                    if (it.isPlaying) it.stop()
                    it.release()
                }
            } catch (e: Exception) {
                // Ignore
            }
            mediaPlayer = null
        }
    }

    LaunchedEffect(isUrgeTimerRunning) {
        while (isUrgeTimerRunning && urgeSecondsRemaining > 0) {
            delay(1000)
            urgeSecondsRemaining--
        }
        if (urgeSecondsRemaining <= 0) {
            isUrgeTimerRunning = false
            HapticFeedbackUtil.performCompletionRumble(context)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentPhase = BreathingPhase.INHALE
            secondsRemaining = 4
            HapticFeedbackUtil.performLightTick(context)
            ringScale.animateTo(
                targetValue = 1.4f,
                animationSpec = tween(4000, easing = LinearEasing)
            )

            currentPhase = BreathingPhase.HOLD
            secondsRemaining = 7
            HapticFeedbackUtil.performLightTick(context)
            delay(7000)

            currentPhase = BreathingPhase.EXHALE
            secondsRemaining = 8
            HapticFeedbackUtil.performLightTick(context)
            ringScale.animateTo(
                targetValue = 0.8f,
                animationSpec = tween(8000, easing = LinearEasing)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.86f))
            .clickable(onClick = onDismiss)
            .padding(top = 40.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SquircleShapeLarge)
                .background(if (appColors.isDark) Color(0xFF0E1E16) else Color.White)
                .border(1.dp, Color(0x3310B981), SquircleShapeLarge)
                .clickable(enabled = false) {}
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Drag handle & close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "استعاذة وطوارئ",
                            tint = AppleCrimson,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "بِرُوتُوكُولُ النَّجْدَةِ وَقَمْعِ الشَّهْوَةِ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppleCrimson
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(appColors.glassHighlight)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "إغلاق",
                            tint = appColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Parasympathetic Breathing Ring
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(SpiritualAmber.copy(alpha = 0.12f))
                        .border(2.dp, SpiritualAmber.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .graphicsLayer(scaleX = ringScale.value, scaleY = ringScale.value)
                            .clip(CircleShape)
                            .background(SpiritualAmber.copy(alpha = 0.25f))
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.R.drawable.ic_sujood),
                            contentDescription = "سجود وسكينة",
                            tint = SpiritualAmber,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentPhase.phaseNameArabic,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = "تهدئة الجهاز العصبي الودي",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = appColors.textSecondary
                            )
                        )
                    }
                }

                // ponytail: 15-minute urge-surfing countdown to ground craving spikes
                AppleGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = SquircleShapeMedium,
                    elevation = 3.dp,
                    accentGlow = SpiritualCyan
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Timer,
                                    contentDescription = "مؤقت ركوب الموجة",
                                    tint = SpiritualCyanBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "تِقْنِيَّةُ رُكُوبِ المَوْجَةِ (15 دقيقة)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.textPrimary
                                    )
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        HapticFeedbackUtil.performLightTick(context)
                                        if (urgeSecondsRemaining <= 0) {
                                            urgeSecondsRemaining = 15 * 60
                                            isUrgeTimerRunning = true
                                        } else {
                                            isUrgeTimerRunning = !isUrgeTimerRunning
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isUrgeTimerRunning && urgeSecondsRemaining > 0) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = if (isUrgeTimerRunning && urgeSecondsRemaining > 0) "إيقاف مؤقت" else "استئناف",
                                        tint = SpiritualCyanBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        HapticFeedbackUtil.performLightTick(context)
                                        urgeSecondsRemaining = 15 * 60
                                        isUrgeTimerRunning = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "إعادة ضبط",
                                        tint = appColors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        val urgeMinutes = urgeSecondsRemaining / 60
                        val urgeSeconds = urgeSecondsRemaining % 60
                        val formattedTime = "%02d:%02d".format(urgeMinutes, urgeSeconds)

                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 42.sp,
                                color = if (urgeSecondsRemaining > 0) SpiritualCyanBright else SpiritualEmeraldBright
                            ),
                            modifier = Modifier.testTag("urge_timer_text")
                        )

                        // Duration Presets (3-Min Quick Wave Surfer & 15-Min Full)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(if (urgeSecondsRemaining in 1..180) SpiritualCyanBright.copy(alpha = 0.25f) else appColors.inset)
                                    .border(1.dp, if (urgeSecondsRemaining in 1..180) SpiritualCyanBright else Color(0x20FFFFFF), PillShape)
                                    .clickable {
                                        HapticFeedbackUtil.performLightTick(context)
                                        urgeSecondsRemaining = 180
                                        isUrgeTimerRunning = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "3 دقائق (موجة الاشتهاء السريع)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (urgeSecondsRemaining in 1..180) SpiritualCyanBright else appColors.textSecondary
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(if (urgeSecondsRemaining > 180) SpiritualCyanBright.copy(alpha = 0.25f) else appColors.inset)
                                    .border(1.dp, if (urgeSecondsRemaining > 180) SpiritualCyanBright else Color(0x20FFFFFF), PillShape)
                                    .clickable {
                                        HapticFeedbackUtil.performLightTick(context)
                                        urgeSecondsRemaining = 15 * 60
                                        isUrgeTimerRunning = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "15 دقيقة (الموجة الكاملة)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (urgeSecondsRemaining > 180) SpiritualCyanBright else appColors.textSecondary
                                    )
                                )
                            }
                        }

                        Text(
                            text = if (urgeSecondsRemaining > 0)
                                "نوبة الرغبة تشبه الموجة: ترتفع وتبلغ ذروتها ثم تخبو حتماً خلال 10-15 دقيقة دون الاستجابة لها."
                            else
                                "الحمد لله! انكسرت ذروة الموجة وتلاشت الرغبة بفضل الله.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Center,
                                color = appColors.textSecondary
                            )
                        )

                        // ponytail: soothing Ayat al-Kursi audio recitation anchor
                        Row(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(if (isAudioPlaying) SpiritualCyan.copy(alpha = 0.25f) else appColors.inset)
                                .border(1.dp, if (isAudioPlaying) SpiritualCyanBright else Color(0x20FFFFFF), PillShape)
                                .clickable {
                                    HapticFeedbackUtil.performLightTick(context)
                                    if (isAudioPlaying) {
                                        mediaPlayer?.pause()
                                        isAudioPlaying = false
                                    } else {
                                        if (mediaPlayer == null) {
                                            mediaPlayer = MediaPlayer.create(context, com.example.R.raw.ayat_al_kursi)?.apply {
                                                setOnCompletionListener { isAudioPlaying = false }
                                            }
                                        }
                                        mediaPlayer?.start()
                                        isAudioPlaying = true
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isAudioPlaying) Icons.Filled.Pause else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "استماع لآية الكرسي",
                                tint = if (isAudioPlaying) SpiritualCyanBright else appColors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isAudioPlaying) "إيقاف تلاوة آية الكرسي مؤقتاً ⏸️" else "تلاوة آية الكرسي المهدئة (صوت خاشع) 🔊",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAudioPlaying) SpiritualCyanBright else appColors.textPrimary
                                )
                            )
                        }
                    }
                }

                // Cold Water Shock Protocol Card (Vagus Nerve Reset)
                AppleGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = SquircleShapeMedium,
                    elevation = 3.dp,
                    accentGlow = SpiritualCyan
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SpiritualCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WaterDrop,
                                contentDescription = "ماء بارد",
                                tint = SpiritualCyanBright,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "بروتوكول صدمة الماء البارد (Vagus Nerve Reset)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpiritualCyanBright
                                )
                            )
                            Text(
                                text = "اغسل وجهك ومؤخرة رقبتك بماء بارد لمدة 30 ثانية لتفعيل منعكس الغطس، وخفض نبضات القلب وإخماد ثوران الدوبامين الزائف.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = appColors.textSecondary,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }

                // ponytail: tactile Istighfar counter for grounding during craving spikes
                AppleGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = SquircleShapeMedium,
                    elevation = 3.dp,
                    accentGlow = SpiritualEmerald
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TouchApp,
                                    contentDescription = "تسبيح تكتيلي",
                                    tint = SpiritualEmeraldBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "الاسْتِغْفَارُ التَّكْتِيلِي لِلتَّأْرِيضِ (Grounding)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.textPrimary
                                    )
                                )
                            }
                            if (sessionIstighfarCount > 0) {
                                IconButton(
                                    onClick = {
                                        HapticFeedbackUtil.performLightTick(context)
                                        sessionIstighfarCount = 0
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "إعادة تصفير العداد",
                                        tint = appColors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        var isCounterPressed by remember { mutableStateOf(false) }
                        val counterScale by animateFloatAsState(
                            targetValue = if (isCounterPressed) 0.92f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "panic_counter_scale"
                        )

                        // Tactile Clicker Circle with spring depression & mechanical haptic
                        Box(
                            modifier = Modifier
                                .size(115.dp)
                                .graphicsLayer {
                                    scaleX = counterScale
                                    scaleY = counterScale
                                }
                                .clip(CircleShape)
                                .background(Color(0xFF0D1E15))
                                .border(2.dp, SpiritualEmeraldBright.copy(alpha = 0.6f), CircleShape)
                                .pointerInput(sessionIstighfarCount) {
                                    detectTapGestures(
                                        onPress = {
                                            isCounterPressed = true
                                            tryAwaitRelease()
                                            isCounterPressed = false
                                        },
                                        onTap = {
                                            sessionIstighfarCount++
                                            val profile = AodManager.getMisbahaHapticProfile(context)
                                            HapticFeedbackUtil.performMisbahaFeedback(
                                                context = context,
                                                profile = profile,
                                                nextCount = sessionIstighfarCount,
                                                cycleTarget = 100
                                            )
                                            onIncrementIstighfar?.invoke() ?: run {
                                                try {
                                                    MuhasibApplication.instance.applicationScope.launch {
                                                        MuhasibApplication.instance.repository.incrementIstighfarCount()
                                                    }
                                                } catch (e: Exception) {
                                                    // Ignore
                                                }
                                            }
                                        }
                                    )
                                }
                                .testTag("urge_istighfar_counter"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$sessionIstighfarCount",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 32.sp,
                                        color = SpiritualEmeraldBright
                                    ),
                                    modifier = Modifier.testTag("urge_istighfar_count_text")
                                )
                                Text(
                                    text = "المِسْ لِلْعَدِّ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = appColors.textSecondary
                                    )
                                )
                            }
                        }

                        Text(
                            text = "«أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ وَأَتُوبُ إِلَيْهِ»",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                        )
                        Text(
                            text = "انقر على الدائرة مع كل استغفار لتفريغ شحنة الدوبامين وتثبيت التفكير بالنبضات الحسية",
                            style = MaterialTheme.typography.labelSmall.copy(
                                lineHeight = 15.sp,
                                textAlign = TextAlign.Center,
                                color = appColors.textSecondary
                            )
                        )
                    }
                }

                // ACTION STEP 1: Cold Wudu (الوضوء بالماء البارد)
                AppleGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = SquircleShapeMedium,
                    elevation = 3.dp,
                    accentGlow = SpiritualAmber
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SpiritualAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WaterDrop,
                                contentDescription = "وضوء",
                                tint = SpiritualAmber,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الخطوة 1: قم وتوضأ بماء بارد فوراً",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Text(
                                text = "اغسل وجهك وخلف عنقك بماء مثلج لتفعيل منعكس الغطس وقطع سريان الدم المثار",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    lineHeight = 16.sp,
                                    color = appColors.textSecondary
                                )
                            )
                        }
                    }
                }

                // ACTION STEP 2: Salat al-Tawbah (صلاة التوبة ركعتين)
                AppleGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = SquircleShapeMedium,
                    elevation = 3.dp,
                    accentGlow = SpiritualEmerald
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SpiritualEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mosque,
                                contentDescription = "صلاة التوبة",
                                tint = SpiritualEmeraldBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الخطوة 2: صَلِّ رَكْعَتَيْ تَوْبَةٍ لِلَّهِ",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Text(
                                text = "قال النبي ﷺ: «مَا مِنْ رَجُلٍ يُذْنِبُ ذَنْبًا فَيَتَوَضَّأُ فَيُصَلِّي رَكْعَتَيْنِ ثُمَّ يَسْتَغْفِرُ اللَّهَ إِلَّا غَفَرَ اللَّهُ لَهُ»",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    lineHeight = 16.sp,
                                    color = appColors.textSecondary
                                )
                            )
                        }
                    }
                }

                // DISMISS BUTTON
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("panic_dismiss_button"),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SpiritualEmerald)
                ) {
                    Text(
                        text = "الحمد لله، استعذتُ بالله وهدأت نفسي",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF041008)
                        )
                    )
                }
            }
        }
    }
}
