package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hadith.HadithBookmark
import com.example.hadith.HadithDownloadState
import com.example.hadith.HadithItem
import com.example.hadith.HadithLibraryManager
import com.example.hadith.HeritageBook
import com.example.quran.book.QuranPaletteType
import com.example.quran.book.WrittenQuranCatalog
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeLarge
import com.example.ui.components.SquircleShapeMedium
import com.example.ui.components.SquircleShapeSmall
import com.example.util.HapticFeedbackUtil
import kotlinx.coroutines.launch

/**
 * Authentic Seerah & As-Sahihayn Library (مكتبة السيرة النبوية الشريفة وصحيح البخاري ومسلم).
 * Features:
 * - Sahih al-Bukhari, Sahih Muslim, and Ar-Raheeq Al-Makhtum
 * - Typographical distinction between Sanad (chain of narrators) and Matn (prophetic speech)
 * - Chapter/Bab layout, hadith numbering, and authenticity grade badges
 * - Public download of complete collections
 * - Reading progress memory and percentage completion per book
 * - Hadith bookmarking and sharing
 * - Search across all collections
 * - Eye-care palettes and font scaling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithLibraryScreen(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedBook by remember { mutableStateOf(HeritageBook.SAHIH_BUKHARI) }
    var hadithList by remember { mutableStateOf<List<HadithItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var currentPalette by remember { mutableStateOf(WrittenQuranCatalog.getSavedPalette(context)) }
    var fontSizeSp by remember { mutableFloatStateOf(WrittenQuranCatalog.getSavedFontSize(context)) }

    val downloadStates by HadithLibraryManager.downloadStates.collectAsStateWithLifecycle()
    val isBookDownloaded = remember(selectedBook, downloadStates) {
        HadithLibraryManager.isBookDownloaded(context, selectedBook.id)
    }

    // Reading progress
    var lastReadHadith by remember(selectedBook) {
        mutableStateOf(HadithLibraryManager.getLastReadHadithNumber(context, selectedBook.id))
    }

    // Sheets
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    // Intercept Android back gesture / button on physical devices
    BackHandler(enabled = true) {
        when {
            showSettingsSheet -> showSettingsSheet = false
            showBookmarksSheet -> showBookmarksSheet = false
            showSearchSheet -> showSearchSheet = false
            else -> onClose()
        }
    }

    // Load items when book switches
    LaunchedEffect(selectedBook) {
        isLoading = true
        hadithList = HadithLibraryManager.loadBookHadiths(context, selectedBook)
        isLoading = false
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(currentPalette.backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "رجوع",
                                tint = currentPalette.textColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "مكتبة السنة والسيرة النبوية",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = currentPalette.textColor
                                )
                            )
                            Text(
                                text = "الأحاديث الصحيحة الموثقة وسيرة المصطفى ﷺ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = currentPalette.accentColor,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { showSearchSheet = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث في الأحاديث",
                                tint = currentPalette.textColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        IconButton(
                            onClick = { showBookmarksSheet = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "العلامات المحفوظة",
                                tint = currentPalette.textColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        IconButton(
                            onClick = { showSettingsSheet = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "المظهر والخط",
                                tint = currentPalette.textColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Book Selector Tabs: البخاري / مسلم / السيرة
                ScrollableTabRow(
                    selectedTabIndex = HeritageBook.entries.indexOf(selectedBook),
                    containerColor = currentPalette.pageColor,
                    contentColor = currentPalette.accentColor,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        val index = HeritageBook.entries.indexOf(selectedBook)
                        if (index in tabPositions.indices) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[index]),
                                color = currentPalette.accentColor
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleShapeMedium)
                        .border(1.dp, currentPalette.borderColor, SquircleShapeMedium)
                ) {
                    HeritageBook.entries.forEach { book ->
                        val selected = book == selectedBook
                        Tab(
                            selected = selected,
                            onClick = {
                                HapticFeedbackUtil.performLightTick(context)
                                selectedBook = book
                            },
                            text = {
                                // ponytail: prevent vertical letter stacking on large accessibility font scales
                                Text(
                                    text = book.titleArabic,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) currentPalette.accentColor else currentPalette.textColor,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Book Summary & Download Banner
                BookHeaderBanner(
                    book = selectedBook,
                    palette = currentPalette,
                    isDownloaded = isBookDownloaded,
                    downloadState = downloadStates[selectedBook.id] ?: HadithDownloadState.Idle,
                    lastReadHadith = lastReadHadith,
                    onStartDownload = {
                        scope.launch {
                            HadithLibraryManager.downloadBook(context, selectedBook)
                            hadithList = HadithLibraryManager.loadBookHadiths(context, selectedBook)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Hadith Items List
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = currentPalette.accentColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(hadithList, key = { it.id }) { hadith ->
                            HadithCard(
                                hadith = hadith,
                                palette = currentPalette,
                                fontSizeSp = fontSizeSp,
                                isLastRead = hadith.hadithNumber == lastReadHadith,
                                onBookmark = {
                                    val bookmark = HadithBookmark(
                                        id = System.currentTimeMillis().toString(),
                                        bookId = selectedBook.id,
                                        hadithId = hadith.id,
                                        title = "${selectedBook.titleArabic} • حديث ${hadith.hadithNumber}",
                                        snippet = hadith.matn.take(60)
                                    )
                                    HadithLibraryManager.saveBookmark(context, bookmark)
                                    Toast.makeText(context, "تم حفظ الحديث في العلامات المرجعية", Toast.LENGTH_SHORT).show()
                                },
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val textToCopy = "${hadith.chapterTitle}\n${hadith.babTitle}\n\nالسند:\n${hadith.sanad}\n\nالمتن:\n${hadith.matn}\n\n[${hadith.reference}]"
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Hadith", textToCopy))
                                    Toast.makeText(context, "تم نسخ الحديث الشريف", Toast.LENGTH_SHORT).show()
                                },
                                onMarkAsRead = {
                                    lastReadHadith = hadith.hadithNumber
                                    HadithLibraryManager.saveReadingProgress(
                                        context,
                                        selectedBook.id,
                                        hadith.hadithNumber,
                                        hadithList.size
                                    )
                                    Toast.makeText(context, "تم تحديد مكان القراءة", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            // Search Sheet
            if (showSearchSheet) {
                HadithSearchSheet(
                    palette = currentPalette,
                    currentBookId = selectedBook.id,
                    onDismiss = { showSearchSheet = false },
                    onSelectHadith = { selected ->
                        // Navigate to book if needed
                        val targetBook = HeritageBook.entries.find { it.id == selected.bookId }
                        if (targetBook != null && targetBook != selectedBook) {
                            selectedBook = targetBook
                        }
                        showSearchSheet = false
                    }
                )
            }

            // Bookmarks Sheet
            if (showBookmarksSheet) {
                HadithBookmarksSheet(
                    palette = currentPalette,
                    onDismiss = { showBookmarksSheet = false },
                    onSelectBookmark = { bookmark ->
                        val targetBook = HeritageBook.entries.find { it.id == bookmark.bookId }
                        if (targetBook != null && targetBook != selectedBook) {
                            selectedBook = targetBook
                        }
                        showBookmarksSheet = false
                    }
                )
            }

            // Settings Sheet
            if (showSettingsSheet) {
                HadithSettingsSheet(
                    palette = currentPalette,
                    fontSizeSp = fontSizeSp,
                    onPaletteChange = {
                        currentPalette = it
                        WrittenQuranCatalog.savePalette(context, it)
                    },
                    onFontSizeChange = {
                        fontSizeSp = it
                        WrittenQuranCatalog.saveFontSize(context, it)
                    },
                    onDismiss = { showSettingsSheet = false }
                )
            }
        }
    }
}

/**
 * Header Banner for current book showing author, description, reading progress, and download button.
 */
@Composable
fun BookHeaderBanner(
    book: HeritageBook,
    palette: QuranPaletteType,
    isDownloaded: Boolean,
    downloadState: HadithDownloadState,
    lastReadHadith: Int,
    onStartDownload: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SquircleShapeLarge)
            .background(palette.pageColor)
            .border(1.dp, palette.borderColor, SquircleShapeLarge)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.titleArabic,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textColor
                        )
                    )
                    Text(
                        text = book.authorArabic,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = palette.accentColor,
                            fontSize = 11.sp
                        )
                    )
                }

                // Download or Downloaded Status Pill
                if (downloadState is HadithDownloadState.Downloading) {
                    CircularProgressIndicator(
                        progress = { downloadState.progress },
                        modifier = Modifier.size(24.dp),
                        color = palette.accentColor,
                        strokeWidth = 2.5.dp
                    )
                } else if (isDownloaded) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(palette.accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = palette.accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "مُحمّل محلياً",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.accentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(palette.accentColor)
                            .clickable { onStartDownload() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "تحميل المجلد كاملاً",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = book.descriptionArabic,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = palette.textColor.copy(alpha = 0.7f),
                    fontSize = 11.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (lastReadHadith > 1) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "آخر قراءة لك: الحديث / الموضع رقم $lastReadHadith",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

/**
 * Individual Hadith or Seerah entry card with Sanad and Matn typography.
 */
@Composable
fun HadithCard(
    hadith: HadithItem,
    palette: QuranPaletteType,
    fontSizeSp: Float,
    isLastRead: Boolean,
    onBookmark: () -> Unit,
    onCopy: () -> Unit,
    onMarkAsRead: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SquircleShapeLarge)
            .background(palette.pageColor)
            .border(
                width = if (isLastRead) 2.dp else 1.dp,
                color = if (isLastRead) palette.accentColor else palette.borderColor,
                shape = SquircleShapeLarge
            )
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Hadith Number Badge & Grade
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
                            .clip(PillShape)
                            .background(palette.accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (hadith.historicalMilestone.isNotBlank()) hadith.historicalMilestone else "حديث #${hadith.hadithNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = palette.accentColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(palette.borderColor.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = hadith.grade,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = palette.textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Reference / Chapter
                Text(
                    text = hadith.chapterTitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.textColor.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                )
            }

            if (hadith.babTitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = hadith.babTitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.accentColor,
                        fontSize = 12.sp
                    )
                )
            }

            // Sanad Section (Chain of narrators in subtle typography)
            if (hadith.sanad.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleShapeSmall)
                        .background(palette.borderColor.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = hadith.sanad,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = palette.textColor.copy(alpha = 0.75f),
                            fontSize = (fontSizeSp * 0.75f).sp,
                            fontFamily = FontFamily.Serif,
                            lineHeight = (fontSizeSp * 1.35f).sp
                        )
                    )
                }
            }

            // Matn Section (The Prophetic Words in prominent bold typography)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = palette.borderColor,
                        shape = SquircleShapeSmall
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = hadith.matn,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = palette.textColor,
                        fontSize = fontSizeSp.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.Serif,
                        lineHeight = (fontSizeSp * 1.8f).sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Bookmark, Copy, Mark as Read
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hadith.reference,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.textColor.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onMarkAsRead,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "تحديد كموضع قراءة",
                            tint = if (isLastRead) palette.accentColor else palette.textColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onBookmark,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "حفظ علامة",
                            tint = palette.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = palette.textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Search Sheet across Hadith and Seerah collections.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithSearchSheet(
    palette: QuranPaletteType,
    currentBookId: String,
    onDismiss: () -> Unit,
    onSelectHadith: (HadithItem) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<HadithItem>>(emptyList()) }

    LaunchedEffect(query) {
        if (query.trim().length >= 2) {
            results = HadithLibraryManager.searchHadithsAsync(query)
        } else {
            results = emptyList()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.pageColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "البحث في أحاديث الصحيحين والسيرة",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.textColor
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ابحث في المتن أو السند (مثلاً: الأعمال بالنيات، بني الإسلام...)", fontSize = 12.sp) },
                singleLine = true,
                shape = PillShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.accentColor,
                    unfocusedBorderColor = palette.borderColor,
                    focusedTextColor = palette.textColor,
                    unfocusedTextColor = palette.textColor
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "النتائج (${results.size})",
                style = MaterialTheme.typography.labelSmall.copy(color = palette.accentColor, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results, key = { it.id }) { item ->
                    val bookTitle = HeritageBook.entries.find { it.id == item.bookId }?.titleArabic ?: ""
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleShapeSmall)
                            .background(palette.borderColor.copy(alpha = 0.3f))
                            .clickable { onSelectHadith(item) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$bookTitle • حديث ${item.hadithNumber}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.accentColor
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.matn,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = palette.textColor,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Serif
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Saved Hadith Bookmarks Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithBookmarksSheet(
    palette: QuranPaletteType,
    onDismiss: () -> Unit,
    onSelectBookmark: (HadithBookmark) -> Unit
) {
    val context = LocalContext.current
    var bookmarks by remember { mutableStateOf(HadithLibraryManager.getBookmarks(context)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.pageColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "العلامات المرجعية المحفوظة (${bookmarks.size})",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.textColor
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد علامات مرجعية محفوظة.\nيمكنك حفظ أي حديث بالضغط على أيقونة الإشارة المرجعية.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = palette.textColor.copy(alpha = 0.6f)
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bookmarks, key = { it.id }) { bookmark ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SquircleShapeSmall)
                                .background(palette.borderColor.copy(alpha = 0.3f))
                                .clickable { onSelectBookmark(bookmark) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bookmark.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = palette.accentColor
                                    )
                                )
                                Text(
                                    text = bookmark.snippet,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = palette.textColor,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 2
                                )
                            }

                            IconButton(
                                onClick = {
                                    HadithLibraryManager.removeBookmark(context, bookmark.id)
                                    bookmarks = HadithLibraryManager.getBookmarks(context)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "حذف",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Settings Sheet for Hadith Library (Palettes and Font Scaling).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithSettingsSheet(
    palette: QuranPaletteType,
    fontSizeSp: Float,
    onPaletteChange: (QuranPaletteType) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
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
            Text(
                text = "إعدادات القراءة والمظهر",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.textColor
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "ألوان الخلفية والورق:",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.accentColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuranPaletteType.entries.forEach { pal ->
                val isSelected = pal == palette
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(SquircleShapeSmall)
                        .background(if (isSelected) pal.accentColor.copy(alpha = 0.15f) else pal.borderColor.copy(alpha = 0.25f))
                        .clickable { onPaletteChange(pal) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = pal.labelArabic,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = palette.textColor
                        )
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = palette.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "حجم خط المتن: ${fontSizeSp.toInt()} sp",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.accentColor
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(palette.borderColor)
                            .clickable { onFontSizeChange((fontSizeSp - 2f).coerceAtLeast(14f)) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("A-", fontWeight = FontWeight.Bold, color = palette.textColor)
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(palette.borderColor)
                            .clickable { onFontSizeChange((fontSizeSp + 2f).coerceAtMost(36f)) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("A+", fontWeight = FontWeight.Bold, color = palette.textColor)
                    }
                }
            }

            Slider(
                value = fontSizeSp,
                onValueChange = onFontSizeChange,
                valueRange = 14f..36f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = palette.accentColor,
                    activeTrackColor = palette.accentColor,
                    inactiveTrackColor = palette.borderColor
                )
            )
        }
    }
}
