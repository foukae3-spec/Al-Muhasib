package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MuhasibApplication
import com.example.data.model.BookChunk
import com.example.data.model.IslamicBook
import com.example.data.seed.IslamicBooksSeedData
import com.example.library.BookDownloadStatus
import com.example.library.BookWirdScheduler
import com.example.library.OpenSourceBookCatalog
import com.example.library.OpenSourceBookRepository
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.BookQuoteChunkUiModel
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeLarge
import com.example.ui.components.SquircleShapeMedium
import com.example.ui.components.appleSpringClickable
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.LocalMuhasibColors
import com.example.ui.theme.MuhasibThemeMode
import com.example.ui.theme.SpiritualAmber
import com.example.ui.theme.SpiritualCyan
import com.example.ui.theme.SpiritualEmerald
import com.example.ui.theme.SpiritualEmeraldBright
import com.example.ui.theme.SpiritualGold
import com.example.ui.theme.SpiritualGoldBright
import com.example.ui.viewmodel.MuhasibViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.IslamicCalendarUtil
import com.example.util.QuotePreferences
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalTime

enum class BookCategoryTab(val catId: String, val titleArabic: String) {
    ADDICTION(IslamicBooksSeedData.CAT_ADDICTION, "كسر الإدمان والشهوات"),
    SALAH(IslamicBooksSeedData.CAT_SALAH, "إقامة الصلاة وحفظها"),
    TAZKIYAH(IslamicBooksSeedData.CAT_TAZKIYAH, "تزكية الروح وصناعة العابد")
}

@Composable
fun BooksLibraryScreen(
    books: List<IslamicBook>,
    onSelectActiveWirdBook: (String) -> Unit,
    viewModel: MuhasibViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val muhasibColors = LocalMuhasibColors.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as MuhasibApplication
    val repository = app.repository

    var showMihrabHub by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var activeReadingBook by remember { mutableStateOf<IslamicBook?>(null) }
    var activeBookId by remember { mutableStateOf(QuotePreferences.getActiveBookId(context)) }
    var quoteUpdateTrigger by remember { mutableIntStateOf(0) }

    // Live states from viewModel
    val uiState = viewModel?.uiState?.collectAsStateWithLifecycle()?.value
    val todayPrayerLog = viewModel?.todayPrayerLog?.collectAsStateWithLifecycle()?.value ?: com.example.data.model.PrayerLog(dateString = "")
    val sobrietyLog = viewModel?.sobrietyState?.collectAsStateWithLifecycle()?.value ?: com.example.data.model.SobrietyLog()
    val recoveryTracks = viewModel?.recoveryTracks?.collectAsStateWithLifecycle()?.value ?: emptyList()
    val muhasabahHistory = viewModel?.muhasabahHistory?.collectAsStateWithLifecycle()?.value ?: emptyList()

    // 1. Muhasabah calculation
    val fardCount = todayPrayerLog.completedCount()
    val muhasabahScore = muhasabahHistory.firstOrNull()?.scorePercentage ?: (fardCount * 20)

    // 2. Next Prayer calculation
    val nextPrayer = remember(quoteUpdateTrigger) { IslamicCalendarUtil.getNextPrayer() }
    val now = remember(quoteUpdateTrigger) { LocalTime.now() }
    val diffMinutes = remember(now, nextPrayer) {
        val diff = Duration.between(now, nextPrayer.time).toMinutes()
        if (diff < 0) diff + 1440 else diff
    }
    val nextSalahCountdown = "${diffMinutes / 60}س ${diffMinutes % 60}د"

    // 3. Active Quote Chunk UI Model
    val pinned = remember(quoteUpdateTrigger) { QuotePreferences.getPinnedQuote(context) }
    val activeQuoteChunk = remember(pinned) {
        BookQuoteChunkUiModel(
            bookId = pinned.bookId,
            bookTitle = pinned.bookTitle,
            authorName = pinned.author,
            chapterTitle = pinned.title,
            excerptText = pinned.content,
            pageNumber = pinned.pageNumber,
            totalBookPages = 180,
            dailyChunkIndex = pinned.dailyChunkIndex,
            dailyTargetChunks = pinned.dailyTargetChunks,
            isPinnedToAod = true
        )
    }

    // 4. Reform Tracks Cards
    val cleanDays = sobrietyLog.cleanDaysCount
    val streakSalah = (recoveryTracks.find { it.trackType == "SALAH" }?.cleanStreakSeconds ?: (cleanDays.toLong() * 86400L)) / 86400L
    val streakWeed = (recoveryTracks.find { it.trackType == "CANNABIS" }?.cleanStreakSeconds ?: (cleanDays.toLong() * 86400L)) / 86400L
    val streakPorn = (recoveryTracks.find { it.trackType == "PORNOGRAPHY" }?.cleanStreakSeconds ?: (cleanDays.toLong() * 86400L)) / 86400L
    val streakSmoking = (recoveryTracks.find { it.trackType == "SMOKING" }?.cleanStreakSeconds ?: (cleanDays.toLong() * 86400L)) / 86400L

    val reformTracksList = remember(recoveryTracks, sobrietyLog, muhasibColors) {
        listOf(
            ReformTrackCardModel(
                id = "SALAH",
                titleArabic = "حفظ الصلاة في وقتها",
                subtitleArabic = "القفل المقدس والخشوع",
                streakDays = maxOf(streakSalah.toInt(), 1),
                streakHours = 14,
                physiologicalStatus = "انتظام الإيقاع الحيوي وهدوء الجهاز العصبي بالصلوات الخمس",
                accentColor = muhasibColors.salahGuardAccent,
                primaryActionLabel = "حراسة الصلاة"
            ),
            ReformTrackCardModel(
                id = "WEED",
                titleArabic = "صفاء الذهن (ترك الحشيش)",
                subtitleArabic = "يقظة العقل وسلامة الصدر",
                streakDays = streakWeed.toInt(),
                streakHours = 8,
                physiologicalStatus = "استعادة مستقبلات CB1 الدماغية وتنظيم إفراز الدوبامين الطبيعي",
                accentColor = muhasibColors.clarityWeedAccent,
                primaryActionLabel = "سجل الثبات"
            ),
            ReformTrackCardModel(
                id = "PORN",
                titleArabic = "طهارة القلب (ترك الإباحية)",
                subtitleArabic = "غض البصر ونقاء السريرة",
                streakDays = streakPorn.toInt(),
                streakHours = 19,
                physiologicalStatus = "إعادة ضبط حساسية الدوبامين وتجديد مادة الدماغ الرمادية",
                accentColor = muhasibColors.purityPornAccent,
                primaryActionLabel = "درع الطهارة"
            ),
            ReformTrackCardModel(
                id = "SMOKING",
                titleArabic = "نقاء الأنفاس (ترك التدخين)",
                subtitleArabic = "حفظ البدن والصحة",
                streakDays = streakSmoking.toInt(),
                streakHours = 6,
                physiologicalStatus = "طرد أول أكسيد الكربون وتجديد الأهداب التنفسية وتوسيع الرئتين",
                accentColor = muhasibColors.breathSmokingAccent,
                primaryActionLabel = "تنفس الصعداء"
            )
        )
    }

    // 5. Open Source Books List
    val openSourceBooksList = remember(books) {
        OpenSourceBookCatalog.canonicalEntries.map { entry ->
            val bookEntity = books.find { it.id == entry.id || it.id == entry.canonicalAlias }
            val downloaded = bookEntity?.isDownloaded == true
            val cursor = bookEntity?.currentChunkCursor ?: 0
            OpenSourceBookItemModel(
                id = entry.id,
                titleArabic = entry.titleArabic,
                authorArabic = entry.authorArabic,
                targetHealingTag = entry.targetHealingTag,
                totalPages = entry.totalPages,
                currentBookmarkPage = (cursor / 2) + 1,
                pagesReadToday = (cursor % 5) / 2,
                dailyGoalPages = 2,
                isDownloaded = downloaded,
                isDownloading = false,
                isEnabledForNotifications = downloaded
            )
        }
    }

    val currentTab = BookCategoryTab.entries[selectedTabIndex]
    val filteredBooks = books.filter { it.category == currentTab.catId }

    val isDark = muhasibColors.isDark
    val hubBg = if (showMihrabHub) muhasibColors.accent else muhasibColors.elevatedPill
    val hubText = if (showMihrabHub) (if (isDark) Color.Black else Color.White) else muhasibColors.primaryInk
    val shelfBg = if (!showMihrabHub) muhasibColors.accent else muhasibColors.elevatedPill
    val shelfText = if (!showMihrabHub) (if (isDark) Color.Black else Color.White) else muhasibColors.primaryInk

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(muhasibColors.canvas)
    ) {
        // Mode Switcher Header: محراب الصالح vs خزانة الكتب
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .appleSpringClickable {
                        HapticFeedbackUtil.performLightTick(context)
                        showMihrabHub = true
                    },
                color = hubBg,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "🏛️ مِحْرَابُ الصَّالِح",
                    color = hubText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .appleSpringClickable {
                        HapticFeedbackUtil.performLightTick(context)
                        showMihrabHub = false
                    },
                color = shelfBg,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "📚 خِزَانَةُ الكُتُبِ الشَّامِلَة",
                    color = shelfText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }

        if (showMihrabHub) {
            SalehReformHubScreen(
                muhasabahScorePercent = muhasabahScore,
                nextSalahName = nextPrayer.arabicName,
                nextSalahCountdown = nextSalahCountdown,
                fardCompletedToday = fardCount,
                activeQuoteChunk = activeQuoteChunk,
                reformTracks = reformTracksList,
                openSourceBooks = openSourceBooksList,
                currentThemeMode = uiState?.muhasibThemeMode ?: MuhasibThemeMode.AMOLED_DARK,
                onToggleThemeMode = { viewModel?.toggleThemeMode() },
                onTriggerEmergencyPanicSheet = { viewModel?.setPanicSheetVisible(true) },
                onMarkQuoteChunkRead = { bookId ->
                    viewModel?.advanceQuoteChunk(bookId)
                    quoteUpdateTrigger++
                    Toast.makeText(context, "تم إتمام الشذرة وتحديث العلامة المرجعية ✓", Toast.LENGTH_SHORT).show()
                },
                onOpenBookReader = { bookId, page ->
                    val book = books.find { it.id == bookId || it.id == OpenSourceBookCatalog.findEntry(bookId)?.canonicalAlias }
                        ?: IslamicBooksSeedData.getInitialBooks().first()
                    activeReadingBook = book
                },
                onTogglePinQuoteToAod = { bookId ->
                    val entry = OpenSourceBookCatalog.findEntry(bookId)
                    if (entry != null && entry.sampleChunks.isNotEmpty()) {
                        val chunk = entry.sampleChunks.first()
                        QuotePreferences.setPinnedQuote(
                            context = context,
                            title = chunk.title,
                            content = chunk.content,
                            bookTitle = entry.titleArabic,
                            bookId = entry.id,
                            author = entry.authorArabic,
                            pageNumber = chunk.pageNumber
                        )
                        quoteUpdateTrigger++
                        Toast.makeText(context, "تم تثبيت الاقتباس على شاشة AOD بنجاح 📌", Toast.LENGTH_SHORT).show()
                    }
                },
                onOneClickDownloadBook = { bookId ->
                    viewModel?.downloadOpenSourceBook(bookId)
                    Toast.makeText(context, "جاري إعداد وتحميل الكتاب للشذرات اليومية ⚡", Toast.LENGTH_SHORT).show()
                },
                onToggleBookNotificationWird = { bookId ->
                    QuotePreferences.setActiveBookId(context, bookId)
                    onSelectActiveWirdBook(bookId)
                    BookWirdScheduler.scheduleDailyWirdAlarms(context)
                    Toast.makeText(context, "تم تفعيل إشعارات الوِرد اليومي للكتاب 🔔", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // Existing Category Shelf view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp)
            ) {
                // Top Header
                Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                    Text(
                        text = "خِزَانَةُ «الصَّالِح» الإِسْلَامِيَّة",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = appColors.textPrimary,
                            fontSize = 24.sp
                        )
                    )
                    Text(
                        text = "كتب التراث الإسلامي المفتوحة المصدر المقسمة للشذرات اليومية",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SpiritualGoldBright,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Categories Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = appColors.primaryAccent,
                    edgePadding = 18.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 3.dp,
                            color = appColors.primaryAccent
                        )
                    },
                    divider = {}
                ) {
                    BookCategoryTab.entries.forEachIndexed { index, tab ->
                        val isSelected = selectedTabIndex == index
                        Tab(
                            selected = isSelected,
                            onClick = {
                                HapticFeedbackUtil.performLightTick(context)
                                selectedTabIndex = index
                            },
                            text = {
                                Text(
                                    text = tab.titleArabic,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isSelected) appColors.textPrimary else appColors.textSecondary
                                    )
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Books List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredBooks, key = { it.id }) { book ->
                        BookCard(
                            book = book,
                            isActiveWird = book.id == activeBookId,
                            onSetAsWird = {
                                QuotePreferences.setActiveBookId(context, book.id)
                                activeBookId = book.id
                                onSelectActiveWirdBook(book.id)
                                HapticFeedbackUtil.performCompletionRumble(context)
                                Toast.makeText(context, "تم تعيين ${book.title} كورداً يومياً", Toast.LENGTH_SHORT).show()
                            },
                            onOpenReader = {
                                HapticFeedbackUtil.performLightTick(context)
                                activeReadingBook = book
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Interactive Reader Dialog
    activeReadingBook?.let { book ->
        BookReaderDialog(
            book = book,
            onDismiss = { activeReadingBook = null },
            onProgressUpdated = { newCursor ->
                scope.launch {
                    repository.updateBookCursor(book.id, newCursor)
                }
            }
        )
    }
}

@Composable
fun BookCard(
    book: IslamicBook,
    isActiveWird: Boolean,
    onSetAsWird: () -> Unit,
    onOpenReader: () -> Unit
) {
    val appColors = LocalAppColors.current
    val progress = (book.currentChunkCursor.toFloat() / maxOf(book.totalChunks, 1)).coerceIn(0f, 1f)

    AppleGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenReader),
        shape = SquircleShapeLarge,
        elevation = 4.dp,
        accentGlow = if (isActiveWird) SpiritualGoldBright else appColors.primaryAccent
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(appColors.primaryAccent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = "كتاب",
                            tint = appColors.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = appColors.textSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                if (isActiveWird) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(SpiritualGold.copy(alpha = 0.2f))
                            .border(1.dp, SpiritualGoldBright, PillShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "وِردك اليومي 📌",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpiritualGoldBright,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Text(
                text = book.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = appColors.textSecondary,
                    lineHeight = 18.sp,
                    fontSize = 12.sp
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Reading Progress Indicator
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "موضع القراءة: شذرة ${book.currentChunkCursor} من ${book.totalChunks}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = appColors.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = appColors.primaryAccent,
                            fontSize = 11.sp
                        )
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = appColors.primaryAccent,
                    trackColor = appColors.inset,
                    strokeCap = StrokeCap.Round
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenReader,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryAccent),
                    shape = SquircleShapeMedium
                ) {
                    Text(
                        text = "📖 افتح القارئ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                if (!isActiveWird) {
                    OutlinedButton(
                        onClick = onSetAsWird,
                        modifier = Modifier.weight(1f),
                        shape = SquircleShapeMedium
                    ) {
                        Text(
                            text = "📌 اجعله وردي",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = appColors.textPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookReaderDialog(
    book: IslamicBook,
    onDismiss: () -> Unit,
    onProgressUpdated: (Int) -> Unit
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as MuhasibApplication
    val repository = app.repository

    var currentChunkIndex by remember { mutableIntStateOf(book.currentChunkCursor) }
    var currentChunk by remember { mutableStateOf<BookChunk?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentChunkIndex) {
        isLoading = true
        withContext(Dispatchers.IO) {
            var chunk = repository.getChunkByIndexDirect(book.id, currentChunkIndex)
            if (chunk == null) {
                chunk = IslamicBooksSeedData.getInitialChunks().find { it.bookId == book.id && it.chunkIndex == currentChunkIndex }
            }
            if (chunk == null) {
                chunk = IslamicBooksSeedData.getInitialChunks().find { it.bookId == book.id }
            }
            currentChunk = chunk
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.background)
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "إغلاق",
                            tint = appColors.textPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "شذرة ${currentChunkIndex + 1} من ${book.totalChunks} (صفحة ${(currentChunkIndex / 2) + 1})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = appColors.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Pin Quote to Screen
                    IconButton(
                        onClick = {
                            currentChunk?.let { chunk ->
                                QuotePreferences.setPinnedQuote(context, chunk.title, chunk.content, book.title)
                                WidgetUpdateHelper.updateAllWidgets(context)
                                HapticFeedbackUtil.performCompletionRumble(context)
                                Toast.makeText(context, "تم تثبيت الاقتباس على شاشة القفل والودجت 📌", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "تثبيت على الشاشة",
                            tint = SpiritualGoldBright
                        )
                    }
                }

                // Chunk Body Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = appColors.primaryAccent)
                    } else {
                        currentChunk?.let { chunk ->
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = chunk.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.primaryAccent,
                                        fontSize = 17.sp,
                                        textAlign = TextAlign.Start
                                    )
                                )

                                Text(
                                    text = chunk.content,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 17.5.sp,
                                        lineHeight = 28.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = appColors.textPrimary,
                                        textAlign = TextAlign.Justify
                                    )
                                )

                                if (!chunk.reflectionQuestion.isNullOrBlank()) {
                                    AppleGlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = SquircleShapeMedium,
                                        accentGlow = SpiritualEmerald
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "💡 سؤال التفكّر والمحاسبة:",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = SpiritualEmeraldBright
                                                )
                                            )
                                            Text(
                                                text = chunk.reflectionQuestion,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = appColors.textPrimary,
                                                    lineHeight = 20.sp,
                                                    fontSize = 13.5.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer Navigation Buttons (Previous / Next)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentChunkIndex > 0) {
                                HapticFeedbackUtil.performLightTick(context)
                                currentChunkIndex--
                                onProgressUpdated(currentChunkIndex)
                            }
                        },
                        enabled = currentChunkIndex > 0,
                        modifier = Modifier.weight(1f),
                        shape = SquircleShapeMedium
                    ) {
                        Text("⬅️ الشذرة السابقة")
                    }

                    Button(
                        onClick = {
                            if (currentChunkIndex < book.totalChunks - 1) {
                                HapticFeedbackUtil.performLightTick(context)
                                currentChunkIndex++
                                onProgressUpdated(currentChunkIndex)
                                scope.launch {
                                    currentChunk?.let { repository.markChunkRead(it.id, true) }
                                }
                            } else {
                                Toast.makeText(context, "ما شاء الله! أتممت قراءة الكتاب كاملاً!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryAccent),
                        shape = SquircleShapeMedium
                    ) {
                        Text(
                            text = if (currentChunkIndex >= book.totalChunks - 1) "ختمت الكتاب ✓" else "التالي والمرور للشذرة التالية ➡️",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
