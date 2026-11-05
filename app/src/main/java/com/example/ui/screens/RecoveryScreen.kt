package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SobrietyLog
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.NeumorphicInsetBox
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeHero
import com.example.ui.components.SquircleShapeLarge
import com.example.ui.components.SquircleShapeSmall
import com.example.ui.theme.AppleCrimson
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.SpiritualAmber
import com.example.ui.theme.SpiritualCyan
import com.example.ui.theme.SpiritualCyanBright
import com.example.ui.theme.SpiritualEmerald
import com.example.ui.theme.SpiritualEmeraldBright
import com.example.ui.theme.SpiritualGold
import com.example.ui.theme.SpiritualGoldBright
import com.example.util.HapticFeedbackUtil
import com.example.util.RelapseFrictionHelper
import kotlinx.coroutines.delay

@Composable
fun RecoveryScreen(
    sobrietyLog: SobrietyLog?,
    onToggleWorkout: (Boolean) -> Unit,
    onToggleColdShower: (Boolean) -> Unit,
    onRecordRelapse: () -> Unit,
    onTriggerPanic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val scrollState = rememberScrollState()

    val currentStreak = sobrietyLog?.cleanDaysCount ?: 0
    val maxStreak = sobrietyLog?.cleanDaysCount ?: 0
    val workoutDone = sobrietyLog?.workoutCompletedToday ?: false
    val coldShowerDone = sobrietyLog?.coldShowerToday ?: false

    val neuroplasticProgress = (currentStreak / 90f).coerceIn(0f, 1f)

    var showRelapseFrictionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .padding(top = 10.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER
        Column {
            Text(
                text = "حِصْنُ التَّعَافِي",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = appColors.textPrimary,
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp
                )
            )
            Text(
                text = "إعادة ضبط الدوبامين، الطهارة، والجهاد الأكبر",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SpiritualGoldBright,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            )
        }

        // HERO STREAK COUNTER (Warm Amber Gold)
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeHero,
            elevation = 8.dp,
            accentGlow = SpiritualAmber
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "عَدَّادُ الطَّهَارَةِ وَالنَّقَاءِ",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = appColors.textSecondary
                    )
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$currentStreak",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 64.sp,
                            color = SpiritualGoldBright
                        )
                    )
                    Text(
                        text = " يوماً",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = appColors.textPrimary
                        ),
                        modifier = Modifier.padding(bottom = 12.dp, start = 6.dp)
                    )
                }

                // 90-Day Progress Bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "مسار التعافي العصبي (90 يوماً)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = appColors.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "${(neuroplasticProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpiritualGoldBright
                            )
                        )
                    }

                    LinearProgressIndicator(
                        progress = { neuroplasticProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = SpiritualGoldBright,
                        trackColor = appColors.inset,
                        strokeCap = StrokeCap.Round
                    )
                }

                Text(
                    text = "أطول مسيرة ثبات سابقة: $maxStreak يوماً",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = appColors.textTertiary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // SOBRIETY MILESTONES & BADGES
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge,
            elevation = 6.dp,
            accentGlow = SpiritualGold
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "أوسمة الثبات",
                        tint = SpiritualGoldBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "أَوْسِمَةُ الثَّبَاتِ وَمَحَطَّاتُ النَّصْر",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RelapseFrictionHelper.MILESTONES.take(3).forEach { milestone ->
                        MilestoneBadgeCard(
                            milestone = milestone,
                            isUnlocked = currentStreak >= milestone.days,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RelapseFrictionHelper.MILESTONES.drop(3).forEach { milestone ->
                        MilestoneBadgeCard(
                            milestone = milestone,
                            isUnlocked = currentStreak >= milestone.days,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // HARSH REALITY CARD
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge,
            elevation = 4.dp,
            accentGlow = AppleCrimson
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = "تحذير الوعيد",
                        tint = AppleCrimson,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "مُحَاسَبَةُ النَّفْسِ الصَّارِمَة",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppleCrimson
                        )
                    )
                }

                Text(
                    text = "«أَتَسْتَبْدِلُونَ الَّذِي هُوَ أَدْنَىٰ بِالَّذِي هُوَ خَيْرٌ»",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary,
                        fontSize = 16.sp
                    )
                )

                Text(
                    text = "عشر دقائق من الدوبامين الرخيص والنشوة الفانية تعقبها مذلة وحرمان من التوفيق... أتجعل الله أهون الناظرين إليك وتجازف بنارٍ أوقد عليها ثلاثة آلاف عام؟",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp,
                        color = appColors.textSecondary
                    )
                )
            }
        }

        // DAILY DETOX & DOPAMINE PROTOCOLS
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge,
            elevation = 5.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "بِرُوتُوكُولُ الطَّهَارَةِ البَدَنِيَّةِ اليَوْمِيّ",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    )
                )

                // Cardio Sweat Workout (Luminous Emerald)
                NeumorphicInsetBox(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        HapticFeedbackUtil.performLightTick(context)
                        onToggleWorkout(!workoutDone)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FitnessCenter,
                                contentDescription = "تمرين",
                                tint = if (workoutDone) SpiritualEmeraldBright else appColors.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "تمرين مكثف وتعرق (45 دقيقة)",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (workoutDone) SpiritualEmeraldBright else appColors.textPrimary
                                    )
                                )
                                Text(
                                    text = "طرد السموم وإفراز الإندورفين الطبيعي وتحفيز BDNF",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        color = appColors.textSecondary
                                    )
                                )
                            }
                        }
                        Text(
                            text = if (workoutDone) "مكتمل ✓" else "لم يكتمل",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (workoutDone) SpiritualEmeraldBright else appColors.textTertiary
                            )
                        )
                    }
                }

                // Ice Cold Shower (Celestial Cyan)
                NeumorphicInsetBox(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        HapticFeedbackUtil.performLightTick(context)
                        onToggleColdShower(!coldShowerDone)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WaterDrop,
                                contentDescription = "استحمام بارد",
                                tint = if (coldShowerDone) SpiritualCyanBright else appColors.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "استحمام بالماء البارد (3 دقائق)",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (coldShowerDone) SpiritualCyanBright else appColors.textPrimary
                                    )
                                )
                                Text(
                                    text = "رفع الدوبامين المستدام بنسبة 250% وقمع الرغبة الملحة",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        color = appColors.textSecondary
                                    )
                                )
                            }
                        }
                        Text(
                            text = if (coldShowerDone) "مكتمل ✓" else "لم يكتمل",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (coldShowerDone) SpiritualCyanBright else appColors.textTertiary
                            )
                        )
                    }
                }
            }
        }

        // EMERGENCY CRAVING BUTTON
        Button(
            onClick = {
                HapticFeedbackUtil.performSevereAlarmVibration(context)
                onTriggerPanic()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(elevation = 6.dp, shape = SquircleShapeLarge, ambientColor = Color(0x60EF4444))
                .testTag("recovery_panic_button"),
            shape = SquircleShapeLarge,
            colors = ButtonDefaults.buttonColors(containerColor = AppleCrimson)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "زر النجدة",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "زِرُّ النَّجْدَةِ وَالاسْتِعَاذَةِ عِنْدَ الوَسْوَسَةِ",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        // RELAPSE REGISTRATION BUTTON (Triggers 30-Second Reflection Friction Gate)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "تسجيل انتكاسة لا قدر الله (إعادة العداد للصفر)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = appColors.textTertiary
                ),
                modifier = Modifier
                    .clickable {
                        HapticFeedbackUtil.performLightTick(context)
                        showRelapseFrictionDialog = true
                    }
                    .padding(8.dp)
            )
        }
    }

    // 30-SECOND RELAPSE REFLECTION FRICTION GATE DIALOG
    if (showRelapseFrictionDialog) {
        RelapseFrictionDialog(
            cleanDays = currentStreak,
            onConfirmRelapse = {
                onRecordRelapse()
                showRelapseFrictionDialog = false
            },
            onDismiss = {
                showRelapseFrictionDialog = false
            }
        )
    }
}

// ponytail: calming 30-second breathing & reflection friction dialog preventing impulsive streak resets
@Composable
fun RelapseFrictionDialog(
    cleanDays: Int,
    onConfirmRelapse: () -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    val context = LocalContext.current
    var secondsRemaining by remember { mutableIntStateOf(RelapseFrictionHelper.COUNTDOWN_SECONDS) }
    val breatheScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            breatheScale.animateTo(1.25f, tween(3000, easing = LinearEasing))
            breatheScale.animateTo(0.95f, tween(3000, easing = LinearEasing))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SquircleShapeLarge)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F1E17),
                            Color(0xFF070E0B)
                        )
                    )
                )
                .border(1.5.dp, SpiritualEmeraldBright.copy(alpha = 0.5f), SquircleShapeLarge)
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Calming pulsating breath circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer(
                            scaleX = breatheScale.value,
                            scaleY = breatheScale.value
                        )
                        .clip(CircleShape)
                        .background(SpiritualEmerald.copy(alpha = 0.2f))
                        .border(1.5.dp, SpiritualEmeraldBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (secondsRemaining > 0) "$secondsRemaining" else "✓",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = SpiritualEmeraldBright,
                            fontSize = 24.sp
                        )
                    )
                }

                Text(
                    text = RelapseFrictionHelper.getPromptText(cleanDays),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                )

                // Quranic Ayah
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleShapeSmall)
                        .background(Color(0x1810B981))
                        .border(1.dp, Color(0x3010B981), SquircleShapeSmall)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = RelapseFrictionHelper.getAyahText(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = SpiritualGoldBright,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    )
                }

                Text(
                    text = "خذ نفساً عميقاً... استعذ بالله من الشيطان الرجيم، فنزوة خمس دقائق تزول ويبقى ألم الذنب والحسرة.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = appColors.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                )

                // Primary Safe Action: Preserve Streak & Cancel
                Button(
                    onClick = {
                        HapticFeedbackUtil.performLightTick(context)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SpiritualEmeraldBright)
                ) {
                    Text(
                        text = "أنا صابر ومستمر بحمد الله (تثبيت العهد) 🛡️",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }

                // Destructive Action: Only enabled/available when 30s reflection countdown completes
                if (secondsRemaining <= 0) {
                    OutlinedButton(
                        onClick = {
                            HapticFeedbackUtil.performSevereAlarmVibration(context)
                            onConfirmRelapse()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = PillShape,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleCrimson),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleCrimson)
                    ) {
                        Text(
                            text = "تأكيد الانتكاسة وتجديد التوبة النصوح",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppleCrimson
                            )
                        )
                    }
                } else {
                    Text(
                        text = "نافذة التأكيد مقفلة لمدة $secondsRemaining ثانية للتفكر",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = appColors.textTertiary,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

// ponytail: milestone badge item component
@Composable
private fun MilestoneBadgeCard(
    milestone: RelapseFrictionHelper.Milestone,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    Box(
        modifier = modifier
            .clip(SquircleShapeSmall)
            .background(if (isUnlocked) Color(0x2210B981) else appColors.inset)
            .border(
                1.dp,
                if (isUnlocked) SpiritualEmeraldBright.copy(alpha = 0.6f) else Color(0x15FFFFFF),
                SquircleShapeSmall
            )
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = milestone.emoji,
                fontSize = 22.sp
            )
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isUnlocked) SpiritualEmeraldBright else appColors.textSecondary,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
            Text(
                text = milestone.subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = if (isUnlocked) SpiritualGoldBright else appColors.textTertiary
                )
            )
        }
    }
}
