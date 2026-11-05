package com.example.quran.tajweed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quran.book.QuranPaletteType
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeLarge
import com.example.ui.components.SquircleShapeMedium
import com.example.ui.components.SquircleShapeSmall
import com.example.util.HapticFeedbackUtil

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TajweedGuideDialog(
    palette: QuranPaletteType,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(TajweedSettings.isTajweedEnabled(context)) }
    var currentTheme by remember { mutableStateOf(TajweedSettings.getTajweedTheme(context)) }
    val customColors = remember {
        mutableStateMapOf<String, Color>().apply {
            putAll(TajweedSettings.getAllCustomColors(context))
        }
    }

    var colorPickerRule by remember { mutableStateOf<TajweedRule?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.pageColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = palette.accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "خريطة ومفتاح ألوان التجويد",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textColor
                        )
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = palette.textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Enable Tajweed Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SquircleShapeMedium)
                    .background(palette.borderColor.copy(alpha = 0.25f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تلوين أحكام التجويد في المصحف",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textColor
                        )
                    )
                    Text(
                        text = "تمييز مواضع المدود والغُنن والقلقلة والإخفاء والإدغام بألوان معتمدة",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = palette.textColor.copy(alpha = 0.65f),
                            fontSize = 11.sp
                        )
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        isEnabled = it
                        TajweedSettings.setTajweedEnabled(context, it)
                        HapticFeedbackUtil.performLightTick(context)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = palette.accentColor,
                        checkedTrackColor = palette.accentColor.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Palette Theme Selector
            Text(
                text = "نمط ألوان التجويد:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.accentColor
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TajweedTheme.entries.forEach { theme ->
                    val isSelected = theme == currentTheme
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(SquircleShapeSmall)
                            .background(if (isSelected) palette.accentColor.copy(alpha = 0.2f) else palette.borderColor.copy(alpha = 0.2f))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) palette.accentColor else palette.borderColor,
                                shape = SquircleShapeSmall
                            )
                            .clickable {
                                currentTheme = theme
                                TajweedSettings.setTajweedTheme(context, theme)
                                HapticFeedbackUtil.performLightTick(context)
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = theme.labelArabic.split(" ").first(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) palette.accentColor else palette.textColor,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rules Map List
            Text(
                text = "دليل الأحكام والمدود (انقر على اللون لتخصيصه):",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.accentColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TajweedRule.entries) { rule ->
                    val ruleColor = TajweedParser.getRuleColor(rule, currentTheme, customColors)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleShapeSmall)
                            .background(palette.borderColor.copy(alpha = 0.15f))
                            .border(1.dp, palette.borderColor.copy(alpha = 0.3f), SquircleShapeSmall)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Color Swatch with click-to-customize
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ruleColor)
                                .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                .clickable {
                                    colorPickerRule = rule
                                    HapticFeedbackUtil.performLightTick(context)
                                }
                        )

                        // Rule Details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rule.arabicName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textColor
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(ruleColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = rule.harakatDuration,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ruleColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            Text(
                                text = rule.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = palette.textColor.copy(alpha = 0.65f),
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            // Example verse with highlighted rule
                            Text(
                                text = "مثال: ${rule.exampleText}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ruleColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 12.sp,
                                    textDirection = TextDirection.Rtl
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reset Custom Colors Button
            if (customColors.isNotEmpty()) {
                OutlinedButton(
                    onClick = {
                        customColors.clear()
                        TajweedSettings.resetCustomColors(context)
                        HapticFeedbackUtil.performMechanicalClick(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.accentColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("استعادة الألوان الافتراضية لقواعد التجويد")
                }
            }
        }
    }

    // Quick Color Swatch Customizer Dialog
    if (colorPickerRule != null) {
        val targetRule = colorPickerRule!!
        val presetColors = listOf(
            Color(0xFFDC2626), Color(0xFFE11D48), Color(0xFF9333EA), Color(0xFF7C3AED),
            Color(0xFF2563EB), Color(0xFF0284C7), Color(0xFF0D9488), Color(0xFF16A34A),
            Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEA580C), Color(0xFF64748B)
        )

        AlertDialog(
            onDismissRequest = { colorPickerRule = null },
            confirmButton = {},
            title = {
                Text(
                    text = "تخصيص لون: ${targetRule.arabicName}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textDirection = TextDirection.Rtl
                    )
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "اختر الدرجة اللونية المفضلة لتمييز ${targetRule.arabicName} في المصحف:",
                        style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Rtl),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        presetColors.take(6).forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .clickable {
                                        customColors[targetRule.id] = col
                                        TajweedSettings.setCustomColor(context, targetRule.id, col)
                                        HapticFeedbackUtil.performLightTick(context)
                                        colorPickerRule = null
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        presetColors.drop(6).forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .clickable {
                                        customColors[targetRule.id] = col
                                        TajweedSettings.setCustomColor(context, targetRule.id, col)
                                        HapticFeedbackUtil.performLightTick(context)
                                        colorPickerRule = null
                                    }
                            )
                        }
                    }
                }
            }
        )
    }
}
