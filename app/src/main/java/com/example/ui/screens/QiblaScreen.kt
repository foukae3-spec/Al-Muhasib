package com.example.ui.screens

import android.content.Context
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeLarge
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.SpiritualEmeraldBright
import com.example.util.HapticFeedbackUtil
import com.example.util.prayer.CityCatalog
import com.example.util.prayer.CityLocation
import com.example.util.prayer.PrayerPreferences
import com.example.util.prayer.UserPrayerSettings
import com.example.util.qibla.CompassSensorManager
import com.example.util.qibla.QiblaCalculator
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun QiblaScreen(
    onClose: () -> Unit,
    onOpenCityPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current

    val prayerSettings by PrayerPreferences.settingsFlow.collectAsStateWithLifecycle()
    val sensorManager = remember { CompassSensorManager(context) }
    val compassReading by sensorManager.compassFlow.collectAsStateWithLifecycle()

    var wasAligned by remember { mutableStateOf(false) }

    DisposableEffect(prayerSettings.latitude, prayerSettings.longitude) {
        sensorManager.updateLocation(prayerSettings.latitude, prayerSettings.longitude)
        sensorManager.startListening()
        onDispose {
            sensorManager.stopListening()
        }
    }

    val qiblaBearing = remember(prayerSettings.latitude, prayerSettings.longitude) {
        QiblaCalculator.calculateQiblaBearing(prayerSettings.latitude, prayerSettings.longitude)
    }

    val distanceKm = remember(prayerSettings.latitude, prayerSettings.longitude) {
        QiblaCalculator.calculateDistanceToKaabaKm(prayerSettings.latitude, prayerSettings.longitude).roundToInt()
    }

    val currentHeading = compassReading.azimuthDegrees
    // Qibla direction relative to the top of the phone screen
    val relativeQiblaAngle = ((qiblaBearing - currentHeading + 360) % 360).toFloat()

    val isAligned = remember(currentHeading, qiblaBearing) {
        QiblaCalculator.isAligned(currentHeading, qiblaBearing, toleranceDegrees = 3.0f)
    }

    // Trigger haptic milestone vibration on first alignment
    LaunchedEffect(isAligned) {
        if (isAligned && !wasAligned) {
            HapticFeedbackUtil.performMilestoneDoublePulse(context)
        }
        wasAligned = isAligned
    }

    // Dial & Needle animated rotation
    val animatedCompassRotation by animateFloatAsState(
        targetValue = -currentHeading,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "compass_rot"
    )

    val animatedGlowColor by animateColorAsState(
        targetValue = if (isAligned) SpiritualEmeraldBright else Color(0x3310B981),
        animationSpec = tween(300),
        label = "qibla_glow"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top Header ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "الرجوع",
                    tint = appColors.textPrimary
                )
            }

            Text(
                text = "بوصلة القبلة الشريفة",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = appColors.textPrimary
                )
            )

            // Location Switcher Pill
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(Color(0x2210B981))
                    .border(1.dp, SpiritualEmeraldBright.copy(alpha = 0.4f), PillShape)
                    .clickable(onClick = onOpenCityPicker)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = SpiritualEmeraldBright,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = prayerSettings.cityName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SpiritualEmeraldBright,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // ── Alignment Status Indicator ────────────────────────────────────────
        Box(
            modifier = Modifier
                .clip(PillShape)
                .background(if (isAligned) Color(0x3510B981) else Color(0x18FFFFFF))
                .border(
                    1.dp,
                    if (isAligned) SpiritualEmeraldBright else Color(0x20FFFFFF),
                    PillShape
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isAligned) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SpiritualEmeraldBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "أنت مواجه للقبلة مباشرة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpiritualEmeraldBright
                    )
                } else {
                    val turnDegree = ((relativeQiblaAngle + 180) % 360 - 180).roundToInt()
                    val turnDirection = if (turnDegree > 0) "يميناً" else "يساراً"
                    Text(
                        text = "أدر هاتفك $turnDirection بمقدار ${kotlin.math.abs(turnDegree)}°",
                        fontSize = 13.sp,
                        color = appColors.textSecondary
                    )
                }
            }
        }

        // ── Main Compass Dial ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            animatedGlowColor.copy(alpha = if (isAligned) 0.25f else 0.08f),
                            Color(0xFF080D0A),
                            Color.Black
                        )
                    )
                )
                .border(
                    width = if (isAligned) 2.5.dp else 1.5.dp,
                    color = animatedGlowColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Compass Rose & Degree Marks
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(animatedCompassRotation)
            ) {
                drawCompassRose(qiblaBearing.toFloat(), isAligned)
            }

            // Fixed Screen Center Pointer Line (Points to phone top)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = Offset(center.x, 16.dp.toPx()),
                    end = Offset(center.x, 34.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Central Core: Heading & Kaaba Direction Degree
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0C140F))
                    .border(1.dp, animatedGlowColor.copy(alpha = 0.5f), CircleShape)
            ) {
                Text(
                    text = "${currentHeading.roundToInt()}°",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isAligned) SpiritualEmeraldBright else Color.White,
                        fontSize = 28.sp
                    )
                )
                Text(
                    text = headingToCompassDirection(currentHeading),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.textSecondary
                    )
                )
            }
        }

        // ── Bottom Info Card ──────────────────────────────────────────────────
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "زاوية القبلة من موقعك",
                        fontSize = 13.sp,
                        color = appColors.textSecondary
                    )
                    Text(
                        text = "${qiblaBearing.roundToInt()}° من الشمال الحقيقي",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpiritualEmeraldBright
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المسافة إلى الكعبة المشرفة",
                        fontSize = 13.sp,
                        color = appColors.textSecondary
                    )
                    Text(
                        text = "$distanceKm كم",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    )
                }

                if (compassReading.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "دقة الحساس منخفضة، حرك الهاتف على شكل رقم 8 لمعايرته",
                            fontSize = 11.sp,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

// ─── Canvas Dial Drawing ──────────────────────────────────────────────────────

private fun DrawScope.drawCompassRose(qiblaBearingDeg: Float, isAligned: Boolean) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = size.width / 2

    // Draw degree tick marks every 5 degrees
    for (i in 0 until 360 step 5) {
        val angleRad = Math.toRadians((i - 90).toDouble())
        val isMajor = (i % 30 == 0)
        val isCardinal = (i % 90 == 0)

        val tickLength = when {
            isCardinal -> 16.dp.toPx()
            isMajor -> 10.dp.toPx()
            else -> 5.dp.toPx()
        }

        val strokeColor = when {
            isCardinal && i == 0 -> Color(0xFFEF4444) // North is Crimson Red
            isCardinal -> Color.White.copy(alpha = 0.8f)
            isMajor -> Color.White.copy(alpha = 0.4f)
            else -> Color.White.copy(alpha = 0.15f)
        }

        val startX = (center.x + (radius - tickLength) * cos(angleRad)).toFloat()
        val startY = (center.y + (radius - tickLength) * sin(angleRad)).toFloat()
        val endX = (center.x + (radius - 2.dp.toPx()) * cos(angleRad)).toFloat()
        val endY = (center.y + (radius - 2.dp.toPx()) * sin(angleRad)).toFloat()

        drawLine(
            color = strokeColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (isCardinal) 2.dp.toPx() else 1.dp.toPx()
        )
    }

    // Draw Holy Kaaba indicator marker on the ring at exact bearing
    val qiblaRad = Math.toRadians((qiblaBearingDeg - 90).toDouble())
    val kaabaCenter = Offset(
        (center.x + (radius - 24.dp.toPx()) * cos(qiblaRad)).toFloat(),
        (center.y + (radius - 24.dp.toPx()) * sin(qiblaRad)).toFloat()
    )

    // Kaaba marker beacon
    drawCircle(
        color = if (isAligned) SpiritualEmeraldBright else Color(0xFF10B981),
        radius = 8.dp.toPx(),
        center = kaabaCenter
    )
    drawCircle(
        color = Color(0xFF0F172A),
        radius = 5.dp.toPx(),
        center = kaabaCenter
    )

    // Needle pointing from center towards Kaaba
    val needlePath = Path().apply {
        val tipX = (center.x + (radius - 36.dp.toPx()) * cos(qiblaRad)).toFloat()
        val tipY = (center.y + (radius - 36.dp.toPx()) * sin(qiblaRad)).toFloat()

        val baseAngle1 = qiblaRad + Math.toRadians(90.0)
        val baseAngle2 = qiblaRad - Math.toRadians(90.0)
        val baseRadius = 6.dp.toPx()

        moveTo(tipX, tipY)
        lineTo(
            (center.x + baseRadius * cos(baseAngle1)).toFloat(),
            (center.y + baseRadius * sin(baseAngle1)).toFloat()
        )
        lineTo(
            (center.x + baseRadius * cos(baseAngle2)).toFloat(),
            (center.y + baseRadius * sin(baseAngle2)).toFloat()
        )
        close()
    }

    drawPath(
        path = needlePath,
        color = if (isAligned) SpiritualEmeraldBright else Color(0xDD10B981)
    )
}

private fun headingToCompassDirection(heading: Float): String {
    val sector = (((heading + 22.5f) % 360) / 45f).toInt()
    return when (sector) {
        0 -> "شمال (N)"
        1 -> "شمال شرق (NE)"
        2 -> "شرق (E)"
        3 -> "جنوب شرق (SE)"
        4 -> "جنوب (S)"
        5 -> "جنوب غرب (SW)"
        6 -> "غرب (W)"
        7 -> "شمال غرب (NW)"
        else -> "شمال (N)"
    }
}
