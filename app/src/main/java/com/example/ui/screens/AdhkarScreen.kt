package com.example.ui.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aod.AodManager
import com.example.data.model.DhikrCategory
import com.example.data.model.DhikrItem
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeLarge
import com.example.ui.components.SquircleShapeMedium
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.SpiritualEmerald
import com.example.ui.theme.SpiritualEmeraldBright
import com.example.ui.theme.SpiritualEmeraldSoft
import com.example.util.HapticFeedbackUtil

@Composable
fun AdhkarScreen(
    dhikrList: List<DhikrItem>,
    selectedCategory: DhikrCategory,
    onCategorySelected: (DhikrCategory) -> Unit,
    onIncrementDhikr: (Int) -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val categoryScrollState = rememberScrollState()

    val filteredList = remember(dhikrList, selectedCategory) {
        dhikrList.filter { it.category == selectedCategory.name }
    }

    val completedInCat = filteredList.count { it.isCompleted }
    val totalInCat = filteredList.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "حِصْنُ المُسْلِم",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = appColors.textPrimary,
                        fontSize = 28.sp,
                        letterSpacing = (-0.5).sp
                    )
                )
                Text(
                    text = "الأذكار المأثورة الصحيحة بالسند والأجر",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SpiritualEmeraldBright,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
            }

            // Reset Today's Counts Button
            IconButton(
                onClick = {
                    HapticFeedbackUtil.performLightTick(context)
                    onResetAll()
                },
                modifier = Modifier
                    .shadow(elevation = 4.dp, shape = CircleShape, ambientColor = appColors.shadow)
                    .clip(CircleShape)
                    .background(appColors.glassCardElevated)
                    .border(1.dp, Color(0x2810B981), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "تصفير الأذكار",
                    tint = appColors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // CATEGORY PILL SELECTOR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(categoryScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DhikrCategory.values().forEach { cat ->
                val isSelected = cat == selectedCategory
                val pillBg = if (isSelected) {
                    SpiritualEmeraldSoft
                } else {
                    appColors.glassCardElevated
                }
                val pillBorder = if (isSelected) SpiritualEmeraldBright else Color(0x1F10B981)

                Box(
                    modifier = Modifier
                        .testTag("dhikr_cat_${cat.name.lowercase()}")
                        .shadow(elevation = if (isSelected) 4.dp else 1.dp, shape = PillShape, ambientColor = appColors.shadow)
                        .clip(PillShape)
                        .background(pillBg)
                        .border(1.dp, pillBorder, PillShape)
                        .clickable {
                            HapticFeedbackUtil.performLightTick(context)
                            onCategorySelected(cat)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // ponytail: prevent vertical letter stacking on large accessibility font scales
                    Text(
                        text = cat.displayName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SpiritualEmeraldBright else appColors.textSecondary,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // CATEGORY COMPLETION PROGRESS BAR
        if (totalInCat > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = { if (totalInCat > 0) completedInCat.toFloat() / totalInCat else 0f },
                    modifier = Modifier
                        .weight(1f)
                        .height(7.dp)
                        .clip(CircleShape),
                    color = SpiritualEmeraldBright,
                    trackColor = appColors.inset,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "$completedInCat / $totalInCat",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SpiritualEmeraldBright,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // DHIKR CARDS LIST
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredList, key = { it.id }) { item ->
                DhikrCardItem(
                    item = item,
                    onIncrement = {
                        val next = item.currentCount + 1
                        val profile = AodManager.getMisbahaHapticProfile(context)
                        HapticFeedbackUtil.performMisbahaFeedback(context, profile, next, item.targetCount)
                        onIncrementDhikr(item.id)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun DhikrCardItem(
    item: DhikrItem,
    onIncrement: () -> Unit
) {
    val appColors = LocalAppColors.current
    val isDone = item.isCompleted

    var isPressed by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dhikr_card_scale"
    )

    AppleGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .pointerInput(isDone, item.id) {
                detectTapGestures(
                    onPress = {
                        if (!isDone) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    },
                    onTap = {
                        if (!isDone) {
                            onIncrement()
                        }
                    }
                )
            },
        shape = SquircleShapeLarge,
        elevation = 5.dp,
        accentGlow = if (isDone) SpiritualEmerald else null
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Arabic Text
            Text(
                text = item.arabic,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    lineHeight = 28.sp,
                    color = appColors.textPrimary,
                    textAlign = TextAlign.Start
                )
            )

            // Hadith Benefit / Virtue
            if (item.benefit.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleShapeMedium)
                        .background(appColors.inset)
                        .border(1.dp, Color(0x1810B981), SquircleShapeMedium)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = item.benefit,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = SpiritualEmeraldBright
                        )
                    )
                }
            }

            // Bottom Action Bar: Counter Button & Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Target Label
                Text(
                    text = "التكرار المطلوب: ${item.targetCount} مرة",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = appColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )

                // Tactile Tap Counter Button
                Box(
                    modifier = Modifier
                        .testTag("dhikr_counter_${item.id}")
                        .shadow(elevation = 4.dp, shape = PillShape, ambientColor = appColors.shadow)
                        .clip(PillShape)
                        .background(
                            if (isDone) SpiritualEmeraldBright else SpiritualEmeraldSoft
                        )
                        .border(
                            1.dp,
                            if (isDone) SpiritualEmeraldBright else SpiritualEmerald,
                            PillShape
                        )
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "اكتمل",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "مُكْتَمَل",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 12.sp
                                )
                            )
                        } else {
                            Text(
                                text = "${item.currentCount} / ${item.targetCount}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpiritualEmeraldBright,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdhkarScreen(
    viewModel: com.example.ui.viewmodel.MuhasibViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allDhikr by viewModel.allDhikr.collectAsStateWithLifecycle()

    AdhkarScreen(
        dhikrList = allDhikr,
        selectedCategory = uiState.selectedDhikrCategory,
        onCategorySelected = { viewModel.setSelectedDhikrCategory(it) },
        onIncrementDhikr = { viewModel.incrementDhikr(it) },
        onResetAll = { viewModel.resetAllDhikr() },
        modifier = modifier
    )
}
