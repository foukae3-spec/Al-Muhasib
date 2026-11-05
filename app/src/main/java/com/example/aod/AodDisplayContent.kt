package com.example.aod

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PillShape
import com.example.ui.theme.AodDimText
import com.example.ui.theme.SpiritualCyan
import com.example.ui.theme.SpiritualEmerald
import com.example.ui.theme.SpiritualEmeraldBright
import com.example.ui.theme.SpiritualGold
import com.example.ui.theme.TrueOledBlack
import com.example.util.HapticFeedbackUtil
import com.example.util.IslamicCalendarUtil
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

fun toEasternArabicDigits(text: String): String {
    val easternDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return text.map { ch ->
        if (ch in '0'..'9') easternDigits[ch - '0'] else ch
    }.joinToString("")
}

/**
 * Authentic OLED Always-On Display.
 * Ultra-dim, zero backlight bleed, pure deep pitch black (#000000), customizable styles & palettes.
 */
@Composable
fun AodDisplayContent(
    istighfarCount: Int,
    onIncrementIstighfar: () -> Unit,
    subhanAllahCount: Int = 0,
    onIncrementSubhanAllah: (() -> Unit)? = null,
    alhamdulillahCount: Int = 0,
    onIncrementAlhamdulillah: (() -> Unit)? = null,
    allahuAkbarCount: Int = 0,
    onIncrementAllahuAkbar: (() -> Unit)? = null,
    laIlahaIllallahCount: Int = 0,
    onIncrementLaIlahaIllallah: (() -> Unit)? = null,
    onUnlock: () -> Unit,
    tasbi7Count: Int = 0,
    onIncrementTasbi7: (() -> Unit)? = null,
    isInPocket: Boolean = false,
    burnInOffsetX: Int = 0,
    burnInOffsetY: Int = 0,
    clockStyle: Int = AodManager.CLOCK_DIGITAL_MINIMAL,
    accentColorStyle: Int = AodManager.ACCENT_EMERALD,
    batteryStyle: Int = AodManager.BATTERY_PERCENTAGE,
    isNotificationPrivacy: Boolean = false,
    onSelectedDhikrChanged: ((index: Int, title: String, onIncrement: () -> Unit, target: Int) -> Unit)? = null,
    onMinuteTick: ((now: LocalTime) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // If device is in pocket or face down, render absolute pitch-black blank screen and block touches
    if (isInPocket) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(TrueOledBlack)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
        )
        return
    }

    // Dynamic Accent Palette Resolution
    val (accentPrimaryColor, accentBrightColor, accentGlowColor) = when (accentColorStyle) {
        AodManager.ACCENT_GOLD -> Triple(Color(0xFFF59E0B), Color(0xFFFBBF24), Color(0x3DF59E0B))
        AodManager.ACCENT_CYAN -> Triple(Color(0xFF06B6D4), Color(0xFF38BDF8), Color(0x3D06B6D4))
        AodManager.ACCENT_WHITE -> Triple(Color(0xFFCBD5E1), Color(0xFFFFFFFF), Color(0x3DE2E8F0))
        AodManager.ACCENT_ROSE -> Triple(Color(0xFFF43F5E), Color(0xFFFB7185), Color(0x3DF43F5E))
        AodManager.ACCENT_PURPLE -> Triple(Color(0xFF8B5CF6), Color(0xFFA78BFA), Color(0x3D8B5CF6))
        else -> Triple(SpiritualEmerald, SpiritualEmeraldBright, Color(0x3D10B981))
    }

    // Live Clock & Dates
    val initialTime = remember { LocalTime.now() }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    var currentTimeString by remember { mutableStateOf(initialTime.format(timeFormatter)) }
    var currentDateArabic by remember { mutableStateOf(IslamicCalendarUtil.getFormattedArabicGregorianDate()) }
    var hijriDateArabic by remember { mutableStateOf(IslamicCalendarUtil.getFormattedHijriDate()) }

    // Read initial battery state quietly using sticky Intent without registering a continuous broadcast
    var batteryPct by remember {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 85
        mutableIntStateOf(pct)
    }
    var isCharging by remember {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        mutableStateOf(status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)
    }

    // Next Prayer State
    var nextPrayer by remember { mutableStateOf(IslamicCalendarUtil.getNextPrayer()) }

    // Independent Misbaha Options & State Decoupling
    val quickDhikrOptions = listOf(
        "أَسْتَغْفِرُ اللَّهَ",
        "سُبْحَانَ اللَّهِ",
        "الْحَمْدُ لِلَّهِ",
        "اللَّهُ أَكْبَرُ",
        "لَا إِلَهَ إِلَّا اللَّهُ"
    )
    var selectedDhikrIndex by remember { mutableIntStateOf(0) }

    // Decoupled active counters and targets per dhikr phrase
    val (activeCount, cycleTarget, onIncrementActive) = when (selectedDhikrIndex) {
        0 -> Triple(istighfarCount, 100, onIncrementIstighfar)
        1 -> Triple(subhanAllahCount, 33, { onIncrementSubhanAllah?.invoke() ?: onIncrementTasbi7?.invoke() ?: onIncrementIstighfar() })
        2 -> Triple(alhamdulillahCount, 33, { onIncrementAlhamdulillah?.invoke() ?: onIncrementTasbi7?.invoke() ?: onIncrementIstighfar() })
        3 -> Triple(allahuAkbarCount, 34, { onIncrementAllahuAkbar?.invoke() ?: onIncrementTasbi7?.invoke() ?: onIncrementIstighfar() })
        4 -> Triple(laIlahaIllallahCount, 100, { onIncrementLaIlahaIllallah?.invoke() ?: onIncrementTasbi7?.invoke() ?: onIncrementIstighfar() })
        else -> Triple(istighfarCount, 100, onIncrementIstighfar)
    }
    val remainingToCycle = cycleTarget - (activeCount % cycleTarget)

    LaunchedEffect(selectedDhikrIndex, onIncrementActive, cycleTarget) {
        onSelectedDhikrChanged?.invoke(
            selectedDhikrIndex,
            quickDhikrOptions[selectedDhikrIndex],
            onIncrementActive,
            cycleTarget
        )
    }

    // Rotating Dhikr Index
    var dhikrIndex by remember { mutableIntStateOf(0) }

    // ULTRA-EFFICIENT MINUTE-ALIGNED CLOCK TICKER
    // Only runs when NOT in pocket, aligned precisely to 60-second minute boundaries
    LaunchedEffect(isInPocket) {
        if (isInPocket) return@LaunchedEffect
        while (true) {
            val now = LocalTime.now()
            val formatted = now.format(timeFormatter)
            if (currentTimeString != formatted) {
                currentTimeString = formatted
            }
            // Update prayer countdown once per minute
            nextPrayer = IslamicCalendarUtil.getNextPrayer()

            // Rotate ambient dhikr once every minute alongside time update
            dhikrIndex++

            // Read battery state once per minute
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val capacity = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            if (capacity in 0..100 && capacity != batteryPct) {
                batteryPct = capacity
            }

            // Refresh dates if midnight passed
            if (now.hour == 0 && now.minute == 0) {
                currentDateArabic = IslamicCalendarUtil.getFormattedArabicGregorianDate()
                hijriDateArabic = IslamicCalendarUtil.getFormattedHijriDate()
            }

            // Notify parent of minute tick (for synchronized burn-in shifting, ambient light sampling, quiet hours)
            onMinuteTick?.invoke(now)

            // Sleep until the exact start of the next minute (down to the millisecond)
            val millisUntilNextMinute = (60 - now.second) * 1000L - (System.currentTimeMillis() % 1000L)
            delay(millisUntilNextMinute.coerceIn(500L, 60000L))
        }
    }

    // Vertical swipe-up to unlock gesture
    var dragAccumulated by remember { mutableStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        dragAccumulated += delta
        if (dragAccumulated < -100f) {
            onUnlock()
        }
    }

    // Active notifications and ambient edge lighting
    val activeNotifications by AodNotificationListenerService.notifications.collectAsState()
    val hasNotifications = activeNotifications.isNotEmpty()
    val primaryNotifColor = activeNotifications.firstOrNull()?.color ?: accentBrightColor

    // ULTRA-EFFICIENT STATIC HIGHLIGHT: Zero continuous 60/120 FPS animation loops!
    // Preserves deep CPU/GPU C-states while AOD sits idle.
    val edgeAlpha = 0.35f

    // PURE BLACK OLED CANVAS with Burn-in Pixel Shifting
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueOledBlack)
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { dragAccumulated = 0f }
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onUnlock() }
                )
            }
            .offset { IntOffset(burnInOffsetX, burnInOffsetY) },
        contentAlignment = Alignment.Center
    ) {
        // Ambient Edge Breathing Glow (Edge Lighting) when notifications are present
        if (hasNotifications) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(10.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                primaryNotifColor.copy(alpha = edgeAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(10.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                primaryNotifColor.copy(alpha = edgeAlpha)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // =====================================================================
            // TOP SECTION: Ambient Battery, Clock Style, Hijri & Gregorian Dates
            // =====================================================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Ambient Battery Indicator (Custom Styles)
                if (batteryStyle != AodManager.BATTERY_HIDDEN) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        if (isCharging) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = "جاري الشحن",
                                tint = accentBrightColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        when (batteryStyle) {
                            AodManager.BATTERY_BAR -> {
                                Box(
                                    modifier = Modifier
                                        .size(width = 34.dp, height = 7.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(0.5.dp, Color(0xFF475569), RoundedCornerShape(3.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(batteryPct / 100f)
                                            .background(if (batteryPct <= 20) Color(0xFFEF4444) else accentBrightColor)
                                    )
                                }
                                Text(
                                    text = "$batteryPct%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AodDimText
                                    )
                                )
                            }
                            AodManager.BATTERY_RING -> {
                                Canvas(modifier = Modifier.size(14.dp)) {
                                    drawCircle(
                                        color = Color(0xFF1E293B),
                                        radius = size.minDimension / 2,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                    drawArc(
                                        color = if (batteryPct <= 20) Color(0xFFEF4444) else accentBrightColor,
                                        startAngle = -90f,
                                        sweepAngle = (batteryPct / 100f) * 360f,
                                        useCenter = false,
                                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Text(
                                    text = "$batteryPct%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AodDimText
                                    )
                                )
                            }
                            else -> {
                                // Default BATTERY_PERCENTAGE
                                Text(
                                    text = "$batteryPct%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AodDimText
                                    )
                                )
                            }
                        }
                    }
                }

                // =================================================================
                // 5 SELECTABLE CLOCK STYLES
                // =================================================================
                when (clockStyle) {
                    AodManager.CLOCK_ARABIC_INDIC -> {
                        // Classical Eastern Arabic Digits ٠١٢٣٤٥٦٧٨٩
                        val indicTime = remember(currentTimeString) {
                            toEasternArabicDigits(currentTimeString.ifEmpty { "12:00" })
                        }
                        Text(
                            text = indicTime,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 66.sp,
                                color = accentBrightColor,
                                letterSpacing = 2.sp
                            )
                        )
                    }
                    AodManager.CLOCK_ANALOG -> {
                        // Spiritual Analog Round Face
                        val now = remember(currentTimeString) { LocalTime.now() }
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val radius = size.minDimension / 2
                                val center = Offset(size.width / 2, size.height / 2)

                                // Outer Rim
                                drawCircle(
                                    color = Color(0xFF1B2620),
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = 1.5.dp.toPx())
                                )

                                // 12 Hour Marks
                                for (i in 0 until 12) {
                                    val angleRad = (i * 30 - 90) * (Math.PI / 180.0)
                                    val isMajor = i % 3 == 0
                                    val innerR = radius - (if (isMajor) 10.dp.toPx() else 6.dp.toPx())
                                    val outerR = radius - 3.dp.toPx()
                                    val startX = (center.x + innerR * cos(angleRad)).toFloat()
                                    val startY = (center.y + innerR * sin(angleRad)).toFloat()
                                    val endX = (center.x + outerR * cos(angleRad)).toFloat()
                                    val endY = (center.y + outerR * sin(angleRad)).toFloat()

                                    drawLine(
                                        color = if (isMajor) accentBrightColor else Color(0xFF475569),
                                        start = Offset(startX, startY),
                                        end = Offset(endX, endY),
                                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }

                                // Hour Hand
                                val hours12 = now.hour % 12
                                val hourAngleRad = ((hours12 + now.minute / 60f) * 30 - 90) * (Math.PI / 180.0)
                                val hourHandLength = radius * 0.52f
                                val hourEndX = (center.x + hourHandLength * cos(hourAngleRad)).toFloat()
                                val hourEndY = (center.y + hourHandLength * sin(hourAngleRad)).toFloat()
                                drawLine(
                                    color = Color(0xFFE2E8F0),
                                    start = center,
                                    end = Offset(hourEndX, hourEndY),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )

                                // Minute Hand
                                val minAngleRad = (now.minute * 6 - 90) * (Math.PI / 180.0)
                                val minHandLength = radius * 0.76f
                                val minEndX = (center.x + minHandLength * cos(minAngleRad)).toFloat()
                                val minEndY = (center.y + minHandLength * sin(minAngleRad)).toFloat()
                                drawLine(
                                    color = accentBrightColor,
                                    start = center,
                                    end = Offset(minEndX, minEndY),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )

                                // Center Pivot Dot
                                drawCircle(
                                    color = accentBrightColor,
                                    radius = 3.dp.toPx(),
                                    center = center
                                )
                            }
                        }
                    }
                    AodManager.CLOCK_STACKED -> {
                        // Stacked Digital (HH on top, mm on bottom)
                        val parts = currentTimeString.split(":")
                        val hh = parts.getOrNull(0) ?: "12"
                        val mm = parts.getOrNull(1) ?: "00"
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy((-12).dp)
                        ) {
                            Text(
                                text = hh,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 50.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            )
                            Text(
                                text = mm,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.ExtraLight,
                                    fontSize = 50.sp,
                                    color = accentBrightColor
                                )
                            )
                        }
                    }
                    AodManager.CLOCK_GEOMETRIC -> {
                        // Islamic Geometric Frame
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0A120D))
                                .border(1.5.dp, accentBrightColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 22.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentTimeString.ifEmpty { "12:00" },
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Light,
                                    fontSize = 58.sp,
                                    color = Color(0xFFE2E8F0),
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                    else -> {
                        // Default CLOCK_DIGITAL_MINIMAL
                        Text(
                            text = currentTimeString.ifEmpty { "12:00" },
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraLight,
                                fontSize = 70.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        )
                    }
                }

                // Authentic Arabic Hijri & Gregorian Dates
                Text(
                    text = hijriDateArabic,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = accentBrightColor.copy(alpha = 0.95f)
                    )
                )
                Text(
                    text = currentDateArabic,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = AodDimText
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Ambient Salah Countdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF0F1713))
                        .border(1.dp, accentBrightColor.copy(alpha = 0.2f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mosque,
                        contentDescription = "الصلاة القادمة",
                        tint = accentBrightColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${nextPrayer.arabicName} بعد ${nextPrayer.timeRemainingFormatted} ⏳",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1)
                        )
                    )
                }

                // Colored Notification Badges or Discreet Glowing Dots (Privacy Mode)
                if (hasNotifications) {
                    Spacer(modifier = Modifier.height(6.dp))
                    if (isNotificationPrivacy) {
                        // Privacy Mode: Elegant glowing dots
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF0F1713))
                                .border(1.dp, primaryNotifColor.copy(alpha = 0.4f), CircleShape)
                                .clickable {
                                    HapticFeedbackUtil.performLightTick(context)
                                    onUnlock()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            activeNotifications.take(5).forEach { notif ->
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(notif.color)
                                )
                            }
                        }
                    } else {
                        // Full Brand Avatar & Count Mode
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF0F1713))
                                .border(1.dp, primaryNotifColor.copy(alpha = 0.5f), CircleShape)
                                .clickable {
                                    HapticFeedbackUtil.performLightTick(context)
                                    onUnlock()
                                }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy((-6).dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                activeNotifications.take(4).forEach { notif ->
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(notif.color.copy(alpha = 0.25f))
                                            .border(1.5.dp, notif.color, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = notif.appName.take(1),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = notif.color
                                            )
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${activeNotifications.sumOf { it.count }} إشعار • اضغط للمشاهدة",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE2E8F0)
                                )
                            )
                        }
                    }
                }
            }

            // =====================================================================
            // CENTER SECTION: Fully Decoupled Independent Dhikr Misbaha Ring
            // =====================================================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // Decoupled Dhikr Selector Pills (5 independent Dhikr phrases)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    quickDhikrOptions.forEachIndexed { index, title ->
                        val isSelected = index == selectedDhikrIndex
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(if (isSelected) accentGlowColor else Color(0xFF0E1310))
                                .border(
                                    1.dp,
                                    if (isSelected) accentBrightColor else Color(0x15FFFFFF),
                                    PillShape
                                )
                                .clickable {
                                    HapticFeedbackUtil.performLightTick(context)
                                    selectedDhikrIndex = index
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) accentBrightColor else Color(0xFF6B7280)
                                )
                            )
                        }
                    }
                }

                var isRingPressed by remember { mutableStateOf(false) }
                val ringScale by animateFloatAsState(
                    targetValue = if (isRingPressed) 0.93f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "aod_ring_scale"
                )

                // Interactive Ring Tap Area with tactile spring depression
                Box(
                    modifier = Modifier
                        .size(175.dp)
                        .graphicsLayer {
                            scaleX = ringScale
                            scaleY = ringScale
                        }
                        .testTag("aod_istighfar_ring")
                        .pointerInput(selectedDhikrIndex, activeCount) {
                            detectTapGestures(
                                onPress = {
                                    isRingPressed = true
                                    tryAwaitRelease()
                                    isRingPressed = false
                                },
                                onTap = {
                                    val nextCount = activeCount + 1
                                    HapticFeedbackUtil.performMisbahaFeedback(
                                        context = context,
                                        profile = AodManager.getMisbahaHapticProfile(context),
                                        nextCount = nextCount,
                                        cycleTarget = cycleTarget
                                    )
                                    onIncrementActive()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(175.dp)) {
                        val strokeWidth = 3.5.dp.toPx()
                        val radius = (size.width / 2) - strokeWidth
                        val center = Offset(size.width / 2, size.height / 2)

                        // Ambient Dim Track
                        drawCircle(
                            color = Color(0xFF141F18),
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )

                        // Glowing Arc in chosen accent color
                        val progress = (activeCount % cycleTarget) / cycleTarget.toFloat()
                        drawArc(
                            color = accentBrightColor,
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Inside Ring: Arabic Dhikr Calligraphy + Independent Count
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = quickDhikrOptions[selectedDhikrIndex],
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$activeCount",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 34.sp,
                                color = accentBrightColor
                            )
                        )
                        Text(
                            text = if (selectedDhikrIndex == 0) "المِسْ لِلِاسْتِغْفَارِ" else "المِسْ لِلتَّسْبِيحِ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Normal,
                                color = AodDimText
                            )
                        )
                    }
                }

                Text(
                    text = "متبقي $remainingToCycle على الختمة ($cycleTarget)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                )
            }

            // =====================================================================
            // ACTIVE BOOK QUOTE BOX (Updates strictly on minute tick - 0% CPU churn)
            // =====================================================================
            val pinnedQuote = remember(currentTimeString) {
                com.example.util.QuotePreferences.getPinnedQuote(context)
            }
            ActiveBookQuoteBox(
                quote = pinnedQuote,
                accentColor = accentBrightColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            // =====================================================================
            // BOTTOM SECTION: Dynamic Time-of-Day Spiritual Dua (Clean AMOLED Purity)
            // =====================================================================
            val phaseDuas = remember(currentTimeString) {
                TimeOfDayDuaProvider.getDuasForCurrentPhase(LocalTime.now())
            }
            val currentPhase = remember(currentTimeString) {
                TimeOfDayDuaProvider.getCurrentPhase(LocalTime.now())
            }
            val currentDhikr = phaseDuas.getOrElse(dhikrIndex % phaseDuas.size) { phaseDuas[0] }

            AnimatedContent(
                targetState = currentDhikr,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "aod_phase_dua_rotation"
            ) { dua ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentPhase.arabicTitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentBrightColor.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = dua.arabic,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = dua.source,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveBookQuoteBox(
    quote: com.example.util.ActiveQuote,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF090D0B))
            .border(0.5.dp, Color(0x2210B981), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "📖 ${quote.bookTitle}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor.copy(alpha = 0.9f)
                )
            )
            Text(
                text = quote.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = Color(0xFF6B7280)
                )
            )
        }
        Text(
            text = quote.content,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFD1D5DB),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            ),
            maxLines = 2
        )
    }
}
