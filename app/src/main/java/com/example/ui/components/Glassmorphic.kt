package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DisplaySettings
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.RingDetoxColor
import com.example.ui.theme.RingDhikrColor
import com.example.ui.theme.RingPrayerColor
import com.example.ui.theme.SpiritualEmerald
import com.example.ui.theme.SpiritualEmeraldBright
import com.example.util.HapticFeedbackUtil

val SquircleShapeHero = RoundedCornerShape(26.dp)
val SquircleShapeLarge = RoundedCornerShape(20.dp)
val SquircleShapeMedium = RoundedCornerShape(16.dp)
val SquircleShapeSmall = RoundedCornerShape(12.dp)
val PillShape = RoundedCornerShape(percent = 50)

/**
 * Rich Dark Green Ambient Backdrop.
 * Breathing emerald & celestial teal nebulae over deep forest black.
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val appColors = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(appColors.background)
    ) {
        // High-efficiency static ambient glow (cached canvas layer, 0% CPU/GPU churn when idle)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (appColors.isDark) {
                // Top-Right Luminous Sheen matching theme primary accent
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(appColors.primaryAccent.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(w * 0.85f, h * 0.12f),
                        radius = w * 0.65f
                    )
                )
                // Center-Left Celestial Mist
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(appColors.blue.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(w * 0.12f, h * 0.50f),
                        radius = w * 0.55f
                    )
                )
                // Bottom-Right Ambient Reflection
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(appColors.amber.copy(alpha = 0.06f), Color.Transparent),
                        center = Offset(w * 0.80f, h * 0.85f),
                        radius = w * 0.60f
                    )
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(appColors.primaryAccent.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(w * 0.88f, h * 0.10f),
                        radius = w * 0.70f
                    )
                )
            }
        }

        content()
    }
}

/**
 * Tactile Dark Green Neumorphic Card with Soft Extruded Shadows & Specular Highlight.
 */
@Composable
fun AppleGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = SquircleShapeLarge,
    elevation: Dp = 6.dp,
    accentGlow: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val appColors = LocalAppColors.current

    val baseModifier = if (onClick != null) {
        modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else modifier

    val borderBrush = if (accentGlow != null) {
        Brush.verticalGradient(
            colors = listOf(
                accentGlow.copy(alpha = 0.50f),
                appColors.glassBorder
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                if (appColors.isDark) Color(0x3834D399) else Color(0x18000000),
                if (appColors.isDark) Color(0x0D10B981) else Color(0x06000000)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    val cardBackgroundBrush = if (appColors.isDark) {
        Brush.linearGradient(
            colors = listOf(
                appColors.glassCardElevated,
                appColors.glassCard
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(listOf(appColors.glassCard, appColors.glassCard))
    }

    Box(
        modifier = baseModifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (appColors.isDark) appColors.shadow else Color(0x10000000),
                spotColor = if (appColors.isDark) Color(0xAA000000) else Color(0x1A000000)
            )
            .clip(shape)
            .background(cardBackgroundBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
    ) {
        content()
    }
}

/**
 * Debossed / Inset Sunken Surface for interactive chips, meters, and embedded inputs.
 */
@Composable
fun NeumorphicInsetBox(
    modifier: Modifier = Modifier,
    shape: Shape = SquircleShapeSmall,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val appColors = LocalAppColors.current

    val baseModifier = if (onClick != null) {
        modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else modifier

    Box(
        modifier = baseModifier
            .clip(shape)
            .background(appColors.inset)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0x10000000),
                        if (appColors.isDark) Color(0x1F10B981) else Color(0x0A000000)
                    )
                ),
                shape = shape
            ),
        content = content
    )
}

/**
 * 3 High-Contrast Distinct Concentric Activity Rings:
 * Outer: Luminous Emerald (#10B981) -> 5 Daily Prayers
 * Middle: Celestial Azure/Cyan (#06B6D4) -> Daily Dhikr & Istighfar
 * Inner: Warm Amber Gold (#F59E0B) -> Purity & Sobriety Streak
 */
@Composable
fun ConcentricActivityRings(
    prayerProgress: Float,
    dhikrProgress: Float,
    detoxProgress: Float,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 160.dp
) {
    val appColors = LocalAppColors.current
    val animPrayer = remember { Animatable(0f) }
    val animDhikr = remember { Animatable(0f) }
    val animDetox = remember { Animatable(0f) }

    LaunchedEffect(prayerProgress, dhikrProgress, detoxProgress) {
        animPrayer.animateTo(
            targetValue = prayerProgress.coerceIn(0f, 1f),
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
        animDhikr.animateTo(
            targetValue = dhikrProgress.coerceIn(0f, 1f),
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
        animDetox.animateTo(
            targetValue = detoxProgress.coerceIn(0f, 1f),
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val strokeWidth = 12.dp.toPx()
            val ringGap = 5.dp.toPx()
            val center = Offset(size.width / 2, size.height / 2)

            // 1. OUTER RING: Salah (Luminous Emerald #10B981)
            val radiusOuter = (size.width / 2) - (strokeWidth / 2)
            drawCircle(
                color = RingPrayerColor.copy(alpha = 0.16f),
                radius = radiusOuter,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = RingPrayerColor,
                startAngle = -90f,
                sweepAngle = animPrayer.value * 360f,
                useCenter = false,
                topLeft = Offset(center.x - radiusOuter, center.y - radiusOuter),
                size = Size(radiusOuter * 2, radiusOuter * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. MIDDLE RING: Dhikr (Celestial Cyan #06B6D4)
            val radiusMiddle = radiusOuter - strokeWidth - ringGap
            drawCircle(
                color = RingDhikrColor.copy(alpha = 0.16f),
                radius = radiusMiddle,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = RingDhikrColor,
                startAngle = -90f,
                sweepAngle = animDhikr.value * 360f,
                useCenter = false,
                topLeft = Offset(center.x - radiusMiddle, center.y - radiusMiddle),
                size = Size(radiusMiddle * 2, radiusMiddle * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 3. INNER RING: Sobriety / Purity (Warm Amber Gold #F59E0B)
            val radiusInner = radiusMiddle - strokeWidth - ringGap
            drawCircle(
                color = RingDetoxColor.copy(alpha = 0.16f),
                radius = radiusInner,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = RingDetoxColor,
                startAngle = -90f,
                sweepAngle = animDetox.value * 360f,
                useCenter = false,
                topLeft = Offset(center.x - radiusInner, center.y - radiusInner),
                size = Size(radiusInner * 2, radiusInner * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Tactile Center Core with Overall Muhasabah Percentage
        val overallPct = ((animPrayer.value + animDhikr.value + animDetox.value) / 3f * 100f).toInt()
        Box(
            modifier = Modifier
                .size(sizeDp * 0.44f)
                .clip(CircleShape)
                .background(appColors.inset)
                .border(1.dp, Color(0x2210B981), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$overallPct%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = appColors.textPrimary
                    )
                )
                Text(
                    text = "المُحَاسَبَة",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpiritualEmeraldBright
                    )
                )
            }
        }
    }
}

/**
 * Floating Dark Green Neumorphic Navigation Bar
 */
enum class NavigationTab(
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    @DrawableRes val iconRes: Int? = null
) {
    GUARDIAN("الرئيسية", Icons.Filled.Home, Icons.Outlined.Home),
    QURAN("القرآن", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    ADHKAR("الأذكار", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    RECOVERY("التعافي", Icons.Filled.Mosque, Icons.Outlined.Mosque, iconRes = R.drawable.ic_sujood),
    BOOKS("الكتب", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    AOD("الشاشة الدائمة", Icons.Filled.DisplaySettings, Icons.Outlined.DisplaySettings),
    LEADERBOARD("المتسابقون", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents);

    companion object {
        val PRIMARY_TABS = listOf(GUARDIAN, QURAN, ADHKAR, RECOVERY, BOOKS)
    }
}

@Composable
fun FloatingPillNavigationBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    tabs: List<NavigationTab> = NavigationTab.PRIMARY_TABS
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(70.dp) // ponytail: comfortable thumb target & elderly-accessible height
                .shadow(
                    elevation = 16.dp,
                    shape = PillShape,
                    ambientColor = if (appColors.isDark) Color(0x3510B981) else appColors.shadow,
                    spotColor = if (appColors.isDark) Color(0xD0000000) else Color(0x30000000)
                )
                .clip(PillShape)
                .background(
                    if (appColors.isDark) {
                        Brush.verticalGradient(
                            listOf(appColors.glassCardElevated, appColors.surface)
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFF7FAF8))
                        )
                    }
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            if (appColors.isDark) appColors.primaryAccentBright.copy(alpha = 0.40f) else Color(0x2010B981),
                            if (appColors.isDark) appColors.primaryAccent.copy(alpha = 0.18f) else Color(0x0C10B981)
                        )
                    ),
                    shape = PillShape
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = tab == currentTab

                val bgScale by animateFloatAsState(
                    targetValue = if (selected) 1f else 0.94f,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
                    label = "tab_scale"
                )

                val glowAlpha by animateFloatAsState(
                    targetValue = if (selected) 1f else 0f,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
                    label = "tab_glow"
                )

                val tabWeight by animateFloatAsState(
                    targetValue = if (selected) 1.45f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
                    label = "tab_weight"
                )

                Box(
                    modifier = Modifier
                        .weight(tabWeight)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                            val activeTabBrush = if (appColors.isDark) {
                                Brush.horizontalGradient(
                                    listOf(
                                        appColors.primaryAccent.copy(alpha = 0.35f),
                                        appColors.primaryAccentBright.copy(alpha = 0.22f)
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        appColors.primaryAccent.copy(alpha = 0.24f),
                                        appColors.primaryAccent.copy(alpha = 0.14f)
                                    )
                                )
                            }
                            val transparentBrush = Brush.horizontalGradient(
                                listOf(Color.Transparent, Color.Transparent)
                            )

                            Box(
                                modifier = Modifier
                                    .testTag("nav_tab_${tab.name.lowercase()}")
                                    .graphicsLayer(scaleX = bgScale, scaleY = bgScale)
                                    .shadow(
                                        elevation = (8 * glowAlpha).dp,
                                        shape = PillShape,
                                        ambientColor = appColors.primaryAccentBright.copy(alpha = 0.45f * glowAlpha),
                                        spotColor = appColors.primaryAccent.copy(alpha = 0.35f * glowAlpha)
                                    )
                                    .clip(PillShape)
                                    .then(
                                        if (selected) Modifier.background(activeTabBrush) else Modifier
                                    )
                                    .then(
                                        if (selected) {
                                            Modifier.border(
                                                width = 1.2.dp,
                                                brush = Brush.horizontalGradient(
                                                    listOf(
                                                        appColors.primaryAccentBright.copy(alpha = 0.85f * glowAlpha),
                                                        appColors.primaryAccent.copy(alpha = 0.40f * glowAlpha)
                                                    )
                                                ),
                                                shape = PillShape
                                            )
                                        } else Modifier
                                    )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!selected) {
                                    HapticFeedbackUtil.performLightTick(context)
                                    onTabSelected(tab)
                                }
                            }
                            .padding(horizontal = if (selected) 14.dp else 10.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (tab.iconRes != null) {
                                Icon(
                                    painter = painterResource(id = tab.iconRes),
                                    contentDescription = tab.label,
                                    tint = if (selected) appColors.primaryAccentBright else appColors.textTertiary,
                                    modifier = Modifier.size(if (selected) 28.dp else 26.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = if (selected) tab.activeIcon else tab.inactiveIcon,
                                    contentDescription = tab.label,
                                    tint = if (selected) appColors.primaryAccentBright else appColors.textTertiary,
                                    modifier = Modifier.size(if (selected) 28.dp else 26.dp)
                                )
                            }
                            if (selected) {
                                // ponytail: prevent vertical character stacking on large accessibility font scales
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = if (appColors.isDark) Color(0xFFEDF8F2) else appColors.textPrimary
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
