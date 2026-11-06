package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BookQuoteChunkUiModel
import com.example.ui.components.BookWisdomQuoteCard
import com.example.ui.components.SpecularGlassCard
import com.example.ui.components.appleSpringClickable
import com.example.ui.theme.LocalMuhasibColors
import com.example.ui.theme.MuhasibThemeMode

@Immutable
data class ReformTrackCardModel(
    val id: String,               // "SALAH", "WEED", "PORN", "SMOKING"
    val titleArabic: String,
    val subtitleArabic: String,
    val streakDays: Int,
    val streakHours: Int,
    val physiologicalStatus: String,
    val accentColor: Color,
    val primaryActionLabel: String
)

@Immutable
data class OpenSourceBookItemModel(
    val id: String,
    val titleArabic: String,
    val authorArabic: String,
    val targetHealingTag: String, // مثلاً: "علاج الإباحية والشهوة"، "أسرار الخشوع في الصلاة"
    val totalPages: Int,
    val currentBookmarkPage: Int,
    val pagesReadToday: Int,
    val dailyGoalPages: Int = 2,
    val isDownloaded: Boolean,
    val isDownloading: Boolean = false,
    val isEnabledForNotifications: Boolean = false
)

@Composable
fun SalehReformHubScreen(
    muhasabahScorePercent: Int,
    nextSalahName: String,
    nextSalahCountdown: String,
    fardCompletedToday: Int,
    activeQuoteChunk: BookQuoteChunkUiModel,
    reformTracks: List<ReformTrackCardModel>,
    openSourceBooks: List<OpenSourceBookItemModel>,
    currentThemeMode: MuhasibThemeMode,
    onToggleThemeMode: () -> Unit,
    onTriggerEmergencyPanicSheet: (String) -> Unit,
    onMarkQuoteChunkRead: (String) -> Unit,
    onOpenBookReader: (String, Int) -> Unit,
    onTogglePinQuoteToAod: (String) -> Unit,
    onOneClickDownloadBook: (String) -> Unit,
    onToggleBookNotificationWird: (String) -> Unit
) {
    val colors = LocalMuhasibColors.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. الرأس المينيمالي: شعار المحراب + مبدل الوضع الليلي/النهاري + زر الطوارئ
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مِحْرَابُ الصَّالِح",
                        color = colors.primaryInk,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "طريق العبودية • الصلاة • العلم • التزكية",
                        color = colors.secondaryInk,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // زر التبديل الفوري بين AMOLED Dark و Parchment Light و Solar Auto
                    Surface(
                        modifier = Modifier.appleSpringClickable(onClick = onToggleThemeMode),
                        color = colors.elevatedPill,
                        shape = CircleShape
                    ) {
                        val modeLabel = when (currentThemeMode) {
                            MuhasibThemeMode.AMOLED_DARK -> "🌙 ليل AMOLED"
                            MuhasibThemeMode.PARCHMENT_LIGHT -> "☀️ ورق المصحف"
                            MuhasibThemeMode.SOLAR_AUTO -> "🧭 فلكي تلقائي"
                        }
                        Text(
                            text = modeLabel,
                            color = colors.primaryInk,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }

                    // زر النجدة الفوري (يكسر موجة الحشيش/الإباحية/التدخين عبر PanicSheet.kt)
                    Surface(
                        modifier = Modifier.appleSpringClickable { onTriggerEmergencyPanicSheet("GENERAL") },
                        color = colors.dangerAlert.copy(alpha = 0.16f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "🛡️ طوارئ النفس",
                            color = colors.dangerAlert,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }

        // 2. بطاقة الميزان اليومي وحراسة الصلاة القادمة (Salah & Muhasabah Cockpit)
        item {
            SpecularGlassCard(
                accentGlowColor = colors.salahGuardAccent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "الصلاة القادمة: $nextSalahName",
                            color = colors.salahGuardAccent,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nextSalahCountdown,
                            color = colors.primaryInk,
                            style = androidx.compose.material3.MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "الفرائض المؤداة اليوم: $fardCompletedToday / 5 • القفل المقدس مفعل عند الأذان",
                            color = colors.secondaryInk,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                    }

                    // حلقة مؤشر المحاسبة اليومي (0.0% Idle GPU Cached Ring)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(84.dp)
                            .drawWithCache {
                                val stroke = 7.dp.toPx()
                                val sweep = (muhasabahScorePercent / 100f) * 360f
                                onDrawBehind {
                                    drawArc(
                                        color = colors.divider,
                                        startAngle = 0f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = stroke)
                                    )
                                    drawArc(
                                        color = colors.accent,
                                        startAngle = -90f,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                                    )
                                }
                            }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$muhasabahScorePercent%",
                                color = colors.primaryInk,
                                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "المُحاسبة",
                                color = colors.mutedInk,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. بطاقة الاقتباس الحي ووِرد الصفحتين اليومي (تعمل بتزامن كامل مع الإشعارات والـ AOD)
        item {
            BookWisdomQuoteCard(
                chunk = activeQuoteChunk,
                onMarkChunkRead = onMarkQuoteChunkRead,
                onOpenInBookReader = onOpenBookReader,
                onTogglePinToAod = onTogglePinQuoteToAod
            )
        }

        // 4. جبهات المجاهدة الأربع (الصلاة • ترك الحشيش • ترك الإباحية • ترك التدخين)
        item {
            Text(
                text = "جَبَهَاتُ التَّزْكِيَةِ وَالمُجَاهَدَةِ الأَرْبَع",
                color = colors.secondaryInk,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                reformTracks.chunked(2).forEach { rowTracks ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowTracks.forEach { track ->
                            SpecularGlassCard(
                                modifier = Modifier.weight(1f),
                                cornerRadius = 22.dp,
                                accentGlowColor = track.accentColor,
                                onClick = { onTriggerEmergencyPanicSheet(track.id) }
                            ) {
                                Text(
                                    text = track.titleArabic,
                                    color = track.accentColor,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${track.streakDays}",
                                        color = colors.primaryInk,
                                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "يوم (${track.streakHours} س)",
                                        color = colors.secondaryInk,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = track.physiologicalStatus,
                                    color = colors.secondaryInk,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = track.accentColor.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = track.primaryActionLabel,
                                        color = track.accentColor,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. خزانة أمهات الكتب الإسلامية المفتوحة المصدر (1-Click Offline Download & 2-Page/Day Notification Sync)
        item {
            Column {
                Text(
                    text = "خِزَانَةُ الكُتُبِ المَفْتُوحَةِ المَصْدَر (تحميل بضغطة واحدة)",
                    color = colors.primaryInk,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "اختر كتبك ليُرسلها الهاتف لك مقسّمة عبر الإشعارات (صفحتان يومياً على الأقل لكل كتاب) مع حفظ موضع القراءة تلقائياً",
                    color = colors.secondaryInk,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
            }
        }

        items(openSourceBooks, key = { it.id }) { book ->
            SpecularGlassCard(
                cornerRadius = 22.dp,
                accentGlowColor = if (book.isEnabledForNotifications) colors.accent else null
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = colors.elevatedPill,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = book.targetHealingTag,
                                color = colors.accent,
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = book.titleArabic,
                            color = colors.primaryInk,
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${book.authorArabic} • ${book.totalPages} صفحة",
                            color = colors.secondaryInk,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                    }

                    // زر التحميل بضغطة واحدة أو فتح الكتاب عند العلامة المرجعية
                    if (!book.isDownloaded) {
                        Surface(
                            modifier = Modifier.appleSpringClickable { onOneClickDownloadBook(book.id) },
                            color = colors.accent,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (book.isDownloading) "جاري التحميل..." else "⬇ تحميل مباشر",
                                color = if (colors.isDark) Color.Black else Color.White,
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    } else {
                        Surface(
                            modifier = Modifier.appleSpringClickable {
                                onOpenBookReader(book.id, book.currentBookmarkPage)
                            },
                            color = colors.elevatedPill,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "📖 ص ${book.currentBookmarkPage}",
                                color = colors.primaryInk,
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                if (book.isDownloaded) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إنجاز اليوم: ${book.pagesReadToday} / ${book.dailyGoalPages} صفحة",
                            color = colors.secondaryInk,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                        )

                        Surface(
                            modifier = Modifier.appleSpringClickable {
                                onToggleBookNotificationWird(book.id)
                            },
                            color = if (book.isEnabledForNotifications) colors.accentSoftGlow else colors.elevatedPill,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (book.isEnabledForNotifications) {
                                    "🔔 إشعارات الوِرد مفعلة (5 شذرات/يوم)"
                                } else {
                                    "🔕 تفعيل القراءة عبر الإشعارات"
                                },
                                color = if (book.isEnabledForNotifications) colors.accent else colors.secondaryInk,
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SalehReformHubScreen(
    viewModel: com.example.ui.viewmodel.MuhasibViewModel,
    modifier: Modifier = Modifier
) {
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()

    BooksLibraryScreen(
        books = allBooks,
        onSelectActiveWirdBook = { bookId -> viewModel.setActiveReadingBook(bookId) },
        viewModel = viewModel,
        modifier = modifier
    )
}

