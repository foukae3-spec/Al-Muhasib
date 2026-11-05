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
import com.example.MuhasibApplication
import com.example.data.model.BookChunk
import com.example.data.model.IslamicBook
import com.example.data.seed.IslamicBooksSeedData
import com.example.library.BookDownloadStatus
import com.example.library.OpenSourceBookRepository
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeLarge
import com.example.ui.components.SquircleShapeMedium
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.SpiritualAmber
import com.example.ui.theme.SpiritualCyan
import com.example.ui.theme.SpiritualEmerald
import com.example.ui.theme.SpiritualEmeraldBright
import com.example.ui.theme.SpiritualGold
import com.example.ui.theme.SpiritualGoldBright
import com.example.util.HapticFeedbackUtil
import com.example.util.QuotePreferences
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class BookCategoryTab(val catId: String, val titleArabic: String) {
    ADDICTION(IslamicBooksSeedData.CAT_ADDICTION, "كسر الإدمان والشهوات"),
    SALAH(IslamicBooksSeedData.CAT_SALAH, "إقامة الصلاة وحفظها"),
    TAZKIYAH(IslamicBooksSeedData.CAT_TAZKIYAH, "تزكية الروح وصناعة العابد")
}

@Composable
fun BooksLibraryScreen(
    books: List<IslamicBook>,
    onSelectActiveWirdBook: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as MuhasibApplication
    val repository = app.repository

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var activeReadingBook by remember { mutableStateOf<IslamicBook?>(null) }
    var activeBookId by remember { mutableStateOf(QuotePreferences.getActiveBookId(context)) }

    val currentTab = BookCategoryTab.entries[selectedTabIndex]
    val filteredBooks = books.filter { it.category == currentTab.catId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(appColors.background)
            .padding(top = 10.dp)
    ) {
        // Top Header
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            Text(
                text = "خِزَانَةُ «الصَّالِح» الإِسْلَامِيَّة",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = appColors.textPrimary,
                    fontSize = 26.sp
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

        Spacer(modifier = Modifier.height(14.dp))

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
