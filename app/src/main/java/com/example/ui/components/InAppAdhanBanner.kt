package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpiritualAmber
import com.example.ui.theme.SpiritualEmerald
import com.example.ui.theme.SpiritualEmeraldBright
import com.example.ui.theme.SpiritualGoldBright

// ponytail: prominent high-contrast in-app controller banner for active Adhan alarm
@Composable
fun InAppAdhanBanner(
    visible: Boolean,
    prayerName: String,
    onStop: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .shadow(elevation = 12.dp, shape = SquircleShapeLarge, ambientColor = SpiritualEmerald)
                .clip(SquircleShapeLarge)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF06331E),
                            Color(0xFF031A0F)
                        )
                    )
                )
                .border(1.5.dp, SpiritualEmeraldBright.copy(alpha = 0.8f), SquircleShapeLarge)
                .padding(16.dp)
                .testTag("in_app_adhan_banner")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row with glowing icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SpiritualEmeraldBright.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "أذان نشط",
                            tint = SpiritualGoldBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "أذان صلاة $prayerName يصدح الآن 🕌",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                    )
                }

                // Action Buttons Row: Stop Adhan & Snooze 10 min
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onStop,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("adhan_stop_button"),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = SpiritualEmeraldBright)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "إيقاف الأذان ✓",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("adhan_snooze_button"),
                        shape = PillShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SpiritualAmber
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SpiritualAmber)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Snooze,
                                contentDescription = null,
                                tint = SpiritualAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "تذكير بعد 10 دقائق ⏱️",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpiritualAmber,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
