package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.prayer.PrayerPreferences
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.CityPickerDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aod.AodActivity
import com.example.data.model.MuhasabahScore
import com.example.data.model.PrayerLog
import com.example.data.model.SobrietyLog
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.ConcentricActivityRings
import com.example.ui.components.NeumorphicInsetBox
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeHero
import com.example.ui.components.SquircleShapeLarge
import com.example.ui.components.SquircleShapeMedium
import com.example.ui.components.SquircleShapeSmall
import com.example.ui.theme.AppleCrimson
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.RingDetoxColor
import com.example.ui.theme.RingDhikrColor
import com.example.ui.theme.RingPrayerColor
import com.example.ui.theme.SpiritualAmber
import com.example.ui.theme.SpiritualCyan
import com.example.ui.theme.SpiritualCyanBright
import com.example.ui.theme.SpiritualEmerald
import com.example.ui.theme.SpiritualEmeraldBright
import com.example.ui.theme.SpiritualGold
import com.example.ui.theme.SpiritualGoldBright
import com.example.util.HapticFeedbackUtil
import com.example.util.IslamicCalendarUtil

@Composable
fun DashboardScreen(
    prayerLog: PrayerLog?,
    sobrietyLog: SobrietyLog?,
    muhasabahHistory: List<MuhasabahScore>,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onTogglePrayer: (String, Boolean) -> Unit,
    onToggleWorkout: (Boolean) -> Unit,
    onToggleColdShower: (Boolean) -> Unit,
    onRecordRelapse: () -> Unit,
    onTriggerPanic: () -> Unit,
    onTestAlarm: () -> Unit,
    onOpenSettings: () -> Unit = {},  // ponytail: settings gear navigation
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val scrollState = rememberScrollState()

    val hijriDateString = remember { IslamicCalendarUtil.getFormattedHijriDate() }
    val gregorianDateString = remember { IslamicCalendarUtil.getFormattedArabicGregorianDate() }

    val completedPrayers = prayerLog?.completedCount() ?: 0
    val prayerRatio = (completedPrayers / 5f).coerceIn(0f, 1f)

    val istighfarToday = sobrietyLog?.istighfarTodayCount ?: 0
    val tasbi7Today = sobrietyLog?.tasbi7TodayCount ?: 0
    val dhikrRatio = (((istighfarToday + tasbi7Today).toFloat()) / 200f).coerceIn(0f, 1f)

    val cleanDays = sobrietyLog?.cleanDaysCount ?: 0
    val workoutDone = sobrietyLog?.workoutCompletedToday ?: false
    val coldShowerDone = sobrietyLog?.coldShowerToday ?: false

    var detoxScore = 0f
    if (cleanDays > 0) detoxScore += 0.4f
    if (workoutDone) detoxScore += 0.3f
    if (coldShowerDone) detoxScore += 0.3f

    // Composite Muhasabah Tier
    val overallScore = ((prayerRatio * 0.45f + dhikrRatio * 0.25f + detoxScore * 0.30f) * 100).toInt()
    val tierTitle = when {
        overallScore >= 85 -> "المُحْسِن"
        overallScore >= 60 -> "المُقْتَصِد"
        else -> "المُجَاهِد"
    }

    var showRelapseConfirm by remember { mutableStateOf(false) }
    var showQiblaScreen by remember { mutableStateOf(false) }
    var showCityPicker by remember { mutableStateOf(false) }
    val prayerSettings by PrayerPreferences.settingsFlow.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .padding(top = 10.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP BAR: Elegant Header & Mode Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "المُحَاسِب",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = appColors.textPrimary,
                        fontSize = 30.sp,
                        letterSpacing = (-0.5).sp
                    )
                )
                Text(
                    text = "$hijriDateString • $gregorianDateString",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SpiritualEmeraldBright,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
            }

            // ponytail: settings + theme toggle buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Settings gear
                IconButton(
                    onClick = {
                        HapticFeedbackUtil.performLightTick(context)
                        onOpenSettings()
                    },
                    modifier = Modifier
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            ambientColor = appColors.shadow,
                            spotColor = Color(0x80000000)
                        )
                        .clip(CircleShape)
                        .background(appColors.glassCardElevated)
                        .border(1.dp, Color(0x2810B981), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "الإعدادات",
                        tint = SpiritualEmeraldBright,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Neumorphic Theme Toggle Button
                IconButton(
                    onClick = {
                        HapticFeedbackUtil.performLightTick(context)
                        onToggleTheme()
                    },
                    modifier = Modifier
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            ambientColor = appColors.shadow,
                            spotColor = Color(0x80000000)
                        )
                        .clip(CircleShape)
                        .background(appColors.glassCardElevated)
                        .border(1.dp, Color(0x2810B981), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = if (isDarkMode) "الوضع الفاتح" else "الوضع الداكن",
                        tint = SpiritualEmeraldBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // NEXT SALAH LIVE COUNTDOWN CARD
        val nextPrayer = remember { IslamicCalendarUtil.getNextPrayer() }
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge,
            elevation = 4.dp,
            accentGlow = SpiritualEmerald
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x2210B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mosque,
                            contentDescription = "الصلاة القادمة",
                            tint = SpiritualEmeraldBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "الصلاة القادمة: صلاة ${nextPrayer.arabicName}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary
                            )
                        )
                        Text(
                            text = "الأذان بحول الله: ${nextPrayer.timeFormatted}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = appColors.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(Color(0x2810B981))
                        .border(1.dp, SpiritualEmeraldBright.copy(alpha = 0.5f), PillShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "بعد ${nextPrayer.timeRemainingFormatted}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpiritualEmeraldBright
                        )
                    )
                }
            }
        }

        // QIBLA COMPASS & CITY LOCATION ACTION ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Qibla Compass Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(SquircleShapeMedium)
                    .background(Color(0x1810B981))
                    .border(1.dp, SpiritualEmeraldBright.copy(alpha = 0.4f), SquircleShapeMedium)
                    .clickable {
                        HapticFeedbackUtil.performLightTick(context)
                        showQiblaScreen = true
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "اتجاه القبلة",
                        tint = SpiritualEmeraldBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "بوصلة القبلة",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpiritualEmeraldBright,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // City / Location Settings Button
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .clip(SquircleShapeMedium)
                    .background(appColors.glassCard)
                    .border(1.dp, appColors.glassBorder, SquircleShapeMedium)
                    .clickable {
                        HapticFeedbackUtil.performLightTick(context)
                        showCityPicker = true
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "تغيير المدينة",
                        tint = appColors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${prayerSettings.cityName} • تغيير",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = appColors.textPrimary,
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        // ponytail: Salat-centric contextual dua/guidance window
        val prayerSchedule = remember { IslamicCalendarUtil.getDailyPrayerSchedule() }
        val nextPrayerInfo = remember { IslamicCalendarUtil.getNextPrayer() }
        val contextualContent = remember(nextPrayerInfo.key) {
            getSalatContextualContent(nextPrayerInfo.key, nextPrayerInfo.isPassed)
        }
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge,
            elevation = 5.dp,
            accentGlow = SpiritualEmerald
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SpiritualEmeraldBright.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contextualContent.first,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = contextualContent.second,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpiritualEmeraldBright
                        )
                    )
                }
                Text(
                    text = contextualContent.third,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = appColors.textPrimary,
                        lineHeight = 18.sp
                    )
                )
            }
        }

        // JUMU'AH MODE SPECIAL BADGE
        val isFriday = remember { IslamicCalendarUtil.isJumuah() }
        if (isFriday) {
            AppleGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("jumuah_reminder_badge"),
                shape = SquircleShapeLarge,
                elevation = 6.dp,
                accentGlow = SpiritualGold
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
                            .background(Color(0x2810B981))
                            .border(1.dp, SpiritualEmeraldBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mosque,
                            contentDescription = "يوم الجمعة",
                            tint = SpiritualGoldBright,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "يَوْمُ الجُمُعَةِ المُبَارَك",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SpiritualGoldBright
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(SpiritualEmeraldBright.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "سُنَّةٌ مُؤَكَّدَة",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SpiritualEmeraldBright
                                    )
                                )
                            }
                        }

                        Text(
                            text = "لا تنس قراءة سورة الكهف، والإكثار من الصلاة والسلام على النبي ﷺ، وتحري ساعة الاستجابة 🕌✨",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = appColors.textPrimary,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }
        }

        // HERO CARD: 3 Distinct High-Contrast Concentric Rings (Emerald / Cyan / Amber)
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeHero,
            elevation = 8.dp,
            accentGlow = SpiritualEmerald
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Concentric Activity Rings (Outer Emerald, Middle Cyan, Inner Amber)
                ConcentricActivityRings(
                    prayerProgress = prayerRatio,
                    dhikrProgress = dhikrRatio,
                    detoxProgress = detoxScore,
                    sizeDp = 150.dp
                )

                // 3 Distinct Ring Legends
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Outer Ring: Salah (Luminous Emerald)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(RingPrayerColor)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "الصلوات الخمس",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Text(
                                text = "$completedPrayers من 5 صلوات",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = RingPrayerColor
                                )
                            )
                        }
                    }

                    // 2. Middle Ring: Dhikr (Celestial Cyan)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(RingDhikrColor)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "الذِّكْرُ وَالِاسْتِغْفَار",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Text(
                                text = "$tasbi7Today تسبيح • $istighfarToday استغفار",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = RingDhikrColor
                                )
                            )
                        }
                    }

                    // 3. Inner Ring: Detox / Sobriety (Warm Amber Gold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(RingDetoxColor)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "حِصْنُ الطَّهَارَة",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Text(
                                text = "$cleanDays أيام ثبات ونقاء",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = RingDetoxColor
                                )
                            )
                        }
                    }
                }
            }
        }

        // DAILY MUHASABAH SCORECARD (Feature Addition for Task 5)
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge,
            elevation = 5.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x2210B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "رتبة المحاسبة",
                            tint = SpiritualEmeraldBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "مَقَامُ اليَوْم: «$tierTitle»",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary
                            )
                        )
                        Text(
                            text = "«حَاسِبُوا أَنْفُسَكُمْ قَبْلَ أَنْ تُحَاسَبُوا»",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = appColors.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(Color(0x2610B981))
                        .border(1.dp, SpiritualEmeraldBright.copy(alpha = 0.5f), PillShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$overallScore / 100",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = SpiritualEmeraldBright
                        )
                    )
                }
            }
        }

        // SALAH TRACKER MATRIX (Tactile Neumorphic Inset / Extruded Chips)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "فَرَائِضُ اليَوْم",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    )
                )
                Text(
                    text = "الصلاة عماد الدين",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SpiritualEmeraldBright,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            val prayers = listOf(
                Triple("Fajr", "الفجر", prayerLog?.fajrDone ?: false),
                Triple("Dhuhr", "الظهر", prayerLog?.dhuhrDone ?: false),
                Triple("Asr", "العصر", prayerLog?.asrDone ?: false),
                Triple("Maghrib", "المغرب", prayerLog?.maghribDone ?: false),
                Triple("Isha", "العشاء", prayerLog?.ishaDone ?: false)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                prayers.forEach { (key, arabicName, isDone) ->
                    val chipBg = if (isDone) {
                        Brush.verticalGradient(
                            listOf(Color(0xFF1B3D2B), Color(0xFF122C1F))
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(appColors.inset, appColors.inset)
                        )
                    }
                    val borderColor = if (isDone) SpiritualEmeraldBright else Color(0x1F10B981)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(
                                elevation = if (isDone) 4.dp else 0.dp,
                                shape = SquircleShapeMedium,
                                ambientColor = appColors.shadow
                            )
                            .clip(SquircleShapeMedium)
                            .background(chipBg)
                            .border(1.dp, borderColor, SquircleShapeMedium)
                            .clickable {
                                HapticFeedbackUtil.performLightTick(context)
                                onTogglePrayer(key, !isDone)
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = arabicName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDone) SpiritualEmeraldBright else appColors.textPrimary,
                                    fontSize = 12.sp
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDone) SpiritualEmerald else Color(0x12FFFFFF)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "تمت الصلاة",
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // PHYSICAL & NEUROLOGICAL RECOVERY CARD
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 6.dp,
            accentGlow = SpiritualAmber
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SpiritualAmber.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = "حصن التعافي",
                                tint = SpiritualAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "حِصْنُ التَّعَافِي وَالتَّطْهِير",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Text(
                                text = "إعادة ضبط مستقبلات الدوبامين والطهارة البدنية",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = appColors.textSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Clean Days Pill
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(SpiritualAmber.copy(alpha = 0.15f))
                            .border(1.dp, SpiritualAmber.copy(alpha = 0.4f), PillShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$cleanDays يوم",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpiritualAmber
                            )
                        )
                    }
                }

                // Checkboxes for Workout & Cold Shower
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Cardio Workout Box
                    NeumorphicInsetBox(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            HapticFeedbackUtil.performLightTick(context)
                            onToggleWorkout(!workoutDone)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FitnessCenter,
                                contentDescription = "تمارين رياضية",
                                tint = if (workoutDone) SpiritualEmeraldBright else appColors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "تمرين وتعرّق 45د",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (workoutDone) SpiritualEmeraldBright else appColors.textPrimary
                                    )
                                )
                                Text(
                                    text = if (workoutDone) "مكتمل ✓" else "طرد السموم",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = appColors.textSecondary
                                    )
                                )
                            }
                        }
                    }

                    // Cold Shower Box
                    NeumorphicInsetBox(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            HapticFeedbackUtil.performLightTick(context)
                            onToggleColdShower(!coldShowerDone)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WaterDrop,
                                contentDescription = "دش بارد",
                                tint = if (coldShowerDone) SpiritualCyanBright else appColors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "استحمام بالماء البارد",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (coldShowerDone) SpiritualCyanBright else appColors.textPrimary
                                    )
                                )
                                Text(
                                    text = if (coldShowerDone) "مكتمل ✓" else "يقظة الدوبامين",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = appColors.textSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // EMERGENCY CRAVING PANIC & ACTION BUTTONS
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Big Apple-style Crimson Panic Button
            Button(
                onClick = {
                    HapticFeedbackUtil.performSevereAlarmVibration(context)
                    onTriggerPanic()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(elevation = 6.dp, shape = SquircleShapeLarge, ambientColor = Color(0x60EF4444))
                    .testTag("panic_craving_button"),
                shape = SquircleShapeLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppleCrimson
                )
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
                        text = "زِرُّ النَّجْدَةِ وَالاسْتِعَاذَةِ (عِنْدَ هُجُومِ الشَّهْوَةِ)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            // Secondary Action Row: Test Immediate Alarm & Launch AOD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Test Alarm
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(SquircleShapeMedium)
                        .background(appColors.glassCardElevated)
                        .border(1.dp, Color(0x28EF4444), SquircleShapeMedium)
                        .clickable { onTestAlarm() }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "تجربة إنذار الصلاة 🚨",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = appColors.crimson
                        )
                    )
                }

                // Instant AOD Launch Preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(SquircleShapeMedium)
                        .background(appColors.glassCardElevated)
                        .border(1.dp, Color(0x2810B981), SquircleShapeMedium)
                        .clickable {
                            val aodIntent = Intent(context, AodActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(aodIntent)
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "معاينة شاشة AOD الآن 🌙",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpiritualEmeraldBright
                        )
                    )
                }
            }
        }

        // RELAPSE LOG BUTTON
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (!showRelapseConfirm) {
                Text(
                    text = "تسجيل انتكاسة لا قدر الله (إعادة العداد)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = appColors.textTertiary,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .clickable { showRelapseConfirm = true }
                        .padding(8.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .clip(PillShape)
                        .background(AppleCrimson.copy(alpha = 0.15f))
                        .border(1.dp, AppleCrimson.copy(alpha = 0.5f), PillShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "هل تأكد الانتكاسة؟",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppleCrimson
                        )
                    )
                    Text(
                        text = "نعم (توبة نصوح)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .clip(PillShape)
                            .background(AppleCrimson)
                            .clickable {
                                onRecordRelapse()
                                showRelapseConfirm = false
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                    Text(
                        text = "إلغاء",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = appColors.textSecondary
                        ),
                        modifier = Modifier
                            .clickable { showRelapseConfirm = false }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    if (showQiblaScreen) {
        Dialog(
            onDismissRequest = { showQiblaScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            QiblaScreen(
                onClose = { showQiblaScreen = false },
                onOpenCityPicker = {
                    showQiblaScreen = false
                    showCityPicker = true
                }
            )
        }
    }

    if (showCityPicker) {
        CityPickerDialog(
            currentCityId = prayerSettings.cityId,
            onSelectCity = { showCityPicker = false },
            onDismiss = { showCityPicker = false }
        )
    }
}

// ponytail: contextual salat-aware dua/guidance content (emoji, title, body)
private fun getSalatContextualContent(nextPrayerKey: String, isPassed: Boolean): Triple<String, String, String> {
    return when (nextPrayerKey) {
        "Fajr" -> Triple(
            "🌙",
            "وقت القيام والتهجد",
            "قُمِ اللَّيْلَ إِلَّا قَلِيلًا ۞ هذا وقت السحر والاستغفار. أكثِر من الدعاء فإن الله ينزل إلى السماء الدنيا في الثلث الأخير من الليل.\n\n«اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا وَرِزْقًا طَيِّبًا وَعَمَلًا مُتَقَبَّلًا»"
        )
        "Dhuhr" -> Triple(
            "☀️",
            "أذكار الصباح وانتظار الظهر",
            "بدأ يومك بذكر الله. هذا وقت أذكار الصباح والعمل الصالح.\n\n«اللَّهُمَّ بِكَ أَصْبَحْنَا وَبِكَ أَمْسَيْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُور»\n\nلا تنس صلاة الضحى — ركعتان تعدل صدقة عن كل مفصل."
        )
        "Asr" -> Triple(
            "🕌",
            "ما بين الظهر والعصر",
            "صلِّ الظهر وأكثِر من الصلاة على النبي ﷺ. هذا وقت مبارك للدعاء.\n\n«اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّد»\n\nاحرص على صلاة السنة الراتبة: أربع ركعات قبل الظهر وركعتان بعدها."
        )
        "Maghrib" -> Triple(
            "🌅",
            "أذكار المساء وانتظار المغرب",
            "حان وقت أذكار المساء. احرص على قراءة آية الكرسي والمعوذات.\n\n«أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ»\n\nصلاة العصر هي الصلاة الوسطى — حافظ عليها خاصة."
        )
        "Isha" -> Triple(
            "🌇",
            "بين المغرب والعشاء",
            "هذا وقت مبارك بين الأذانين. ادعُ الله فإن الدعاء بين الأذان والإقامة لا يُردّ.\n\n«اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ»\n\nصلِّ ركعتين بين المغرب والعشاء — هي من التطوع المستحب."
        )
        else -> Triple(
            "🤲",
            "وقت الذكر والدعاء",
            "اذكر الله في كل حال. سبحان الله والحمد لله ولا إله إلا الله والله أكبر.\n\n«رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّار»"
        )
    }
}
