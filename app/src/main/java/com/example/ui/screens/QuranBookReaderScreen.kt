package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.quran.book.AyahTafsir
import com.example.quran.book.JuzIndex
import com.example.quran.book.PageTurnMode
import com.example.quran.book.QuranBookmark
import com.example.quran.book.QuranPaletteType
import com.example.quran.book.QuranReadingStyle
import com.example.quran.book.SurahPageIndex
import com.example.quran.book.TafsirCatalog
import com.example.quran.book.TafsirEdition
import com.example.quran.book.WrittenAyah
import com.example.quran.book.WrittenQuranCatalog
import com.example.quran.book.WrittenQuranDownloadState
import com.example.quran.book.WrittenQuranPage
import com.example.quran.book.WrittenQuranRiwayah
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeLarge
import com.example.ui.components.SquircleShapeMedium
import com.example.ui.components.SquircleShapeSmall
import com.example.util.HapticFeedbackUtil
import androidx.compose.foundation.gestures.detectTransformGestures
import coil.request.CachePolicy
import com.example.quran.tajweed.TajweedGuideDialog
import com.example.quran.tajweed.TajweedParser
import com.example.quran.tajweed.TajweedRule
import com.example.quran.tajweed.TajweedSettings
import com.example.quran.tajweed.TajweedTheme
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * Hyper-Realistic Written Quran Book Reader (المصحف الشريف المقروء).
 * Features:
 * - 3D paper curling/turn physics with dynamic spine shadows and specular light highlights
 * - Normal smooth swipe mode toggle
 * - Eye-care palettes: Warm Sepia paper, AMOLED Deep Forest/Black, and Daylight Soft White
 * - Complete 604-page Madani Mushaf layout with ornate Islamic cartouches & page borders
 * - Ayah bookmarking with instant jump
 * - Last-read page memory (auto-resumes)
 * - Surah (1-114) and Juz (1-30) quick index sheets
 * - Diacritic-tolerant Quranic Arabic search
 * - Adjustable Uthmanic font scaling
 * - One-click full written Quran download from verified public repository
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranBookReaderScreen(
    initialPage: Int? = null,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current.density

    var currentPalette by remember { mutableStateOf(WrittenQuranCatalog.getSavedPalette(context)) }
    var pageTurnMode by remember { mutableStateOf(WrittenQuranCatalog.getSavedPageTurnMode(context)) }
    var fontSizeSp by remember { mutableFloatStateOf(WrittenQuranCatalog.getSavedFontSize(context)) }
    var lineSpacingMultiplier by remember { mutableFloatStateOf(WrittenQuranCatalog.getSavedLineSpacing(context)) }
    var tashkeelContrast by remember { mutableStateOf(WrittenQuranCatalog.getSavedTashkeelContrast(context)) }
    var readingStyle by remember { mutableStateOf(WrittenQuranCatalog.getSavedReadingStyle(context)) }
    var currentRiwayah by remember { mutableStateOf(WrittenQuranCatalog.getSavedRiwayah(context)) }

    val startPage = remember {
        (initialPage ?: WrittenQuranCatalog.getLastReadPage(context)).coerceIn(1, 604)
    }

    // 604 pages indexed 0 to 603
    val pagerState = rememberPagerState(
        initialPage = startPage - 1,
        pageCount = { 604 }
    )

    // Load pages in memory
    var pagesLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(currentRiwayah) {
        WrittenQuranCatalog.loadPages(context, currentRiwayah)
        pagesLoaded = true
    }

    val downloadState by WrittenQuranCatalog.downloadState.collectAsStateWithLifecycle()
    val isDownloaded = remember(downloadState, currentRiwayah) { WrittenQuranCatalog.isFullQuranDownloaded(context, currentRiwayah) }

    // HUD visibility (toggle on center tap)
    var showHud by remember { mutableStateOf(true) }

    // Dialog & Sheet States
    var showIndexSheet by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showTajweedGuide by remember { mutableStateOf(false) }
    var tajweedSettingsVersion by remember { mutableIntStateOf(0) }

    // Selected Ayah for quick action (copy / bookmark)
    var selectedAyah by remember { mutableStateOf<WrittenAyah?>(null) }
    // Selected Ayah for Tafsir bottom sheet (triggered on long press)
    var tafsirAyah by remember { mutableStateOf<WrittenAyah?>(null) }

    val verticalListState = androidx.compose.foundation.lazy.rememberLazyListState(
        initialFirstVisibleItemIndex = startPage - 1
    )

    // Handle Android system back gesture / back button
    BackHandler(enabled = true) {
        when {
            tafsirAyah != null -> tafsirAyah = null
            showTajweedGuide -> showTajweedGuide = false
            showIndexSheet -> showIndexSheet = false
            showBookmarksSheet -> showBookmarksSheet = false
            showSearchSheet -> showSearchSheet = false
            showSettingsSheet -> showSettingsSheet = false
            showDownloadDialog -> showDownloadDialog = false
            selectedAyah != null -> selectedAyah = null
            else -> onClose()
        }
    }

    // Active page info
    val currentPageNumber = if (pageTurnMode == PageTurnMode.VERTICAL_SCROLL) {
        (verticalListState.firstVisibleItemIndex + 1).coerceIn(1, 604)
    } else {
        (pagerState.currentPage + 1).coerceIn(1, 604)
    }

    // Keep page index synced across mode changes
    LaunchedEffect(pageTurnMode) {
        if (pageTurnMode == PageTurnMode.VERTICAL_SCROLL) {
            verticalListState.scrollToItem(pagerState.currentPage)
        } else {
            pagerState.scrollToPage(verticalListState.firstVisibleItemIndex)
        }
    }

    val currentPage = remember(currentPageNumber, pagesLoaded, currentRiwayah) {
        WrittenQuranCatalog.getPage(currentPageNumber, currentRiwayah)
    }

    // Auto-save last read page & tactile paper flick tick
    var lastHapticPage by remember { mutableIntStateOf(currentPageNumber) }
    LaunchedEffect(currentPageNumber) {
        WrittenQuranCatalog.saveLastReadPage(context, currentPageNumber, currentPage.surahNameArabic)
        if (lastHapticPage != currentPageNumber) {
            lastHapticPage = currentPageNumber
            HapticFeedbackUtil.performLightTick(context)
        }
    }

    // RTL for Arabic Book: right-to-left layout direction
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(currentPalette.backgroundColor)
        ) {
            if (pageTurnMode == PageTurnMode.VERTICAL_SCROLL) {
                LazyColumn(
                    state = verticalListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showHud = !showHud
                        },
                    contentPadding = PaddingValues(
                        top = if (showHud) 70.dp else 24.dp,
                        bottom = if (showHud) 90.dp else 30.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(604, key = { it }) { pageIndex ->
                        val pageNumber = pageIndex + 1
                        val pageData = WrittenQuranCatalog.getPage(pageNumber, currentRiwayah)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                        ) {
                            BookPageSurface(
                                page = pageData,
                                palette = currentPalette,
                                fontSizeSp = fontSizeSp,
                                lineSpacingMultiplier = lineSpacingMultiplier,
                                tashkeelContrast = tashkeelContrast,
                                readingStyle = readingStyle,
                                riwayah = currentRiwayah,
                                tajweedVersion = tajweedSettingsVersion,
                                onAyahClick = { ayah ->
                                    HapticFeedbackUtil.performLightTick(context)
                                    selectedAyah = ayah
                                },
                                onAyahLongPress = { ayah ->
                                    HapticFeedbackUtil.performMechanicalClick(context)
                                    tafsirAyah = ayah
                                }
                            )
                        }
                    }
                }
            } else {
                // Main Book Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showHud = !showHud
                        }
                ) { pageIndex ->
                    val pageNumber = pageIndex + 1
                    val pageData = WrittenQuranCatalog.getPage(pageNumber, currentRiwayah)

                    val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                    val absOffset = pageOffset.absoluteValue
                    val isTurningForward = pageOffset in 0f..1f
                    val isPageUnderneath = pageOffset in -1f..0f

                    // 3D realistic page curling transformation
                    val pageModifier = if (pageTurnMode == PageTurnMode.REALISTIC_3D) {
                        Modifier.graphicsLayer {
                            cameraDistance = 16f * density

                            if (isTurningForward) {
                                // Anchor rotation at the book spine (right edge in RTL layout)
                                transformOrigin = TransformOrigin(1f, 0.5f)
                                // Smooth 3D curl rotation
                                val angle = (-pageOffset * 85f).coerceIn(-90f, 0f)
                                rotationY = angle
                                // Counteract pager translation to anchor page to spine
                                translationX = pageOffset * size.width
                                // Depth attenuation
                                alpha = (1f - pageOffset * 0.35f).coerceIn(0.6f, 1f)
                            } else if (isPageUnderneath) {
                                // Revealing underlying page rests flat with subtle scale depth
                                rotationY = 0f
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                                val progress = 1f - absOffset
                                scaleX = 0.96f + (0.04f * progress)
                                scaleY = 0.96f + (0.04f * progress)
                                alpha = 0.7f + (0.3f * progress)
                            } else {
                                rotationY = 0f
                            }
                        }
                    } else {
                        Modifier
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(pageModifier)
                            .padding(horizontal = 10.dp, vertical = if (showHud) 70.dp else 24.dp)
                    ) {
                        BookPageSurface(
                            page = pageData,
                            palette = currentPalette,
                            fontSizeSp = fontSizeSp,
                            lineSpacingMultiplier = lineSpacingMultiplier,
                            tashkeelContrast = tashkeelContrast,
                            readingStyle = readingStyle,
                            riwayah = currentRiwayah,
                            tajweedVersion = tajweedSettingsVersion,
                            onAyahClick = { ayah ->
                                HapticFeedbackUtil.performLightTick(context)
                                selectedAyah = ayah
                            },
                            onAyahLongPress = { ayah ->
                                HapticFeedbackUtil.performMechanicalClick(context)
                                tafsirAyah = ayah
                            }
                        )

                        // 3D paper fold and curl shadows
                        if (pageTurnMode == PageTurnMode.REALISTIC_3D) {
                            if (isTurningForward && pageOffset > 0.01f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = (pageOffset * 0.25f).coerceIn(0f, 0.35f))
                                                )
                                            )
                                        )
                                )
                            } else if (isPageUnderneath && absOffset < 1f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = (absOffset * 0.3f).coerceIn(0f, 0.35f)),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Top HUD Bar
            AnimatedVisibility(
                visible = showHud,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                ReaderTopHud(
                    currentPage = currentPage,
                    palette = currentPalette,
                    isDownloaded = isDownloaded,
                    readingStyle = readingStyle,
                    riwayah = currentRiwayah,
                    onToggleReadingStyle = {
                        val nextStyle = if (readingStyle == QuranReadingStyle.AUTHENTIC_HANDWRITTEN) {
                            QuranReadingStyle.DIGITAL_UTHMANI
                        } else {
                            QuranReadingStyle.AUTHENTIC_HANDWRITTEN
                        }
                        readingStyle = nextStyle
                        WrittenQuranCatalog.saveReadingStyle(context, nextStyle)
                        HapticFeedbackUtil.performLightTick(context)
                    },
                    onClose = onClose,
                    onOpenIndex = { showIndexSheet = true },
                    onOpenSearch = { showSearchSheet = true },
                    onOpenBookmarks = { showBookmarksSheet = true },
                    onOpenSettings = { showSettingsSheet = true },
                    onOpenDownload = { showDownloadDialog = true },
                    onOpenTajweedGuide = { showTajweedGuide = true }
                )
            }

            // Bottom HUD Bar
            AnimatedVisibility(
                visible = showHud,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                ReaderBottomHud(
                    currentPageNumber = currentPageNumber,
                    palette = currentPalette,
                    pageTurnMode = pageTurnMode,
                    onPageSeek = { targetPage ->
                        scope.launch {
                            if (pageTurnMode == PageTurnMode.VERTICAL_SCROLL) {
                                verticalListState.scrollToItem(targetPage - 1)
                            } else {
                                pagerState.scrollToPage(targetPage - 1)
                            }
                        }
                    },
                    onTogglePageTurnMode = {
                        val nextMode = when (pageTurnMode) {
                            PageTurnMode.REALISTIC_3D -> PageTurnMode.NORMAL_SWIPE
                            PageTurnMode.NORMAL_SWIPE -> PageTurnMode.VERTICAL_SCROLL
                            PageTurnMode.VERTICAL_SCROLL -> PageTurnMode.REALISTIC_3D
                        }
                        pageTurnMode = nextMode
                        WrittenQuranCatalog.savePageTurnMode(context, nextMode)
                        HapticFeedbackUtil.performLightTick(context)
                    },
                    onBookmarkCurrentPage = {
                        val ayah = currentPage.ayahs.firstOrNull()
                        val bookmark = QuranBookmark(
                            id = System.currentTimeMillis().toString(),
                            pageNumber = currentPageNumber,
                            surahNumber = currentPage.surahNumber,
                            surahName = currentPage.surahNameArabic,
                            ayahNumber = ayah?.ayahNumber ?: 1,
                            snippet = ayah?.textUthmani?.take(40) ?: "صفحة $currentPageNumber"
                        )
                        WrittenQuranCatalog.saveBookmark(context, bookmark)
                        HapticFeedbackUtil.performMechanicalClick(context)
                        Toast.makeText(context, "تم حفظ العلامة المرجعية: ص $currentPageNumber", Toast.LENGTH_SHORT).show()
                    },
                    onOpenSettings = {
                        HapticFeedbackUtil.performLightTick(context)
                        showSettingsSheet = true
                    }
                )
            }

            // ponytail: prominent floating Customization FAB — always visible & easily tapped
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = if (showHud) 105.dp else 24.dp)
                    .shadow(10.dp, CircleShape)
                    .clip(CircleShape)
                    .background(currentPalette.pageColor)
                    .border(1.5.dp, currentPalette.accentColor, CircleShape)
                    .clickable {
                        HapticFeedbackUtil.performLightTick(context)
                        showSettingsSheet = true
                    }
                    .padding(11.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "تخصيص الخط والرواية والمظهر",
                    tint = currentPalette.accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Download Status Banner if actively downloading
            if (downloadState is WrittenQuranDownloadState.Downloading) {
                val state = downloadState as WrittenQuranDownloadState.Downloading
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp, start = 20.dp, end = 20.dp)
                        .clip(SquircleShapeMedium)
                        .background(currentPalette.pageColor)
                        .border(1.dp, currentPalette.accentColor, SquircleShapeMedium)
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "جاري تحميل المصحف الشريف المكتوب",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = currentPalette.textColor
                                )
                            )
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = currentPalette.accentColor,
                                strokeWidth = 2.dp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.statusMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = currentPalette.accentColor,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { state.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = currentPalette.accentColor,
                            trackColor = currentPalette.borderColor
                        )
                    }
                }
            }

            // Quick Ayah Action Sheet (Bookmark / Copy / Tafsir)
            if (selectedAyah != null) {
                AyahActionSheet(
                    ayah = selectedAyah!!,
                    palette = currentPalette,
                    onDismiss = { selectedAyah = null },
                    onBookmark = {
                        val bookmark = QuranBookmark(
                            id = System.currentTimeMillis().toString(),
                            pageNumber = selectedAyah!!.pageNumber,
                            surahNumber = selectedAyah!!.surahNumber,
                            surahName = currentPage.surahNameArabic,
                            ayahNumber = selectedAyah!!.ayahNumber,
                            snippet = selectedAyah!!.textUthmani
                        )
                        WrittenQuranCatalog.saveBookmark(context, bookmark)
                        Toast.makeText(context, "تم حفظ الآية في العلامات المرجعية", Toast.LENGTH_SHORT).show()
                        selectedAyah = null
                    },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Quran Ayah", "${selectedAyah!!.textUthmani} (${currentPage.surahNameArabic}:${selectedAyah!!.ayahNumber})")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "تم نسخ الآية الكريمة", Toast.LENGTH_SHORT).show()
                        selectedAyah = null
                    },
                    onTafsir = {
                        val ayahToExplain = selectedAyah
                        selectedAyah = null
                        tafsirAyah = ayahToExplain
                    }
                )
            }

            // Ayah Tafsir Bottom Sheet (triggered on Long-Press on Ayah span or via quick action)
            if (tafsirAyah != null) {
                val currentAyahForTafsir = tafsirAyah!!
                AyahTafsirBottomSheet(
                    ayah = currentAyahForTafsir,
                    surahName = WrittenQuranCatalog.getSurahNameArabic(currentAyahForTafsir.surahNumber),
                    palette = currentPalette,
                    fontSizeSp = fontSizeSp,
                    onDismiss = { tafsirAyah = null },
                    onBookmark = {
                        val bookmark = QuranBookmark(
                            id = System.currentTimeMillis().toString(),
                            pageNumber = currentAyahForTafsir.pageNumber,
                            surahNumber = currentAyahForTafsir.surahNumber,
                            surahName = WrittenQuranCatalog.getSurahNameArabic(currentAyahForTafsir.surahNumber),
                            ayahNumber = currentAyahForTafsir.ayahNumber,
                            snippet = currentAyahForTafsir.textUthmani
                        )
                        WrittenQuranCatalog.saveBookmark(context, bookmark)
                        HapticFeedbackUtil.performLightTick(context)
                        Toast.makeText(context, "تم حفظ الآية في العلامات المرجعية", Toast.LENGTH_SHORT).show()
                    },
                    onCopy = { fullText ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Quran Ayah Tafsir", fullText)
                        clipboard.setPrimaryClip(clip)
                        HapticFeedbackUtil.performLightTick(context)
                        Toast.makeText(context, "تم نسخ الآية وتفسيرها بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    onShare = { fullText ->
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, fullText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "مشاركة الآية الكريمة وتفسيرها")
                        context.startActivity(shareIntent)
                    }
                )
            }

            // Index Sheet (Surahs 1-114 and Ajza' 1-30)
            if (showIndexSheet) {
                QuranIndexSheet(
                    palette = currentPalette,
                    onSelectSurah = { surahIndex ->
                        scope.launch {
                            pagerState.scrollToPage(surahIndex.pageNumber - 1)
                        }
                        showIndexSheet = false
                    },
                    onSelectJuz = { juzIndex ->
                        scope.launch {
                            pagerState.scrollToPage(juzIndex.startPageNumber - 1)
                        }
                        showIndexSheet = false
                    },
                    onDismiss = { showIndexSheet = false }
                )
            }

            // Bookmarks Sheet
            if (showBookmarksSheet) {
                BookmarksSheet(
                    palette = currentPalette,
                    onSelectBookmark = { bookmark ->
                        scope.launch {
                            pagerState.scrollToPage(bookmark.pageNumber - 1)
                        }
                        showBookmarksSheet = false
                    },
                    onDismiss = { showBookmarksSheet = false }
                )
            }

            // Search Sheet
            if (showSearchSheet) {
                QuranSearchSheet(
                    palette = currentPalette,
                    onSelectResult = { ayah ->
                        scope.launch {
                            pagerState.scrollToPage(ayah.pageNumber - 1)
                        }
                        showSearchSheet = false
                    },
                    onDismiss = { showSearchSheet = false }
                )
            }

            // Settings Sheet (Palette, Font Size, Mode, Line Spacing, Tashkeel Contrast, Riwayah)
            if (showSettingsSheet) {
                ReaderSettingsSheet(
                    palette = currentPalette,
                    pageTurnMode = pageTurnMode,
                    fontSizeSp = fontSizeSp,
                    lineSpacingMultiplier = lineSpacingMultiplier,
                    tashkeelContrast = tashkeelContrast,
                    readingStyle = readingStyle,
                    riwayah = currentRiwayah,
                    onPaletteChange = {
                        currentPalette = it
                        WrittenQuranCatalog.savePalette(context, it)
                    },
                    onModeChange = {
                        pageTurnMode = it
                        WrittenQuranCatalog.savePageTurnMode(context, it)
                    },
                    onFontSizeChange = {
                        fontSizeSp = it
                        WrittenQuranCatalog.saveFontSize(context, it)
                    },
                    onLineSpacingChange = {
                        lineSpacingMultiplier = it
                        WrittenQuranCatalog.saveLineSpacing(context, it)
                    },
                    onTashkeelContrastChange = {
                        tashkeelContrast = it
                        WrittenQuranCatalog.saveTashkeelContrast(context, it)
                    },
                    onReadingStyleChange = {
                        readingStyle = it
                        WrittenQuranCatalog.saveReadingStyle(context, it)
                    },
                    onRiwayahChange = {
                        currentRiwayah = it
                        WrittenQuranCatalog.saveRiwayah(context, it)
                    },
                    onOpenTajweedGuide = { showTajweedGuide = true },
                    onDismiss = { showSettingsSheet = false }
                )
            }

            // Tajweed Rules & Color Map Guide Sheet
            if (showTajweedGuide) {
                TajweedGuideDialog(
                    palette = currentPalette,
                    onDismiss = {
                        showTajweedGuide = false
                        tajweedSettingsVersion++
                    }
                )
            }

            // Download Full Quran Dialog
            if (showDownloadDialog) {
                DownloadFullQuranDialog(
                    palette = currentPalette,
                    isDownloaded = isDownloaded,
                    downloadState = downloadState,
                    riwayah = currentRiwayah,
                    onStartDownload = {
                        scope.launch {
                            WrittenQuranCatalog.downloadFullWrittenQuran(context, currentRiwayah)
                        }
                    },
                    onDismiss = { showDownloadDialog = false }
                )
            }
        }
    }
}

/**
 * The physical paper surface rendering of a single Quran page with ornate Islamic border.
 * Supports:
 * - Authentic King Fahd Complex handwritten calligraphy scan (Sheikh Uthman Taha)
 * - Pure OLED high-contrast dark inversion for night reading
 * - Responsive Digital Uthmanic script with selectable ayahs
 */
@Composable
fun BookPageSurface(
    page: WrittenQuranPage,
    palette: QuranPaletteType,
    fontSizeSp: Float,
    lineSpacingMultiplier: Float = 1.9f,
    tashkeelContrast: Boolean = false,
    readingStyle: QuranReadingStyle = QuranReadingStyle.AUTHENTIC_HANDWRITTEN,
    riwayah: WrittenQuranRiwayah = WrittenQuranRiwayah.HAFS,
    tajweedVersion: Int = 0,
    onAyahClick: (WrittenAyah) -> Unit,
    onAyahLongPress: (WrittenAyah) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .shadow(
                elevation = 8.dp,
                shape = SquircleShapeLarge,
                ambientColor = if (palette.isDark) Color(0x33000000) else Color(0x18000000),
                spotColor = if (palette.isDark) Color(0x66000000) else Color(0x22000000)
            )
            .clip(SquircleShapeLarge)
            .background(palette.pageColor)
            .border(2.dp, palette.borderColor, SquircleShapeLarge)
            .padding(if (readingStyle == QuranReadingStyle.AUTHENTIC_HANDWRITTEN) 4.dp else 14.dp)
    ) {
        if (readingStyle == QuranReadingStyle.AUTHENTIC_HANDWRITTEN) {
            val context = LocalContext.current
            val imageUrl = remember(page.pageNumber, riwayah) {
                WrittenQuranCatalog.getAuthenticPageImageUrl(page.pageNumber, riwayah)
            }
            val darkInvertFilter = remember(palette.isDark) {
                if (palette.isDark) {
                    ColorFilter.colorMatrix(
                        ColorMatrix(
                            floatArrayOf(
                                -1f,  0f,  0f,  0f, 255f,
                                 0f, -1f,  0f,  0f, 255f,
                                 0f,  0f, -1f,  0f, 255f,
                                 0f,  0f,  0f,  1f,   0f
                            )
                        )
                    )
                } else null
            }

            var isImageLoading by remember(page.pageNumber, riwayah) { mutableStateOf(true) }
            var isImageError by remember(page.pageNumber, riwayah) { mutableStateOf(false) }
            var showAyahPicker by remember { mutableStateOf(false) }

            // Dynamic Font & Zoom Scaling:
            // Base scale mapped from the font size slider (24sp = 1.0x, up to 42sp = 1.75x)
            val baseScale = (fontSizeSp / 24f).coerceIn(1.0f, 2.5f)
            var userPinchZoom by remember(page.pageNumber, riwayah) { mutableFloatStateOf(1f) }
            var panOffset by remember(page.pageNumber, riwayah) { mutableStateOf(Offset.Zero) }
            val effectiveScale = (baseScale * userPinchZoom).coerceIn(1.0f, 4.0f)

            if (!isImageError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(page.pageNumber, riwayah) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newZoom = (userPinchZoom * zoom).coerceIn(0.85f, 3.5f)
                                userPinchZoom = newZoom
                                val currentTotal = (baseScale * newZoom).coerceIn(1.0f, 4.0f)
                                if (currentTotal > 1.05f) {
                                    val maxPanX = (size.width * (currentTotal - 1f)) / 2f
                                    val maxPanY = (size.height * (currentTotal - 1f)) / 2f
                                    panOffset = Offset(
                                        x = (panOffset.x + pan.x).coerceIn(-maxPanX, maxPanX),
                                        y = (panOffset.y + pan.y).coerceIn(-maxPanY, maxPanY)
                                    )
                                } else {
                                    panOffset = Offset.Zero
                                }
                            }
                        }
                        .pointerInput(page.pageNumber, riwayah) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (userPinchZoom > 1.1f) {
                                        userPinchZoom = 1f
                                        panOffset = Offset.Zero
                                    } else {
                                        userPinchZoom = 1.8f
                                    }
                                    HapticFeedbackUtil.performLightTick(context)
                                },
                                onLongPress = {
                                    if (page.ayahs.size == 1) {
                                        onAyahLongPress(page.ayahs.first())
                                    } else if (page.ayahs.isNotEmpty()) {
                                        showAyahPicker = true
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(200)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = "صفحة المصحف الشريف ${page.pageNumber} (${riwayah.shortName})",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = effectiveScale
                                scaleY = effectiveScale
                                translationX = panOffset.x
                                translationY = panOffset.y
                            }
                            .clip(SquircleShapeMedium),
                        contentScale = ContentScale.Fit,
                        colorFilter = darkInvertFilter,
                        onLoading = {
                            isImageLoading = true
                            isImageError = false
                        },
                        onSuccess = {
                            isImageLoading = false
                            isImageError = false
                        },
                        onError = {
                            isImageLoading = false
                            isImageError = true
                        }
                    )

                    // Loading Spinner
                    if (isImageLoading) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(palette.pageColor.copy(alpha = 0.85f))
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = palette.accentColor,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "جاري فتح صفحة ${page.pageNumber} (${riwayah.labelArabic})...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = palette.textColor,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    // Floating Zoom Indicator & Quick Reset
                    if (effectiveScale > 1.05f) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(PillShape)
                                .background(Color.Black.copy(alpha = 0.75f))
                                .clickable {
                                    userPinchZoom = 1f
                                    panOffset = Offset.Zero
                                    HapticFeedbackUtil.performLightTick(context)
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "تكبير: ${(effectiveScale * 100).toInt()}%  ↺ إعادة",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Ayah picker for image mode
                if (showAyahPicker) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showAyahPicker = false },
                        confirmButton = {},
                        title = {
                            Text(
                                text = "اختر الآية للتفسير",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textDirection = TextDirection.Rtl
                                )
                            )
                        },
                        text = {
                            LazyColumn {
                                items(page.ayahs.size) { index ->
                                    val ayah = page.ayahs[index]
                                    Text(
                                        text = "﴿${ayah.ayahNumber}﴾ ${ayah.textUthmani.take(60)}${if (ayah.textUthmani.length > 60) "..." else ""}",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                showAyahPicker = false
                                                onAyahLongPress(ayah)
                                            }
                                            .padding(vertical = 10.dp, horizontal = 4.dp),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDirection = TextDirection.Rtl
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            } else {
                // Friendly error state with retry and fallback to digital reading
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "تعذر تحميل صفحة ${riwayah.labelArabic} (${page.pageNumber})",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textColor
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يرجى التحقق من الاتصال بالإنترنت، أو متابعة القراءة بالنص الرقمي المعتمد",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = palette.textColor.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            isImageError = false
                            isImageLoading = true
                        }
                    ) {
                        Text("إعادة المحاولة")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DigitalPageContent(
                        page = page,
                        palette = palette,
                        fontSizeSp = fontSizeSp,
                        lineSpacingMultiplier = lineSpacingMultiplier,
                        tashkeelContrast = tashkeelContrast,
                        tajweedVersion = tajweedVersion,
                        onAyahClick = onAyahClick,
                        onAyahLongPress = onAyahLongPress
                    )
                }
            }
        } else {
            DigitalPageContent(
                page = page,
                palette = palette,
                fontSizeSp = fontSizeSp,
                lineSpacingMultiplier = lineSpacingMultiplier,
                tashkeelContrast = tashkeelContrast,
                tajweedVersion = tajweedVersion,
                onAyahClick = onAyahClick,
                onAyahLongPress = onAyahLongPress
            )
        }
    }
}

@Composable
fun DigitalPageContent(
    page: WrittenQuranPage,
    palette: QuranPaletteType,
    fontSizeSp: Float,
    lineSpacingMultiplier: Float = 1.9f,
    tashkeelContrast: Boolean = false,
    tajweedVersion: Int = 0,
    onAyahClick: (WrittenAyah) -> Unit,
    onAyahLongPress: (WrittenAyah) -> Unit = {}
) {
    val context = LocalContext.current
    val isTajweedEnabled = remember(tajweedVersion) { TajweedSettings.isTajweedEnabled(context) }
    val tajweedTheme = remember(tajweedVersion) { TajweedSettings.getTajweedTheme(context) }
    val customTajweedColors = remember(tajweedVersion) { TajweedSettings.getAllCustomColors(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Inner ornate Mushaf border lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val insetPx = 6.dp.toPx()
            val w = size.width
            val h = size.height
            val color = palette.accentColor.copy(alpha = 0.35f)

            // Outer hairline rectangle
            drawRect(
                color = color,
                topLeft = Offset(insetPx, insetPx),
                size = androidx.compose.ui.geometry.Size(w - insetPx * 2, h - insetPx * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            val surahGroups = remember(page.ayahs) {
                page.ayahs.groupBy { it.surahNumber }
            }

            val headerSurahTitle = remember(surahGroups, page.surahNameArabic) {
                val names = surahGroups.keys.map { WrittenQuranCatalog.getSurahNameArabic(it) }
                if (names.isEmpty()) {
                    page.surahNameArabic
                } else if (names.size == 1) {
                    names.first()
                } else {
                    "${names.first()} - ${names.last()}"
                }
            }

            // Page Header (Juz, Surah, Hizb)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الجزء ${page.juzNumber}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )

                Text(
                    text = "سورة $headerSurahTitle",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = palette.textColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                )

                Text(
                    text = "الحزب ${(page.juzNumber * 2) - 1}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Ornate separator hairline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(palette.borderColor)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quran Text Content - Grouped by Surah
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                surahGroups.forEach { (surahNum, surahAyahs) ->
                    val hasSurahStart = surahAyahs.any { it.ayahNumber == 1 }
                    if (hasSurahStart) {
                        val surahName = WrittenQuranCatalog.getSurahNameArabic(surahNum)
                        Spacer(modifier = Modifier.height(4.dp))
                        SurahTitleCartouche(
                            surahName = surahName,
                            palette = palette
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (surahNum != 9 && surahNum != 1) {
                            BismillahBanner(palette = palette)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    val surahAnnotated = buildAnnotatedString {
                        surahAyahs.forEach { ayah ->
                            pushStringAnnotation(tag = "AYAH", annotation = "${ayah.surahNumber}_${ayah.ayahNumber}")
                            if (isTajweedEnabled) {
                                val tajweedPiece = TajweedParser.parseTajweed(
                                    text = ayah.textUthmani,
                                    theme = tajweedTheme,
                                    baseColor = if (tashkeelContrast) {
                                        if (palette.isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
                                    } else {
                                        palette.textColor
                                    },
                                    fontSize = fontSizeSp.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = if (tashkeelContrast) FontWeight.SemiBold else FontWeight.Normal,
                                    customColors = customTajweedColors
                                )
                                append(tajweedPiece)
                            } else {
                                withStyle(
                                    style = SpanStyle(
                                        color = if (tashkeelContrast) {
                                            if (palette.isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
                                        } else {
                                            palette.textColor
                                        },
                                        fontSize = fontSizeSp.sp,
                                        fontWeight = if (tashkeelContrast) FontWeight.SemiBold else FontWeight.Normal,
                                        fontFamily = FontFamily.Serif
                                    )
                                ) {
                                    append(ayah.textUthmani)
                                }
                            }

                            // Decorative Ayah end symbol
                            withStyle(
                                style = SpanStyle(
                                    color = palette.accentColor,
                                    fontSize = (fontSizeSp * 0.78f).sp,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(" " + ayah.decoratedNumber + " ")
                            }
                            pop()
                        }
                    }

                    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                    Text(
                        text = surahAnnotated,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * lineSpacingMultiplier).sp,
                            textAlign = TextAlign.Justify,
                            fontFamily = FontFamily.Serif,
                            textDirection = TextDirection.Rtl
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .pointerInput(surahAnnotated) {
                                detectTapGestures(
                                    onTap = { pos ->
                                        textLayoutResult?.let { layout ->
                                            val offset = layout.getOffsetForPosition(pos)
                                            surahAnnotated.getStringAnnotations(tag = "AYAH", start = offset, end = offset)
                                                .firstOrNull()?.let { annotation ->
                                                    val parts = annotation.item.split("_")
                                                    if (parts.size == 2) {
                                                        val sNum = parts[0].toIntOrNull()
                                                        val aNum = parts[1].toIntOrNull()
                                                        val clicked = surahAyahs.find { it.surahNumber == sNum && it.ayahNumber == aNum }
                                                        if (clicked != null) {
                                                            onAyahClick(clicked)
                                                        }
                                                    }
                                                }
                                        }
                                    },
                                    onLongPress = { pos ->
                                        textLayoutResult?.let { layout ->
                                            val offset = layout.getOffsetForPosition(pos)
                                            surahAnnotated.getStringAnnotations(tag = "AYAH", start = offset, end = offset)
                                                .firstOrNull()?.let { annotation ->
                                                    val parts = annotation.item.split("_")
                                                    if (parts.size == 2) {
                                                        val sNum = parts[0].toIntOrNull()
                                                        val aNum = parts[1].toIntOrNull()
                                                        val clicked = surahAyahs.find { it.surahNumber == sNum && it.ayahNumber == aNum }
                                                        if (clicked != null) {
                                                            onAyahLongPress(clicked)
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                )
                            },
                        onTextLayout = { textLayoutResult = it }
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Ornate separator hairline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(palette.borderColor)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Page Footer (Traditional Page Number Badge)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(palette.borderColor.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "- ${page.pageNumber} -",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = palette.textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Traditional Islamic calligraphic Surah Title Cartouche (إطار عنوان السورة).
 */
@Composable
fun SurahTitleCartouche(
    surahName: String,
    palette: QuranPaletteType
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SquircleShapeSmall)
            .background(palette.borderColor.copy(alpha = 0.45f))
            .border(1.5.dp, palette.accentColor, SquircleShapeSmall)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "❖",
                color = palette.accentColor,
                fontSize = 12.sp
            )
            Text(
                text = "سُورَةُ $surahName",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textColor,
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Serif
                )
            )
            Text(
                text = "❖",
                color = palette.accentColor,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Noble Basmalah banner at the beginning of Surahs.
 */
@Composable
fun BismillahBanner(palette: QuranPaletteType) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَـٰنِ الرَّحِيمِ",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = palette.textColor,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif
            )
        )
    }
}

/**
 * Top HUD Bar with Quick Tools & Book Info.
 */
@Composable
fun ReaderTopHud(
    currentPage: WrittenQuranPage,
    palette: QuranPaletteType,
    isDownloaded: Boolean,
    readingStyle: QuranReadingStyle,
    riwayah: WrittenQuranRiwayah = WrittenQuranRiwayah.HAFS,
    onToggleReadingStyle: () -> Unit,
    onClose: () -> Unit,
    onOpenIndex: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDownload: () -> Unit,
    onOpenTajweedGuide: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .shadow(12.dp, PillShape)
            .clip(PillShape)
            .background(palette.pageColor)
            .border(1.dp, palette.borderColor, PillShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Close button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = palette.textColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Settings & Customization button (pinned at top-left so it never gets clipped)
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "تخصيص المصحف والخط والرواية",
                        tint = palette.accentColor,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Title & Index trigger
            Row(
                modifier = Modifier
                    .clip(PillShape)
                    .clickable { onOpenIndex() }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = palette.accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Column(horizontalAlignment = Alignment.Start) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "سورة ${currentPage.surahNameArabic}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.textColor
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(palette.accentColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = riwayah.shortName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.accentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                    Text(
                        text = "${riwayah.labelArabic} • ص ${currentPage.pageNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = palette.textColor.copy(alpha = 0.65f),
                            fontSize = 9.sp
                        )
                    )
                }
            }

            // Action Icons Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Quick Style Toggle (Handwritten vs Digital)
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(palette.accentColor.copy(alpha = 0.15f))
                        .clickable { onToggleReadingStyle() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (readingStyle == QuranReadingStyle.AUTHENTIC_HANDWRITTEN) "مخطوط 📖" else "رقمي 🔤",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.accentColor,
                            fontSize = 11.sp
                        )
                    )
                }

                // Download button if not downloaded yet
                IconButton(
                    onClick = onOpenDownload,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isDownloaded) Icons.Default.Check else Icons.Default.CloudDownload,
                        contentDescription = "تحميل المصحف المكتوب كاملاً",
                        tint = if (isDownloaded) palette.accentColor else Color(0xFFEAB308),
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = palette.textColor,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onOpenBookmarks,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "العلامات المرجعية",
                        tint = palette.textColor,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Tajweed color guide
                IconButton(
                    onClick = onOpenTajweedGuide,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "دليل ومفتاح ألوان أحكام التجويد",
                        tint = palette.accentColor,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "المظهر والخط والرواية",
                        tint = palette.textColor,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

/**
 * Bottom HUD Bar with Page Slider, Turn Mode, and Quick Bookmark.
 */
@Composable
fun ReaderBottomHud(
    currentPageNumber: Int,
    palette: QuranPaletteType,
    pageTurnMode: PageTurnMode,
    onPageSeek: (Int) -> Unit,
    onTogglePageTurnMode: () -> Unit,
    onBookmarkCurrentPage: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    var isDragging by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(currentPageNumber.toFloat()) }

    LaunchedEffect(currentPageNumber) {
        if (!isDragging) {
            sliderValue = currentPageNumber.toFloat()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .shadow(12.dp, SquircleShapeLarge)
            .clip(SquircleShapeLarge)
            .background(palette.pageColor)
            .border(1.dp, palette.borderColor, SquircleShapeLarge)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Slider Row with Page Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ص ${sliderValue.toInt()}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.accentColor,
                        fontSize = 12.sp
                    )
                )

                Slider(
                    value = sliderValue,
                    onValueChange = {
                        isDragging = true
                        sliderValue = it
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        onPageSeek(sliderValue.toInt())
                    },
                    valueRange = 1f..604f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = palette.accentColor,
                        activeTrackColor = palette.accentColor,
                        inactiveTrackColor = palette.borderColor
                    )
                )

                Text(
                    text = "604",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.textColor.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons Row with Prominent Customization Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 3D vs Normal mode toggle button
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(palette.borderColor.copy(alpha = 0.45f))
                        .clickable { onTogglePageTurnMode() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (pageTurnMode == PageTurnMode.REALISTIC_3D) Icons.Default.Layers else Icons.Default.ViewCarousel,
                            contentDescription = null,
                            tint = palette.accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = pageTurnMode.labelArabic,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = palette.textColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // ponytail: prominent Quran customization button (Font, Riwayah, Palette)
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(palette.accentColor.copy(alpha = 0.18f))
                        .border(1.dp, palette.accentColor.copy(alpha = 0.6f), PillShape)
                        .clickable { onOpenSettings() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "تخصيص المصحف",
                            tint = palette.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "تخصيص المصحف ⚙️",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = palette.accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Bookmark page button
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(palette.accentColor.copy(alpha = 0.15f))
                        .clickable { onBookmarkCurrentPage() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = palette.accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "حفظ الصفحة",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = palette.accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Surahs (1-114) and Ajza' (1-30) Navigation Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranIndexSheet(
    palette: QuranPaletteType,
    onSelectSurah: (SurahPageIndex) -> Unit,
    onSelectJuz: (JuzIndex) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

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
                text = "فهرس المصحف الشريف",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.textColor
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Tabs: السور / الأجزاء
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = palette.pageColor,
                contentColor = palette.accentColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = palette.accentColor
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "السور (114)",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) palette.accentColor else palette.textColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "الأجزاء (30)",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) palette.accentColor else palette.textColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTab == 0) {
                // Surah search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("ابحث باسم السورة أو رقمها...", fontSize = 12.sp) },
                    singleLine = true,
                    shape = PillShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.accentColor,
                        unfocusedBorderColor = palette.borderColor,
                        focusedTextColor = palette.textColor,
                        unfocusedTextColor = palette.textColor
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filteredSurahs = remember(searchQuery) {
                    val q = WrittenQuranCatalog.normalizeArabic(searchQuery)
                    if (q.isBlank()) {
                        WrittenQuranCatalog.SURAH_PAGE_INDICES
                    } else {
                        WrittenQuranCatalog.SURAH_PAGE_INDICES.filter {
                            WrittenQuranCatalog.normalizeArabic(it.nameArabic).contains(q) ||
                                    it.number.toString().contains(q) ||
                                    it.nameEnglish.contains(q, ignoreCase = true)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredSurahs, key = { it.number }) { surah ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SquircleShapeSmall)
                                .background(palette.borderColor.copy(alpha = 0.3f))
                                .clickable { onSelectSurah(surah) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(palette.accentColor.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = surah.number.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = palette.accentColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Column {
                                    Text(
                                        text = "سورة ${surah.nameArabic}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = palette.textColor
                                        )
                                    )
                                    Text(
                                        text = "${if (surah.isMeccan) "مكية" else "مدنية"} • ${surah.ayahCount} آية",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = palette.textColor.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(palette.borderColor)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ص ${surah.pageNumber}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = palette.textColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // Juz list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(WrittenQuranCatalog.JUZ_INDICES, key = { it.juzNumber }) { juz ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SquircleShapeSmall)
                                .background(palette.borderColor.copy(alpha = 0.3f))
                                .clickable { onSelectJuz(juz) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = juz.nameArabic,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textColor
                                    )
                                )
                                Text(
                                    text = "يبدأ من سورة ${juz.startSurahName} (الآية ${juz.startAyahNumber})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = palette.textColor.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(palette.borderColor)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ص ${juz.startPageNumber}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = palette.textColor,
                                        fontWeight = FontWeight.Bold
                                    )
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
 * Saved Bookmarks Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksSheet(
    palette: QuranPaletteType,
    onSelectBookmark: (QuranBookmark) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var bookmarks by remember { mutableStateOf(WrittenQuranCatalog.getBookmarks(context)) }

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
                        text = "لا توجد علامات مرجعية محفوظة حتى الآن.\nيمكنك حفظ أي صفحة أو آية بضغطة زر.",
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
                                    text = "سورة ${bookmark.surahName} • آية ${bookmark.ayahNumber}",
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

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(palette.borderColor)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ص ${bookmark.pageNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = palette.textColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        WrittenQuranCatalog.removeBookmark(context, bookmark.id)
                                        bookmarks = WrittenQuranCatalog.getBookmarks(context)
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
}

/**
 * Diacritic-Tolerant Verse Search Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranSearchSheet(
    palette: QuranPaletteType,
    onSelectResult: (WrittenAyah) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<WrittenAyah>>(emptyList()) }

    LaunchedEffect(query) {
        if (query.trim().length >= 2) {
            results = WrittenQuranCatalog.searchVerses(query)
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
                text = "البحث في آيات المصحف الشريف",
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
                placeholder = { Text("اكتب كلمة للبحث (مثال: الصلاة، الجنة، الصبر...)", fontSize = 12.sp) },
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
                items(results) { ayah ->
                    val surah = WrittenQuranCatalog.SURAH_PAGE_INDICES.find { it.number == ayah.surahNumber }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleShapeSmall)
                            .background(palette.borderColor.copy(alpha = 0.3f))
                            .clickable { onSelectResult(ayah) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "سورة ${surah?.nameArabic ?: ""} • آية ${ayah.ayahNumber}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.accentColor
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = ayah.textUthmani,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = palette.textColor,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Serif
                                ),
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(palette.borderColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ص ${ayah.pageNumber}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.textColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reader Customization Sheet: Reading Style, Riwayah, Palettes, Page Turn Mode, Font Scaling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    palette: QuranPaletteType,
    pageTurnMode: PageTurnMode,
    fontSizeSp: Float,
    lineSpacingMultiplier: Float,
    tashkeelContrast: Boolean,
    readingStyle: QuranReadingStyle,
    riwayah: WrittenQuranRiwayah,
    onPaletteChange: (QuranPaletteType) -> Unit,
    onModeChange: (PageTurnMode) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onLineSpacingChange: (Float) -> Unit,
    onTashkeelContrastChange: (Boolean) -> Unit,
    onReadingStyleChange: (QuranReadingStyle) -> Unit,
    onRiwayahChange: (WrittenQuranRiwayah) -> Unit,
    onOpenTajweedGuide: () -> Unit = {},
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "إعدادات المصحف المقروء",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.textColor
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 1. Reading Style: Authentic Calligraphy vs Digital
            Text(
                text = "طريقة عرض المصحف الشريف:",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.accentColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuranReadingStyle.entries.forEach { style ->
                val isSelected = style == readingStyle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(SquircleShapeSmall)
                        .background(if (isSelected) palette.accentColor.copy(alpha = 0.15f) else palette.borderColor.copy(alpha = 0.25f))
                        .clickable { onReadingStyleChange(style) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = style.labelArabic,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = palette.textColor
                            )
                        )
                        Text(
                            text = style.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textColor.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        )
                    }
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

            // 2. Quranic Riwayah Selection (All Qira'at: Hafs, Warsh, Qaloon, Doori)
            Text(
                text = "الرواية القرآنية والقراءات المتواترة:",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.accentColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            WrittenQuranRiwayah.entries.forEach { riw ->
                val isSelected = riw == riwayah
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(SquircleShapeSmall)
                        .background(if (isSelected) palette.accentColor.copy(alpha = 0.15f) else palette.borderColor.copy(alpha = 0.25f))
                        .clickable { onRiwayahChange(riw) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = riw.labelArabic,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = palette.textColor
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(palette.accentColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = riw.shortName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = palette.accentColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = riw.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textColor.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        )
                        if (riw.geographicalSpread.isNotBlank()) {
                            Text(
                                text = "مواطن الانتشار: ${riw.geographicalSpread}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.accentColor.copy(alpha = 0.85f),
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
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

            // 3. Eye-care Palettes
            Text(
                text = "نمط ألوان ورق المصحف (لحماية العين):",
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(pal.pageColor)
                                .border(1.5.dp, pal.accentColor, CircleShape)
                        )

                        Column {
                            Text(
                                text = pal.labelArabic,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = palette.textColor
                                )
                            )
                            Text(
                                text = pal.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = palette.textColor.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

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

            // 4. Font Size Scaling (18sp to 42sp for elder comfort)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (readingStyle == QuranReadingStyle.AUTHENTIC_HANDWRITTEN) {
                        "تكبير المصحف المخطوط الأصلي: ${((fontSizeSp / 24f) * 100).toInt()}%"
                    } else {
                        "حجم الخط العثماني: ${fontSizeSp.toInt()} sp"
                    },
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
                            .clickable { onFontSizeChange((fontSizeSp - 2f).coerceAtLeast(18f)) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("A-", fontWeight = FontWeight.Bold, color = palette.textColor)
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(palette.borderColor)
                            .clickable { onFontSizeChange((fontSizeSp + 2f).coerceAtMost(42f)) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("A+", fontWeight = FontWeight.Bold, color = palette.textColor)
                    }
                }
            }

            Slider(
                value = fontSizeSp,
                onValueChange = onFontSizeChange,
                valueRange = 18f..42f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = palette.accentColor,
                    activeTrackColor = palette.accentColor,
                    inactiveTrackColor = palette.borderColor
                )
            )

            if (readingStyle == QuranReadingStyle.AUTHENTIC_HANDWRITTEN) {
                Text(
                    text = "💡 نصيحة: يمكنك أيضاً تقريب وإزاحة صفحة المخطوطة بحرية مباشرة بإصبعين (Pinch-to-zoom) أو بالنقر المزدوج على الشاشة.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = palette.textColor.copy(alpha = 0.65f),
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Line Spacing (تباعد الأسطر لراحة القراءة)
            Text(
                text = "تباعد الأسطر (راحة العينين):",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.accentColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            val spacingOptions = listOf(
                Triple(1.6f, "مضغوط", "عرض مكثف للآيات في الصفحة الواحدة"),
                Triple(1.9f, "متوازن قياسي (موصى به)", "التباعد المعتمد في طباعة المصاحف الكلاسيكية"),
                Triple(2.3f, "واسع مريح (لكبار السن)", "تباعد رحب يمنع تداخل الأسطر والحركات ويوفر راحة قصوى")
            )

            spacingOptions.forEach { (factor, title, desc) ->
                val isSelected = (lineSpacingMultiplier - factor).absoluteValue < 0.05f
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(SquircleShapeSmall)
                        .background(if (isSelected) palette.accentColor.copy(alpha = 0.15f) else palette.borderColor.copy(alpha = 0.25f))
                        .clickable { onLineSpacingChange(factor) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = palette.textColor
                            )
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textColor.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        )
                    }
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

            // 6. Tashkeel Contrast Toggle (تباين وضبط الحركات)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SquircleShapeSmall)
                    .background(if (tashkeelContrast) palette.accentColor.copy(alpha = 0.15f) else palette.borderColor.copy(alpha = 0.25f))
                    .clickable { onTashkeelContrastChange(!tashkeelContrast) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تباين وضبط الحركات والتشكيل",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (tashkeelContrast) FontWeight.Bold else FontWeight.Normal,
                            color = palette.textColor
                        )
                    )
                    Text(
                        text = "إبراز الحركات وعلامات الضبط بخط أوضح وأغمق لتيسير القراءة وضبط الإعراب",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = palette.textColor.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    )
                }

                Switch(
                    checked = tashkeelContrast,
                    onCheckedChange = onTashkeelContrastChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = palette.accentColor,
                        checkedTrackColor = palette.accentColor.copy(alpha = 0.35f),
                        uncheckedThumbColor = palette.borderColor,
                        uncheckedTrackColor = palette.borderColor.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Page Turn Physics Mode
            Text(
                text = "طريقة تقليب الصفحات:",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.accentColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            PageTurnMode.entries.forEach { mode ->
                val isSelected = mode == pageTurnMode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(SquircleShapeSmall)
                        .background(if (isSelected) palette.accentColor.copy(alpha = 0.15f) else palette.borderColor.copy(alpha = 0.25f))
                        .clickable { onModeChange(mode) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = mode.labelArabic,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = palette.textColor
                            )
                        )
                        Text(
                            text = mode.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textColor.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        )
                    }
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

            // 8. Tajweed Guide & Custom Color Map (أحكام التجويد وخريطة ومفتاح الألوان)
            Text(
                text = "أحكام التجويد والترميز اللوني:",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.accentColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SquircleShapeSmall)
                    .background(palette.accentColor.copy(alpha = 0.15f))
                    .clickable { onOpenTajweedGuide() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = palette.accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "خريطة ومفتاح ألوان أحكام التجويد",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.textColor
                            )
                        )
                        Text(
                            text = "تخصيص ألوان المدود، الغنن، القلقلة، والإخفاء مع دليل الشرح وقواعد التلاوة",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textColor.copy(alpha = 0.65f),
                                fontSize = 10.sp
                            )
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = palette.accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Download Full Quran Dialog (604 pages from verified public repo).
 */
@Composable
fun DownloadFullQuranDialog(
    palette: QuranPaletteType,
    isDownloaded: Boolean,
    downloadState: WrittenQuranDownloadState,
    riwayah: WrittenQuranRiwayah = WrittenQuranRiwayah.HAFS,
    onStartDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تحميل المصحف المكتوب (${riwayah.labelArabic})",
                fontWeight = FontWeight.Bold,
                color = palette.textColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isDownloaded) {
                    Text(
                        text = "المصحف الشريف المكتوب بالرسم العثماني (604 صفحات كاملة) مُحمّل بالفعل ومتاح للاستخدام بدون إنترنت في أي وقت.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = palette.textColor)
                    )
                } else {
                    Text(
                        text = "تنزيل كامل نص المصحف الشريف بالرسم العثماني المدني (604 صفحة / 114 سورة) من المستودع المفتوح المعتمد (Tanzil / AlQuran Cloud) للقراءة والبحث بدون إنترنت.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = palette.textColor)
                    )

                    if (downloadState is WrittenQuranDownloadState.Downloading) {
                        val d = downloadState as WrittenQuranDownloadState.Downloading
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = d.statusMessage,
                            style = MaterialTheme.typography.labelSmall.copy(color = palette.accentColor)
                        )
                        LinearProgressIndicator(
                            progress = { d.progressFraction },
                            modifier = Modifier.fillMaxWidth(),
                            color = palette.accentColor
                        )
                    } else if (downloadState is WrittenQuranDownloadState.Error) {
                        val e = downloadState as WrittenQuranDownloadState.Error
                        Text(
                            text = e.message,
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFEF4444))
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isDownloaded && downloadState !is WrittenQuranDownloadState.Downloading) {
                TextButton(onClick = onStartDownload) {
                    Text("بدء التحميل", color = palette.accentColor, fontWeight = FontWeight.Bold)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("إغلاق", color = palette.accentColor)
                }
            }
        },
        dismissButton = {
            if (!isDownloaded) {
                TextButton(onClick = onDismiss) {
                    Text("إلغاء", color = palette.textColor.copy(alpha = 0.6f))
                }
            }
        }
    )
}

/**
 * Bottom Sheet for interacting with a tapped Ayah.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahActionSheet(
    ayah: WrittenAyah,
    palette: QuranPaletteType,
    onDismiss: () -> Unit,
    onBookmark: () -> Unit,
    onCopy: () -> Unit,
    onTafsir: () -> Unit = {}
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
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "الآية رقم ${ayah.ayahNumber}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.accentColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ayah.textUthmani,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                    color = palette.textColor
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tafsir action
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(SquircleShapeSmall)
                        .background(palette.accentColor)
                        .clickable { onTafsir() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "التفسير",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                // Bookmark action
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(SquircleShapeSmall)
                        .background(palette.accentColor.copy(alpha = 0.15f))
                        .clickable { onBookmark() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = palette.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "علامة",
                            fontWeight = FontWeight.Bold,
                            color = palette.accentColor,
                            fontSize = 12.sp
                        )
                    }
                }

                // Copy action
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(SquircleShapeSmall)
                        .background(palette.borderColor.copy(alpha = 0.5f))
                        .clickable { onCopy() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = palette.textColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "نسخ",
                            fontWeight = FontWeight.Bold,
                            color = palette.textColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Beautiful Bottom Sheet for Ayah Tafsir (التفسير والبيان لآيات الذكر الحكيم).
 * Displays:
 * - Full Arabic Ayah with Tashkeel in ornate Islamic frame
 * - Tabs for authentic Tafsir (التفسير الميسر / تفسير الجلالين / تفسير السعدي)
 * - Quick actions: Copy Ayah & Tafsir, Share Ayah, Bookmark
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahTafsirBottomSheet(
    ayah: WrittenAyah,
    surahName: String,
    palette: QuranPaletteType,
    fontSizeSp: Float,
    onDismiss: () -> Unit,
    onBookmark: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedEdition by remember {
        mutableStateOf(TafsirCatalog.getPreferredEdition(context))
    }
    var currentTafsir by remember { mutableStateOf<AyahTafsir?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(ayah.surahNumber, ayah.ayahNumber, selectedEdition) {
        isLoading = true
        currentTafsir = TafsirCatalog.getTafsir(
            surahNumber = ayah.surahNumber,
            ayahNumber = ayah.ayahNumber,
            edition = selectedEdition,
            surahName = surahName,
            ayahText = ayah.textUthmani,
            context = context
        )
        isLoading = false
    }

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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = palette.textColor
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "تفسير وبيان الآية الكريمة",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textColor
                        )
                    )
                    Text(
                        text = "سورة $surahName • الآية ${ayah.ayahNumber}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = palette.accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ornate Ayah Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SquircleShapeMedium)
                    .background(palette.borderColor.copy(alpha = 0.35f))
                    .border(1.5.dp, palette.accentColor.copy(alpha = 0.6f), SquircleShapeMedium)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${ayah.textUthmani}${ayah.decoratedNumber}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontSize = (fontSizeSp.coerceIn(18f, 26f)).sp,
                        textAlign = TextAlign.Center,
                        lineHeight = (fontSizeSp.coerceIn(18f, 26f) * 1.8f).sp,
                        color = palette.textColor,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tafsir Edition Tabs
            TabRow(
                selectedTabIndex = TafsirEdition.entries.indexOf(selectedEdition).coerceAtLeast(0),
                containerColor = palette.pageColor,
                contentColor = palette.accentColor,
                indicator = { tabPositions ->
                    val idx = TafsirEdition.entries.indexOf(selectedEdition).coerceIn(0, tabPositions.lastIndex)
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[idx]),
                        color = palette.accentColor
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SquircleShapeSmall)
            ) {
                TafsirEdition.entries.forEach { edition ->
                    val isSelected = edition == selectedEdition
                    Tab(
                        selected = isSelected,
                        onClick = {
                            selectedEdition = edition
                            TafsirCatalog.savePreferredEdition(context, edition)
                        },
                        text = {
                            Text(
                                text = edition.nameArabic,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) palette.accentColor else palette.textColor.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                ),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tafsir Content Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SquircleShapeMedium)
                    .background(palette.backgroundColor.copy(alpha = 0.6f))
                    .border(1.dp, palette.borderColor, SquircleShapeMedium)
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = palette.accentColor,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            text = "جاري استحضار التفسير والبيان...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textColor.copy(alpha = 0.7f)
                            )
                        )
                    }
                } else {
                    val tafsir = currentTafsir
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المصدر: ${selectedEdition.author}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.accentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                            if (tafsir?.isOfflineAvailable == true) {
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(palette.accentColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "متاح بدون إنترنت ✓",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = palette.accentColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Text(
                            text = tafsir?.tafsirText ?: "تعذر استحضار التفسير حالياً",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = palette.textColor,
                                lineHeight = 26.sp,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Start
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Bookmark, Copy, Share
            val fullTextToShare = remember(ayah, surahName, currentTafsir, selectedEdition) {
                buildString {
                    append("﴿ ${ayah.textUthmani} ﴾ [سورة $surahName: ${ayah.ayahNumber}]\n\n")
                    append("📖 ${selectedEdition.nameArabic}:\n")
                    append(currentTafsir?.tafsirText ?: "")
                    append("\n\n— عبر تطبيق المحاسب القرآني")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Bookmark
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(SquircleShapeSmall)
                        .background(palette.accentColor.copy(alpha = 0.15f))
                        .clickable { onBookmark() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = palette.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "حفظ علامة",
                            fontWeight = FontWeight.Bold,
                            color = palette.accentColor,
                            fontSize = 13.sp
                        )
                    }
                }

                // 2. Copy
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(SquircleShapeSmall)
                        .background(palette.borderColor.copy(alpha = 0.5f))
                        .clickable { onCopy(fullTextToShare) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = palette.textColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "نسخ التفسير",
                            fontWeight = FontWeight.Bold,
                            color = palette.textColor,
                            fontSize = 13.sp
                        )
                    }
                }

                // 3. Share
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(SquircleShapeSmall)
                        .background(palette.accentColor)
                        .clickable { onShare(fullTextToShare) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "مشاركة",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

