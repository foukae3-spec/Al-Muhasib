package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.NeumorphicInsetBox
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeHero
import com.example.ui.components.SquircleShapeMedium
import com.example.ui.components.SquircleShapeSmall
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.SpiritualCyanBright
import com.example.ui.theme.SpiritualEmeraldBright
import com.example.ui.theme.SpiritualGoldBright
import com.example.util.HapticFeedbackUtil

/**
 * Data model for a single onboarding walkthrough step.
 */
data class OnboardingStepData(
    val stepIndex: Int,
    val badge: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val heroIcon: ImageVector,
    val accentColor: Color,
    val features: List<Pair<ImageVector, Pair<String, String>>>
)

/**
 * Minimal, Apple-grade Frosted Glass Onboarding Experience Dialog.
 *
 * Tailored for high-contrast Arabic readability across all ages (including elders).
 * Introduces Al-Muhasib's 3 core pillars in concise, accessible steps:
 * 1. Faith & Sobriety Guardian (Private, ad-free, zero tracking).
 * 2. Ambient AOD & Hardware Volume-Button Counting (Tactile haptics).
 * 3. Quran Reader & Emergency Grounding (Panic Craving Shield & Adhan warning).
 */
@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit,
    onComplete: () -> Unit = onDismiss,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val scrollState = rememberScrollState()

    var currentStepIndex by remember { mutableIntStateOf(0) }

    // Intercept back navigation to step backward or dismiss
    BackHandler(enabled = true) {
        if (currentStepIndex > 0) {
            currentStepIndex--
        } else {
            onDismiss()
        }
    }

    val steps = remember {
        listOf(
            OnboardingStepData(
                stepIndex = 0,
                badge = "الخطوة ١ من ٣ • البداية المباركة",
                title = "رفيقك الإيماني والتعافي",
                subtitle = "حارس إسلامي خاص ومجاني بالكامل لوجه الله تعالى",
                description = "مساحتك الآمنة لحفظ صلواتك الخمس، وأذكارك المأثورة، ومسار طهارتك وتعافيك بأعلى درجات الخصوصية؛ دون إعلانات، ودون تتبع، وبلا جمع لأي بيانات شخصية.",
                heroIcon = Icons.Filled.VerifiedUser,
                accentColor = SpiritualEmeraldBright,
                features = listOf(
                    Icons.Filled.VerifiedUser to (
                        "أمان وخصوصية مطلقة" to "لا اتصال بسيرفرات خارجية ولا إعلانات، كافة سجلاتك مشفرة ومحفوظة على هاتفك فقط."
                    ),
                    Icons.Filled.Mosque to (
                        "المحاسبة الإيمانية المتكاملة" to "مؤشر يومي ذكي يربط الصلاة والذكر والتعافي في حلقة روحانية واحدة."
                    )
                )
            ),
            OnboardingStepData(
                stepIndex = 1,
                badge = "الخطوة ٢ من ٣ • تسبيح بدون شاشة",
                title = "الشاشة الدائمة والمسبحة الفيزيائية",
                subtitle = "سبّح في أي وقت وأنت مغمض العينين بأزرار الصوت",
                description = "حوّل أزرار الصوت بهاتفك إلى مسبحة ذكية؛ اضغط على زر رفع أو خفض الصوت للتسبيح غيباً بنبضات اهتزاز ميكانيكية واقعية، مع شاشة محيطية دائمة (AOD) خافتة وموفرة للبطارية.",
                heroIcon = Icons.Filled.VolumeUp,
                accentColor = SpiritualCyanBright,
                features = listOf(
                    Icons.Filled.VolumeUp to (
                        "مسبحة الأزرار الفيزيائية" to "سبّح وأنت تمشي أو وهاتفك في جيبك دون الحاجة للنظر إلى الشاشة بفضل الاهتزاز اللمسي."
                    ),
                    Icons.Filled.DisplaySettings to (
                        "الشاشة الدائمة (AOD)" to "عرض خافت للذكر والوقت مخصص لشاشات OLED وموفر للطاقة مع حماية تامة من الاحتراق."
                    )
                )
            ),
            OnboardingStepData(
                stepIndex = 2,
                badge = "الخطوة ٣ من ٣ • نور وثبات",
                title = "المصحف الشريف والتنبيه الحرج",
                subtitle = "وردك القرآني اليومي ودرع النجاة عند اشتداد الأزمات",
                description = "مصحف رقمي شريف وتلاوات صوتية متصلة تحفظ موضع قراءتك تلقائياً، مع زر الطوارئ الفوري للتنفس الواعي وتجديد التوبة والاستغفار عند اشتداد نزغات النفس.",
                heroIcon = Icons.AutoMirrored.Filled.MenuBook,
                accentColor = SpiritualGoldBright,
                features = listOf(
                    Icons.AutoMirrored.Filled.MenuBook to (
                        "المصحف وقراء القرآن" to "تلاوة صفحات القرآن الكريم مع استماع لأشهر القراء وحفظ تلقائي لصفحة التوقف."
                    ),
                    Icons.Filled.Alarm to (
                        "إنذار الصلاة ودرع الطوارئ" to "إنذار صوتي حاسم قبل فوات الفريضة، وجلسات تفريغ وتنفس موجهة لتثبيتك عند الأوقات الحرجة."
                    )
                )
            )
        )
    }

    val currentStep = steps[currentStepIndex]

    // Frosted Glass Scrim / Backdrop Overlay
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (appColors.isDark) Color(0xDC050E09) else Color(0xCC0D2117)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Spiritual Light Sheen behind Dialog
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        currentStep.accentColor.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.45f),
                    radius = w * 0.70f
                )
            )
        }

        // Apple-Grade Frosted Glass Modal Container
        AppleGlassCard(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 480.dp)
                .padding(vertical = 16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Consume clicks inside dialog */ }
                )
                .testTag("onboarding_dialog"),
            shape = SquircleShapeHero,
            elevation = 16.dp,
            accentGlow = currentStep.accentColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // TOP BAR: Step Progress Indicator & Skip Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Animated Step Indicators (Pills)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("onboarding_step_indicator")
                    ) {
                        steps.forEachIndexed { index, step ->
                            val isSelected = index == currentStepIndex
                            val pillWidth by animateDpAsState(
                                targetValue = if (isSelected) 28.dp else 8.dp,
                                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                label = "pill_width"
                            )

                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(pillWidth)
                                    .clip(PillShape)
                                    .background(
                                        if (isSelected) {
                                            step.accentColor
                                        } else {
                                            appColors.textTertiary.copy(alpha = 0.35f)
                                        }
                                    )
                                    .clickable {
                                        HapticFeedbackUtil.performLightTick(context)
                                        currentStepIndex = index
                                    }
                            )
                        }
                    }

                    // Skip Button ("تخطي")
                    TextButton(
                        onClick = {
                            HapticFeedbackUtil.performLightTick(context)
                            onComplete()
                        },
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "تخطي",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textTertiary,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                // DYNAMIC STEP CONTENT WITH FLUID SLIDE-FADE ANIMATION
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        val isForward = targetState.stepIndex > initialState.stepIndex
                        if (isForward) {
                            (slideInHorizontally(initialOffsetX = { -it }) + fadeIn(tween(250)))
                                .togetherWith(slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(200)))
                        } else {
                            (slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(250)))
                                .togetherWith(slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(tween(200)))
                        }
                    },
                    label = "onboarding_step_content"
                ) { step ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Glowing Hero Icon Badge
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(step.accentColor.copy(alpha = 0.14f))
                                .border(1.5.dp, step.accentColor.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = step.heroIcon,
                                contentDescription = null,
                                tint = step.accentColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Category Pill Badge
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(step.accentColor.copy(alpha = 0.12f))
                                .border(1.dp, step.accentColor.copy(alpha = 0.28f), PillShape)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = step.badge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = step.accentColor
                                )
                            )
                        }

                        // Step Title (High Contrast & Large Font for elders)
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 23.sp,
                                lineHeight = 32.sp,
                                color = appColors.textPrimary,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_step_title")
                        )

                        // Subtitle
                        Text(
                            text = step.subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.5.sp,
                                lineHeight = 22.sp,
                                color = step.accentColor,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Clear, Digestible Description
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 15.sp,
                                lineHeight = 24.sp,
                                color = appColors.textSecondary,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Concise Feature Highlights Cards
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            step.features.forEach { (icon, texts) ->
                                val (featureTitle, featureDesc) = texts
                                NeumorphicInsetBox(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = SquircleShapeMedium
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(SquircleShapeSmall)
                                                .background(step.accentColor.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = step.accentColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = featureTitle,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    color = appColors.textPrimary
                                                )
                                            )
                                            Text(
                                                text = featureDesc,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 12.sp,
                                                    lineHeight = 18.sp,
                                                    color = appColors.textSecondary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // BOTTOM ACTION CONTROLS: "السابق" & Primary "متابعة" / "ابدأ رحلتك المباركة"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = {
                                HapticFeedbackUtil.performLightTick(context)
                                currentStepIndex--
                            },
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("onboarding_prev_button"),
                            shape = PillShape,
                            border = BorderStroke(1.dp, appColors.glassBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = appColors.textPrimary
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "السابق",
                                    tint = appColors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "السابق",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = appColors.textPrimary
                                    )
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStepIndex < steps.size - 1) {
                                HapticFeedbackUtil.performLightTick(context)
                                currentStepIndex++
                            } else {
                                HapticFeedbackUtil.performMilestoneDoublePulse(context)
                                onComplete()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("onboarding_next_button"),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentStep.accentColor,
                            contentColor = Color(0xFF041008)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (currentStepIndex < steps.size - 1) "متابعة" else "ابدأ رحلتك المباركة",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF041008)
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = if (currentStepIndex < steps.size - 1) {
                                    Icons.AutoMirrored.Filled.ArrowForward
                                } else {
                                    Icons.Filled.Check
                                },
                                contentDescription = null,
                                tint = Color(0xFF041008),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
