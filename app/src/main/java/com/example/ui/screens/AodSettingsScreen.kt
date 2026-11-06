package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aod.AodActivity
import com.example.aod.AodManager
import com.example.data.model.AppTheme
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeLarge
import com.example.ui.components.SquircleShapeMedium
import com.example.ui.components.SquircleShapeSmall
import com.example.ui.theme.AppleCrimson
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.SpiritualAmber
import com.example.ui.theme.SpiritualEmerald
import com.example.ui.theme.SpiritualEmeraldBright
import com.example.ui.theme.SpiritualGoldBright
import com.example.util.HapticFeedbackUtil
import com.example.util.PermissionHelper

@Composable
fun AodSettingsScreen(
    currentTheme: AppTheme = AppTheme.DEFAULT,
    onThemeChanged: (AppTheme) -> Unit = {},
    isAodEnabled: Boolean,
    onToggleAod: (Boolean) -> Unit,
    isBurnInProtectionEnabled: Boolean,
    onToggleBurnIn: (Boolean) -> Unit,
    brightness: Float,
    onBrightnessChanged: (Float) -> Unit,
    isAutoBrightnessEnabled: Boolean = true,
    onToggleAutoBrightness: (Boolean) -> Unit = {},
    aodDisplayMode: Int = AodManager.MODE_ALWAYS_ON,
    onDisplayModeChanged: (Int) -> Unit = {},
    isBatterySaverCutoffEnabled: Boolean = true,
    onToggleBatterySaverCutoff: (Boolean) -> Unit = {},
    misbahaHapticProfile: Int = AodManager.HAPTIC_MECHANICAL,
    onHapticProfileChanged: (Int) -> Unit = {},
    isPocketModeEnabled: Boolean = true,
    onTogglePocketMode: (Boolean) -> Unit = {},
    aodClockStyle: Int = AodManager.CLOCK_DIGITAL_MINIMAL,
    onClockStyleChanged: (Int) -> Unit = {},
    aodAccentColor: Int = AodManager.ACCENT_EMERALD,
    onAccentColorChanged: (Int) -> Unit = {},
    aodBatteryStyle: Int = AodManager.BATTERY_PERCENTAGE,
    onBatteryStyleChanged: (Int) -> Unit = {},
    isNotificationPrivacy: Boolean = false,
    onToggleNotificationPrivacy: (Boolean) -> Unit = {},
    onTestAlarm: () -> Unit,
    isFloatingDhikrEnabled: Boolean = false,
    onToggleFloatingDhikr: (Boolean) -> Unit = {},
    onReplayOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val scrollState = rememberScrollState()

    var hasOverlayPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        )
    }
    var hasFullScreenPermission by remember {
        mutableStateOf(PermissionHelper.canUseFullScreenIntent(context))
    }
    var isIgnoringBattery by remember {
        mutableStateOf(PermissionHelper.isIgnoringBatteryOptimizations(context))
    }
    var hasNotificationAccess by remember {
        mutableStateOf(com.example.aod.AodNotificationListenerService.isNotificationAccessGranted(context))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else true
                hasFullScreenPermission = PermissionHelper.canUseFullScreenIntent(context)
                isIgnoringBattery = PermissionHelper.isIgnoringBatteryOptimizations(context)
                hasNotificationAccess = com.example.aod.AodNotificationListenerService.isNotificationAccessGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER
        Column {
            Text(
                text = "الشَّاشَةُ الدَّائِمَةُ (AOD)",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = appColors.textPrimary,
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp
                )
            )
            Text(
                text = "محرك العرض المحيطي التلقائي فائق التوفير لشاشات OLED",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SpiritualEmeraldBright,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            )
        }

        // PREVIEW HERO CARD: Experience Real AOD Now
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge,
            elevation = 8.dp,
            accentGlow = SpiritualEmerald,
            onClick = {
                val aodIntent = Intent(context, AodActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                    )
                }
                context.startActivity(aodIntent)
            }
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SpiritualEmerald.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Launch,
                        contentDescription = "معاينة",
                        tint = SpiritualEmeraldBright,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "معاينة شاشة AOD الحقيقية الآن",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        )
                    )
                    Text(
                        text = "ملء الشاشة بالكامل بدون أي أشرطة نظام مع خاتم التسبيح وساعة الصلاة ومستشعر الجيب",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = appColors.textSecondary,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }

        // =========================================================================
        // VISUAL THEME SELECTOR CARD (Multi-Theme Customization System)
        // =========================================================================
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth().testTag("theme_selector_card"),
            shape = SquircleShapeLarge,
            elevation = 6.dp,
            accentGlow = currentTheme.primaryColor
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Palette Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(currentTheme.primaryColor.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = "سمات التطبيق",
                            tint = currentTheme.primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "أَلْوَانُ التَّطْبِيقِ وَالسِّمَاتُ الرُّوحَانِيَّةُ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "اختر الطابع اللوني الروحاني المفضل لديك (شاشات OLED فائقة التوفير)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = appColors.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Row of Circular Preview Swatches
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppTheme.entries.forEach { theme ->
                        val isSelected = currentTheme == theme
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(SquircleShapeSmall)
                                .clickable {
                                    HapticFeedbackUtil.performLightTick(context)
                                    onThemeChanged(theme)
                                }
                                .padding(4.dp)
                                .testTag("swatch_quick_${theme.id}")
                        ) {
                            // Circular Preview Swatch
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(theme.backgroundColor)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.5.dp,
                                        color = if (isSelected) theme.primaryColor else theme.primaryColor.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(theme.primaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "محدد",
                                            tint = if (theme == AppTheme.MONOCHROME) Color.Black else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(theme.accentColor)
                                        )
                                    }
                                }
                            }

                            // Short title
                            // ponytail: prevent vertical letter stacking on large accessibility font scales
                            Text(
                                text = theme.arabicTitle.split(" ").firstOrNull() ?: theme.arabicTitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) theme.primaryColor else appColors.textSecondary,
                                    fontSize = 10.sp
                                ),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Detailed Theme Selector Items
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTheme.entries.forEach { theme ->
                        val isSelected = currentTheme == theme
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SquircleShapeMedium)
                                .background(if (isSelected) theme.primaryColor.copy(alpha = 0.12f) else appColors.inset)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) theme.primaryColor else Color(0x15FFFFFF),
                                    shape = SquircleShapeMedium
                                )
                                .clickable {
                                    HapticFeedbackUtil.performLightTick(context)
                                    onThemeChanged(theme)
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("theme_selector_${theme.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Circular preview swatch
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(theme.backgroundColor)
                                        .border(2.dp, theme.primaryColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(theme.primaryColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(theme.accentColor)
                                        )
                                    }
                                }

                                // Arabic title & description
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = theme.arabicTitle,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) theme.primaryColor else appColors.textPrimary,
                                                fontSize = 14.sp
                                            )
                                        )
                                        if (theme == AppTheme.EMERALD_NIGHT) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(PillShape)
                                                    .background(theme.primaryColor.copy(alpha = 0.20f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "الافتراضي",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = theme.primaryColor
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${theme.subtitle} • ${theme.englishTitle}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) appColors.textPrimary else appColors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                // Selected indicator
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "مفعل",
                                        tint = theme.primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .border(1.5.dp, appColors.textTertiary.copy(alpha = 0.5f), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // AOD CUSTOMIZATION & CLOCK STYLES CARD (Detailed Personalization)
        // =========================================================================
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth().testTag("aod_customization_card"),
            shape = SquircleShapeLarge,
            elevation = 6.dp,
            accentGlow = when (aodAccentColor) {
                AodManager.ACCENT_GOLD -> SpiritualGoldBright
                AodManager.ACCENT_CYAN -> Color(0xFF06B6D4)
                AodManager.ACCENT_WHITE -> Color.White
                AodManager.ACCENT_ROSE -> Color(0xFFF43F5E)
                AodManager.ACCENT_PURPLE -> Color(0xFF8B5CF6)
                else -> SpiritualEmeraldBright
            }
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x2810B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✨", fontSize = 20.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تَخْصِيصُ مَظْهَرِ الشَّاشَةِ الدَّائِمَةِ (AOD)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "أنماط الساعات، ألوان الإضاءة المحيطية، شكل شارة البطارية، وخصوصية الإشعارات",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = appColors.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // 1. CLOCK STYLE SELECTOR
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "نمط وشكل الساعة (Clock Style):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        )
                    )

                    val clockStyles = listOf(
                        Triple(AodManager.CLOCK_DIGITAL_MINIMAL, "رقمي بسيط", "12:00"),
                        Triple(AodManager.CLOCK_ARABIC_INDIC, "أرقام مشرقية", "١٢:٠٠"),
                        Triple(AodManager.CLOCK_ANALOG, "ساعة عقارب", "عقارب ⏱️"),
                        Triple(AodManager.CLOCK_STACKED, "رقمي رأسي", "12\n00"),
                        Triple(AodManager.CLOCK_GEOMETRIC, "إطار إسلامي", "ثماني ۞")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        clockStyles.forEach { (style, title, preview) ->
                            val isSelected = aodClockStyle == style
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(SquircleShapeSmall)
                                    .background(if (isSelected) Color(0x2810B981) else appColors.inset)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) SpiritualEmeraldBright else Color(0x15FFFFFF),
                                        shape = SquircleShapeSmall
                                    )
                                    .clickable {
                                        HapticFeedbackUtil.performLightTick(context)
                                        onClockStyleChanged(style)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = preview,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isSelected) SpiritualEmeraldBright else appColors.textPrimary,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            color = if (isSelected) SpiritualEmeraldBright else appColors.textTertiary,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. ACCENT COLOR PALETTE SELECTOR
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "لون الإضاءة المحيطية (AOD Accent Color):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        )
                    )

                    val palettes = listOf(
                        Triple(AodManager.ACCENT_EMERALD, "زمردي", Color(0xFF10B981)),
                        Triple(AodManager.ACCENT_GOLD, "مكي", Color(0xFFF59E0B)),
                        Triple(AodManager.ACCENT_CYAN, "سماوي", Color(0xFF06B6D4)),
                        Triple(AodManager.ACCENT_WHITE, "أبيض", Color(0xFFF8FAFC)),
                        Triple(AodManager.ACCENT_ROSE, "ياقوتي", Color(0xFFF43F5E)),
                        Triple(AodManager.ACCENT_PURPLE, "ملكي", Color(0xFF8B5CF6))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        palettes.forEach { (colorId, name, color) ->
                            val isSelected = aodAccentColor == colorId
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clickable {
                                        HapticFeedbackUtil.performLightTick(context)
                                        onAccentColorChanged(colorId)
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = 0.25f))
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) color else color.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = if (colorId == AodManager.ACCENT_WHITE) Color.Black else Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) color else appColors.textTertiary
                                    )
                                )
                            }
                        }
                    }
                }

                // 3. BATTERY INDICATOR STYLE
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "شكل مؤشر البطارية (Battery Indicator):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        )
                    )

                    val batteryStyles = listOf(
                        Triple(AodManager.BATTERY_PERCENTAGE, "نسبة مئوية", "85%"),
                        Triple(AodManager.BATTERY_BAR, "شريط أفقي", "▭▭"),
                        Triple(AodManager.BATTERY_RING, "حلقة دائرية", "◎"),
                        Triple(AodManager.BATTERY_HIDDEN, "مخفي", "بدون")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        batteryStyles.forEach { (style, title, iconStr) ->
                            val isSelected = aodBatteryStyle == style
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(SquircleShapeSmall)
                                    .background(if (isSelected) Color(0x2810B981) else appColors.inset)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) SpiritualEmeraldBright else Color(0x15FFFFFF),
                                        shape = SquircleShapeSmall
                                    )
                                    .clickable {
                                        HapticFeedbackUtil.performLightTick(context)
                                        onBatteryStyleChanged(style)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = iconStr,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) SpiritualEmeraldBright else appColors.textPrimary
                                        )
                                    )
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            color = if (isSelected) SpiritualEmeraldBright else appColors.textTertiary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. NOTIFICATION PRIVACY MODE SWITCH
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "وضع خصوصية الإشعارات (Privacy Dots)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary
                            )
                        )
                        Text(
                            text = "عرض نقاط مضيئة ملونة فقط دون أسماء التطبيقات أو أعداد الإشعارات",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = appColors.textSecondary
                            )
                        )
                    }

                    Switch(
                        checked = isNotificationPrivacy,
                        onCheckedChange = {
                            HapticFeedbackUtil.performLightTick(context)
                            onToggleNotificationPrivacy(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = SpiritualEmeraldBright,
                            uncheckedThumbColor = appColors.textTertiary,
                            uncheckedTrackColor = appColors.inset
                        )
                    )
                }
            }
        }


        // =========================================================================
        // SUNNI ADHAN VOICE SELECTOR CARD (PrayTimes Collection)
        // =========================================================================
        val prayerPrefs = remember { com.example.util.prayer.PrayerPreferences.getSettings(context) }
        var selectedAdhanTone by remember { mutableStateOf(prayerPrefs.adhanToneId) }
        var adhanPreviewPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
        var previewingToneId by remember { mutableStateOf<String?>(null) }

        DisposableEffect(Unit) {
            onDispose {
                try { adhanPreviewPlayer?.stop(); adhanPreviewPlayer?.release() } catch (_: Exception) {}
                adhanPreviewPlayer = null
            }
        }

        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge,
            elevation = 6.dp,
            accentGlow = SpiritualEmerald
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SpiritualEmerald.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "صوت الأذان",
                            tint = SpiritualEmeraldBright,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "صوت الأذان المبارك (مكتبة أهل السنّة)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary
                            )
                        )
                        Text(
                            text = "أصوات أذان معتمدة رسمياً وموثقة من praytimes.org",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = appColors.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                com.example.util.prayer.SUNNI_ADHANS.forEach { tone ->
                    val isSelected = selectedAdhanTone == tone.id
                    val isPreviewing = previewingToneId == tone.id

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleShapeMedium)
                            .background(if (isSelected) SpiritualEmerald.copy(alpha = 0.14f) else appColors.inset)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) SpiritualEmeraldBright else Color(0x15FFFFFF),
                                shape = SquircleShapeMedium
                            )
                            .clickable {
                                HapticFeedbackUtil.performLightTick(context)
                                selectedAdhanTone = tone.id
                                com.example.util.prayer.PrayerPreferences.updateAdhanTone(context, tone.id)
                                android.widget.Toast.makeText(context, "تم اختيار: ${tone.arabicName}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Play / Stop preview button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isPreviewing) SpiritualEmeraldBright else Color(0x2010B981))
                                        .clickable {
                                            if (isPreviewing) {
                                                try { adhanPreviewPlayer?.stop(); adhanPreviewPlayer?.release() } catch (_: Exception) {}
                                                adhanPreviewPlayer = null
                                                previewingToneId = null
                                            } else {
                                                try {
                                                    adhanPreviewPlayer?.stop()
                                                    adhanPreviewPlayer?.release()
                                                    adhanPreviewPlayer = android.media.MediaPlayer.create(context, tone.rawResId)?.apply {
                                                        setOnCompletionListener { previewingToneId = null }
                                                        start()
                                                    }
                                                    previewingToneId = tone.id
                                                } catch (_: Exception) {}
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPreviewing) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                        contentDescription = "استماع",
                                        tint = if (isPreviewing) Color.Black else SpiritualEmeraldBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = tone.arabicName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) SpiritualEmeraldBright else appColors.textPrimary
                                        )
                                    )
                                    Text(
                                        text = "${tone.reciterName} • أذان سني معتمد",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = appColors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = SpiritualEmeraldBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // XIAOMI / POCO F4 / HYPEROS PERMISSION ADVISOR
        val isAllPermissionsGranted = hasOverlayPermission && hasFullScreenPermission && isIgnoringBattery
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge,
            elevation = 6.dp,
            accentGlow = if (isAllPermissionsGranted) SpiritualEmerald else SpiritualAmber
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhoneAndroid,
                        contentDescription = "Poco F4 Setup",
                        tint = if (isAllPermissionsGranted) SpiritualEmeraldBright else SpiritualAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "إعدادات هواتف Poco F4 و Xiaomi (MIUI / HyperOS)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        )
                    )
                }

                Text(
                    text = "لتشغيل AOD تلقائياً فور قفل الهاتف دون حظر من النظام، يُرجى تفعيل إذن «الظهور فوق التطبيقات» وأذونات شاشة القفل وإلغاء قيود البطارية.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = appColors.textSecondary,
                        lineHeight = 18.sp
                    )
                )

                // Permissions Status Checklist
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // 1. Overlay Permission
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
                                imageVector = if (hasOverlayPermission) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (hasOverlayPermission) SpiritualEmeraldBright else SpiritualAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (hasOverlayPermission) "إذن الظهور فوق التطبيقات: مفعّل ✓" else "إذن الظهور فوق التطبيقات: مطلوب",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasOverlayPermission) SpiritualEmeraldBright else SpiritualAmber
                                )
                            )
                        }

                        if (!hasOverlayPermission) {
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    }
                                },
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = SpiritualAmber)
                            ) {
                                Text(
                                    text = "تفعيل",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    // 2. Full Screen Intent (Android 14+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
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
                                    imageVector = if (hasFullScreenPermission) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = if (hasFullScreenPermission) SpiritualEmeraldBright else SpiritualAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (hasFullScreenPermission) "إذن ملء الشاشة عند القفل: مفعّل ✓" else "إذن ملء الشاشة عند القفل: مطلوب",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasFullScreenPermission) SpiritualEmeraldBright else SpiritualAmber
                                    )
                                )
                            }

                            if (!hasFullScreenPermission) {
                                Button(
                                    onClick = {
                                        PermissionHelper.openFullScreenIntentSettings(context)
                                    },
                                    shape = PillShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = SpiritualAmber)
                                ) {
                                    Text(
                                        text = "تفعيل",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 3. Notification Access for AOD Badges & Glowing Effects
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (hasNotificationAccess) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (hasNotificationAccess) SpiritualEmeraldBright else SpiritualAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (hasNotificationAccess) "إذن إشعارات AOD الملونة: مفعّل ✓" else "إذن إشعارات AOD الملونة: مطلوب",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasNotificationAccess) SpiritualEmeraldBright else SpiritualAmber
                                )
                            )
                        }

                        if (!hasNotificationAccess) {
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    context.startActivity(intent)
                                },
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = SpiritualAmber)
                            ) {
                                Text(
                                    text = "تفعيل",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }

                // Xiaomi MIUI / HyperOS Specific Quick Links using PermissionHelper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            PermissionHelper.openXiaomiPopupPermissions(context)
                        },
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF142B20)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "أذونات القفل",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpiritualEmeraldBright
                            )
                        )
                    }

                    Button(
                        onClick = {
                            PermissionHelper.openXiaomiAutostart(context)
                        },
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF142B20)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "التشغيل الذاتي",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpiritualEmeraldBright
                            )
                        )
                    }

                    Button(
                        onClick = {
                            PermissionHelper.openBatteryOptimizationSettings(context)
                        },
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF142B20)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isIgnoringBattery) "بلا قيود ✓" else "إلغاء القيود",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isIgnoringBattery) SpiritualEmeraldBright else SpiritualAmber
                            )
                        )
                    }
                }
            }
        }

        // SETTINGS CARD 1: Core Lock Screen Interception
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge,
            elevation = 5.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // AOD Main Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x2410B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DisplaySettings,
                                contentDescription = "تفعيل AOD",
                                tint = SpiritualEmeraldBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "تفعيل شاشة AOD الدائمة التلقائية",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Text(
                                text = "الظهور فورياً وتلقائياً عند إغلاق شاشة الهاتف",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = appColors.textSecondary
                                )
                            )
                        }
                    }

                    Switch(
                        checked = isAodEnabled,
                        onCheckedChange = {
                            HapticFeedbackUtil.performLightTick(context)
                            onToggleAod(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = SpiritualEmeraldBright,
                            uncheckedThumbColor = appColors.textTertiary,
                            uncheckedTrackColor = appColors.inset
                        ),
                        modifier = Modifier.testTag("aod_enable_switch")
                    )
                }

                // AOD Display Mode Selector (Always On vs Tap 10s vs Scheduled)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HourglassTop,
                            contentDescription = "نمط العرض",
                            tint = SpiritualEmeraldBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "نمط عمل الشاشة (Display Mode)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val modes = listOf(
                            Triple(AodManager.MODE_ALWAYS_ON, "دائم دائماً 🌙", "عرض مستمر"),
                            Triple(AodManager.MODE_TAP_10S, "نقر 10 ثوانٍ ⏱️", "توفير 100%"),
                            Triple(AodManager.MODE_SCHEDULED, "مجدول ⏳", "إيقاف ليلاً")
                        )

                        modes.forEach { (mode, title, sub) ->
                            val isSelected = aodDisplayMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(SquircleShapeSmall)
                                    .background(if (isSelected) Color(0x2810B981) else appColors.inset)
                                    .border(
                                        1.dp,
                                        if (isSelected) SpiritualEmeraldBright else Color(0x15FFFFFF),
                                        SquircleShapeSmall
                                    )
                                    .clickable {
                                        HapticFeedbackUtil.performLightTick(context)
                                        onDisplayModeChanged(mode)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) SpiritualEmeraldBright else appColors.textSecondary,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                    Text(
                                        text = sub,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            color = if (isSelected) SpiritualGoldBright else appColors.textTertiary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Auto-Brightness Toggle (Hardware Light Sensor TYPE_LIGHT)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x2410B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BrightnessAuto,
                                contentDescription = "السطوع التلقائي",
                                tint = SpiritualEmeraldBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "السطوع التلقائي بحساس الضوء",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Text(
                                text = "تكيف دقيق مع إضاءة الغرفة لتقليل استنزاف بطارية OLED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = appColors.textSecondary
                                )
                            )
                        }
                    }

                    Switch(
                        checked = isAutoBrightnessEnabled,
                        onCheckedChange = {
                            HapticFeedbackUtil.performLightTick(context)
                            onToggleAutoBrightness(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = SpiritualEmeraldBright,
                            uncheckedThumbColor = appColors.textTertiary,
                            uncheckedTrackColor = appColors.inset
                        ),
                        modifier = Modifier.testTag("aod_auto_brightness_switch")
                    )
                }

                // Manual Brightness Baseline Slider
                if (!isAutoBrightnessEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.BrightnessLow,
                                    contentDescription = "السطوع اليدوي",
                                    tint = SpiritualEmeraldBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "السطوع اليدوي الثابت",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = appColors.textPrimary
                                    )
                                )
                            }
                            Text(
                                text = "${(brightness * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpiritualEmeraldBright
                                )
                            )
                        }

                        Slider(
                            value = brightness,
                            onValueChange = onBrightnessChanged,
                            valueRange = 0.01f..0.20f,
                            colors = SliderDefaults.colors(
                                thumbColor = SpiritualEmeraldBright,
                                activeTrackColor = SpiritualEmeraldBright,
                                inactiveTrackColor = appColors.inset
                            )
                        )
                    }
                }

                // Battery Saver Cut-off Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x24F59E0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BatterySaver,
                                contentDescription = "موفر البطارية",
                                tint = SpiritualAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "حماية البطارية المنخفضة (< 15%)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Text(
                                text = "إيقاف AOD تلقائياً عند تفعيل موفر الطاقة أو ضعف البطارية",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = appColors.textSecondary
                                )
                            )
                        }
                    }

                    Switch(
                        checked = isBatterySaverCutoffEnabled,
                        onCheckedChange = {
                            HapticFeedbackUtil.performLightTick(context)
                            onToggleBatterySaverCutoff(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = SpiritualAmber,
                            uncheckedThumbColor = appColors.textTertiary,
                            uncheckedTrackColor = appColors.inset
                        ),
                        modifier = Modifier.testTag("battery_saver_cutoff_switch")
                    )
                }

                // Pocket Mode Sensor Guard Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x2410B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.TouchApp,
                                contentDescription = "حساس الجيب",
                                tint = SpiritualEmeraldBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "حساس الجيب ومنع اللمس العرضي",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Text(
                                text = "إطفاء الشاشة تماماً لتوفير الطاقة عند وضع الهاتف في الجيب",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = appColors.textSecondary
                                )
                            )
                        }
                    }

                    Switch(
                        checked = isPocketModeEnabled,
                        onCheckedChange = {
                            HapticFeedbackUtil.performLightTick(context)
                            onTogglePocketMode(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = SpiritualEmeraldBright,
                            uncheckedThumbColor = appColors.textTertiary,
                            uncheckedTrackColor = appColors.inset
                        ),
                        modifier = Modifier.testTag("pocket_mode_switch")
                    )
                }

                // Misbaha Haptic Profile Selector (Mechanical vs Tick vs Silent)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Vibration,
                            contentDescription = "اهتزاز المسبحة",
                            tint = SpiritualEmeraldBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "اهتزاز خاتم التسبيح الرقمي (Misbaha Haptics)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val profiles = listOf(
                            Triple(AodManager.HAPTIC_MECHANICAL, "ميكانيكي ⚙️", "نقرة ثقيلة"),
                            Triple(AodManager.HAPTIC_TICK, "ناعم 📿", "نبضة خفيفة"),
                            Triple(AodManager.HAPTIC_SILENT, "صامت 🔕", "بدون اهتزاز")
                        )

                        profiles.forEach { (profile, title, sub) ->
                            val isSelected = misbahaHapticProfile == profile
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(SquircleShapeSmall)
                                    .background(if (isSelected) Color(0x2810B981) else appColors.inset)
                                    .border(
                                        1.dp,
                                        if (isSelected) SpiritualEmeraldBright else Color(0x15FFFFFF),
                                        SquircleShapeSmall
                                    )
                                    .clickable {
                                        when (profile) {
                                            AodManager.HAPTIC_MECHANICAL -> HapticFeedbackUtil.performMechanicalClick(context)
                                            AodManager.HAPTIC_TICK -> HapticFeedbackUtil.performDhikrTick(context)
                                            else -> {}
                                        }
                                        onHapticProfileChanged(profile)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) SpiritualEmeraldBright else appColors.textSecondary,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                    Text(
                                        text = sub,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            color = if (isSelected) SpiritualGoldBright else appColors.textTertiary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Live Interactive Haptic Preview Button
                    if (misbahaHapticProfile != AodManager.HAPTIC_SILENT) {
                        var testCount by remember { mutableIntStateOf(0) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SquircleShapeMedium)
                                .background(Color(0x1810B981))
                                .border(1.dp, SpiritualEmeraldBright.copy(alpha = 0.4f), SquircleShapeMedium)
                                .clickable {
                                    testCount++
                                    HapticFeedbackUtil.performMisbahaFeedback(
                                        context = context,
                                        profile = misbahaHapticProfile,
                                        nextCount = testCount,
                                        cycleTarget = 100
                                    )
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🔘",
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "اضغط لتجربة إحساس الزر الميكانيكي الآن (النقرة $testCount)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SpiritualEmeraldBright,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Burn-in Protection Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x24F59E0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = "حماية الشاشة",
                                tint = SpiritualAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "حماية الشاشة من الاحتراق",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Text(
                                text = "إزاحة العناصر بمقدار 2-3 بيكسل دورياً (Pixel Shifting)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = appColors.textSecondary
                                )
                            )
                        }
                    }

                    Switch(
                        checked = isBurnInProtectionEnabled,
                        onCheckedChange = {
                            HapticFeedbackUtil.performLightTick(context)
                            onToggleBurnIn(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = SpiritualAmber,
                            uncheckedThumbColor = appColors.textTertiary,
                            uncheckedTrackColor = appColors.inset
                        )
                    )
                }
            }
        }

        // SETTINGS CARD 2: Floating Adhkar Overlay HUD
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge,
            elevation = 5.dp,
            accentGlow = if (isFloatingDhikrEnabled) SpiritualEmerald else null
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x2410B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📿",
                                fontSize = 18.sp
                            )
                        }
                        Column {
                            Text(
                                text = "النافذة العائمة للأذكار (Floating HUD)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Text(
                                text = "ظهور بطاقة الأذكار الحالية فوق التطبيقات مع إمكانية التسبيح والتصغير لفقاعة",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = appColors.textSecondary
                                )
                            )
                        }
                    }

                    Switch(
                        checked = isFloatingDhikrEnabled,
                        onCheckedChange = { enabled ->
                            HapticFeedbackUtil.performLightTick(context)
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } else {
                                onToggleFloatingDhikr(enabled)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = SpiritualEmeraldBright,
                            uncheckedThumbColor = appColors.textTertiary,
                            uncheckedTrackColor = appColors.inset
                        ),
                        modifier = Modifier.testTag("floating_dhikr_switch")
                    )
                }
            }
        }

        // ONBOARDING TOUR REPLAY CARD
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge,
            elevation = 5.dp,
            accentGlow = SpiritualEmeraldBright
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
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "دليل البداية",
                        tint = SpiritualEmeraldBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "جَوْلَةُ التَّعْرِيفِ بِالتَّطْبِيق (التَّرْحِيب)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpiritualEmeraldBright
                        )
                    )
                }

                Text(
                    text = "تعرّف من جديد على المزايا الأساسية للمحاسب: الحارس الإيماني، والمسبحة الفيزيائية بأزرار الصوت، والمصحف الشريف ودرع الطوارئ.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = appColors.textSecondary,
                        lineHeight = 18.sp
                    )
                )

                Button(
                    onClick = onReplayOnboarding,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("replay_onboarding_button"),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SpiritualEmeraldBright)
                ) {
                    Text(
                        text = "إعادة عرض دليل البداية ✨",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF041008)
                        )
                    )
                }
            }
        }

        // ALARM TESTING CARD
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge,
            elevation = 5.dp,
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
                        imageVector = Icons.Filled.Alarm,
                        contentDescription = "إنذار الصلاة",
                        tint = AppleCrimson,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "نِظَامُ الإِنْذَارِ الحَاسِمِ لِلصَّلَوَات",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppleCrimson
                        )
                    )
                }

                Text(
                    text = "إنذار عالي الأولوية ينطلق قبل انتهاء وقت الصلاة بـ 30 دقيقة بملء الشاشة مع التنبيه الصوتي المستمر والوعيد الشديد.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = appColors.textSecondary,
                        lineHeight = 18.sp
                    )
                )

                Button(
                    onClick = onTestAlarm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("test_alarm_button"),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AppleCrimson)
                ) {
                    Text(
                        text = "تجربة شاشة وصوت إنذار الصلاة الفوري 🚨",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AodSettingsScreen(
    viewModel: com.example.ui.viewmodel.MuhasibViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        onReplayOnboarding = { viewModel.replayOnboarding() },
        modifier = modifier
    )
}
