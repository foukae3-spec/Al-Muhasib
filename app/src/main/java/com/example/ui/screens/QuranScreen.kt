package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quran.BatchDownloadProgress
import com.example.quran.QuranPlayerState
import com.example.quran.Reciter
import com.example.quran.ReciterCatalog
import com.example.quran.RepeatMode
import com.example.quran.SleepTimerDuration
import com.example.quran.Surah
import com.example.quran.SurahDownloadState
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeLarge
import com.example.ui.components.SquircleShapeMedium
import com.example.ui.components.SquircleShapeSmall
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.SpiritualEmerald
import com.example.ui.theme.SpiritualEmeraldBright
import com.example.ui.theme.SpiritualEmeraldSoft
import com.example.ui.viewmodel.QuranViewModel
import com.example.quran.book.WrittenQuranCatalog
import com.example.ui.screens.QuranBookReaderScreen
import com.example.ui.screens.HadithLibraryScreen
import kotlinx.coroutines.flow.StateFlow
import com.example.util.HapticFeedbackUtil
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current

    val selectedReciter by viewModel.selectedReciter.collectAsStateWithLifecycle()
    val surahs by viewModel.surahs.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val downloadStates by viewModel.downloadStates.collectAsStateWithLifecycle()
    val isBatchDownloading by viewModel.isBatchDownloading.collectAsStateWithLifecycle()
    val batchProgress by viewModel.batchProgress.collectAsStateWithLifecycle()
    val downloadedSurahs by viewModel.downloadedSurahIds.collectAsStateWithLifecycle()
    val downloadedCount by viewModel.downloadedCount.collectAsStateWithLifecycle()

    var showWrittenQuranReader by remember { mutableStateOf(false) }
    var showHadithLibrary by remember { mutableStateOf(false) }
    var showReciterDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showFullPlayerSheet by remember { mutableStateOf(false) }

    LaunchedEffect(showWrittenQuranReader, showHadithLibrary) {
        viewModel.setIsFullscreenReaderActive(showWrittenQuranReader || showHadithLibrary)
    }

    val lastReadPage = remember(showWrittenQuranReader) { WrittenQuranCatalog.getLastReadPage(context) }
    val lastReadSurah = remember(showWrittenQuranReader) { WrittenQuranCatalog.getLastReadSurahName(context) }

    val onPlaySurah = remember(viewModel) { { surah: Surah -> viewModel.playSurah(surah) } }
    val onTogglePlay = remember(viewModel) { { viewModel.togglePlayPause() } }
    val onDownloadSurah = remember(viewModel) { { surah: Surah -> viewModel.downloadSurah(surah) } }
    val onDeleteSurah = remember(viewModel) { { surah: Surah -> viewModel.deleteSurah(surah) } }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header: Title & Reciter Selector
            QuranTopHeader(
                selectedReciter = selectedReciter,
                downloadedCount = downloadedCount,
                onOpenReciterPicker = {
                    HapticFeedbackUtil.performLightTick(context)
                    showReciterDialog = true
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Surah Search Filter (Always pinned at top for instant filtering)
            QuranSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Surahs List (Zero disk I/O, memoized callbacks, hardware-accelerated list rendering)
            // ponytail: move hero buttons & banner inside LazyColumn so large system fonts never push Surah list off screen
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = if (playerState.currentSurah != null) 160.dp else 100.dp)
            ) {
                // Prominent Written Quran & Hadith Library Hero Buttons
                item(key = "hero_buttons") {
                    QuranLibraryHeroButtons(
                        lastReadPage = lastReadPage,
                        lastReadSurah = lastReadSurah,
                        onOpenWrittenQuran = {
                            HapticFeedbackUtil.performMechanicalClick(context)
                            showWrittenQuranReader = true
                        },
                        onOpenHadithLibrary = {
                            HapticFeedbackUtil.performMechanicalClick(context)
                            showHadithLibrary = true
                        }
                    )
                }

                // One-Click Full Quran Download Banner
                item(key = "batch_download_banner") {
                    BatchDownloadBanner(
                        isBatchDownloading = isBatchDownloading,
                        batchProgress = batchProgress,
                        downloadedCount = downloadedCount,
                        onStartBatchDownload = {
                            HapticFeedbackUtil.performMechanicalClick(context)
                            viewModel.startFullQuranDownload()
                        },
                        onCancelBatchDownload = {
                            HapticFeedbackUtil.performLightTick(context)
                            viewModel.cancelBatchDownload()
                        }
                    )
                }
                items(surahs, key = { it.number }, contentType = { "surah" }) { surah ->
                    val isPlayingThis = playerState.currentSurah?.number == surah.number
                    val isDownloaded = surah.number in downloadedSurahs
                    val downloadKey = "${selectedReciter.id}_${surah.number}"
                    val downloadState = downloadStates[downloadKey] ?: if (isDownloaded) SurahDownloadState.Downloaded else SurahDownloadState.NotDownloaded

                    SurahListItem(
                        surah = surah,
                        isPlaying = isPlayingThis && playerState.isPlaying,
                        isCurrentTrack = isPlayingThis,
                        downloadState = downloadState,
                        onPlayClick = {
                            HapticFeedbackUtil.performLightTick(context)
                            if (isPlayingThis) {
                                onTogglePlay()
                            } else {
                                onPlaySurah(surah)
                            }
                        },
                        onDownloadClick = {
                            HapticFeedbackUtil.performLightTick(context)
                            onDownloadSurah(surah)
                        },
                        onDeleteClick = {
                            HapticFeedbackUtil.performLightTick(context)
                            onDeleteSurah(surah)
                        }
                    )
                }
            }
        }

        // Sticky Bottom Mini Player
        AnimatedVisibility(
            visible = playerState.currentSurah != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            QuranMiniPlayer(
                playerState = playerState,
                positionFlow = viewModel.playbackPositionMs,
                onTogglePlay = {
                    HapticFeedbackUtil.performLightTick(context)
                    onTogglePlay()
                },
                onSkipNext = {
                    HapticFeedbackUtil.performLightTick(context)
                    viewModel.skipNext()
                },
                onOpenFullPlayer = {
                    HapticFeedbackUtil.performLightTick(context)
                    showFullPlayerSheet = true
                }
            )
        }
    }

    // Reciters Picker Dialog
    if (showReciterDialog) {
        ReciterPickerDialog(
            currentReciter = selectedReciter,
            onSelect = { reciter ->
                viewModel.selectReciter(reciter)
                showReciterDialog = false
            },
            onDismiss = { showReciterDialog = false }
        )
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentState = playerState,
            onSelectDuration = { duration ->
                viewModel.setSleepTimer(duration)
                showSleepTimerDialog = false
            },
            onDismiss = { showSleepTimerDialog = false }
        )
    }

    // Full Screen / Bottom Sheet Player
    if (showFullPlayerSheet && playerState.currentSurah != null) {
        ModalBottomSheet(
            onDismissRequest = { showFullPlayerSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = appColors.surface,
            scrimColor = Color(0x99000000)
        ) {
            QuranFullPlayerSheet(
                playerState = playerState,
                positionFlow = viewModel.playbackPositionMs,
                onTogglePlay = { viewModel.togglePlayPause() },
                onSkipNext = { viewModel.skipNext() },
                onSkipPrevious = { viewModel.skipPrevious() },
                onSeekTo = { viewModel.seekTo(it) },
                onToggleRepeat = {
                    val nextMode = when (playerState.repeatMode) {
                        RepeatMode.OFF -> RepeatMode.ALL
                        RepeatMode.ALL -> RepeatMode.ONE
                        RepeatMode.ONE -> RepeatMode.OFF
                    }
                    viewModel.setRepeatMode(nextMode)
                },
                onOpenSleepTimer = { showSleepTimerDialog = true },
                onClose = { showFullPlayerSheet = false }
            )
        }
    }

    // Full-Screen Written Quran Book Reader (المصحف المقروء)
    if (showWrittenQuranReader) {
        QuranBookReaderScreen(
            onClose = {
                showWrittenQuranReader = false
                viewModel.setIsFullscreenReaderActive(false)
            }
        )
    }

    // Full-Screen Authentic Seerah & As-Sahihayn Library (مكتبة الصحيحين والسيرة)
    if (showHadithLibrary) {
        HadithLibraryScreen(
            onClose = {
                showHadithLibrary = false
                viewModel.setIsFullscreenReaderActive(false)
            }
        )
    }
}

/**
 * Prominent Written Quran & Compact Hadith Library Hero Buttons.
 */
@Composable
fun QuranLibraryHeroButtons(
    lastReadPage: Int,
    lastReadSurah: String,
    onOpenWrittenQuran: () -> Unit,
    onOpenHadithLibrary: () -> Unit
) {
    val appColors = LocalAppColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prominent Primary Button: المصحف الشريف المقروء
        Box(
            modifier = Modifier
                .weight(1.35f)
                .clip(SquircleShapeLarge)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (appColors.isDark) Color(0xFF0F3826) else Color(0xFFD1FAE5),
                            if (appColors.isDark) Color(0xFF082217) else Color(0xFFA7F3D0)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(SpiritualEmeraldBright, SpiritualEmerald)
                    ),
                    shape = SquircleShapeLarge
                )
                .clickable { onOpenWrittenQuran() }
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(SpiritualEmeraldBright, SpiritualEmerald))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(if (appColors.isDark) Color(0x3310B981) else Color(0x4410B981))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "3D واقعي",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (appColors.isDark) SpiritualEmeraldBright else Color(0xFF065F46),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "المصحف المقروء",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (appColors.isDark) Color(0xFFECFDF5) else Color(0xFF064E3B),
                        fontSize = 15.sp
                    )
                )

                Text(
                    text = "تابع: سورة $lastReadSurah ص $lastReadPage",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (appColors.isDark) SpiritualEmeraldBright.copy(alpha = 0.9f) else Color(0xFF047857),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Compact Secondary Button: مكتبة الصحيحين والسيرة
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(SquircleShapeLarge)
                .background(
                    if (appColors.isDark) Color(0xFF131D17) else Color(0xFFF1F5F9)
                )
                .border(
                    width = 1.dp,
                    color = if (appColors.isDark) Color(0x3310B981) else Color(0xFFCBD5E1),
                    shape = SquircleShapeLarge
                )
                .clickable { onOpenHadithLibrary() }
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (appColors.isDark) Color(0x2210B981) else Color(0x220284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = SpiritualEmeraldBright,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(if (appColors.isDark) Color(0x22F59E0B) else Color(0x33F59E0B))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "موثقة",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "السنة والسيرة",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary,
                        fontSize = 13.sp
                    )
                )

                Text(
                    text = "الصحيحان والرحيق",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = appColors.textSecondary,
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Top Header with Title and Reciter Switcher.
 */
@Composable
fun QuranTopHeader(
    selectedReciter: Reciter,
    downloadedCount: Int,
    onOpenReciterPicker: () -> Unit
) {
    val appColors = LocalAppColors.current

    AppleGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = SquircleShapeLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "القرآن الكريم",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        )
                    )
                    Text(
                        text = "تلاوات عذبة بجودة استوديو بدون إنترنت وبث مباشر",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = appColors.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(if (appColors.isDark) Color(0x3310B981) else Color(0x2210B981))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$downloadedCount / 114 محلياً",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SpiritualEmeraldBright,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reciter Switcher Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SquircleShapeMedium)
                    .background(appColors.inset)
                    .clickable { onOpenReciterPicker() }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(SpiritualEmeraldBright, SpiritualEmerald))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedReciter.nameArabic,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            if (selectedReciter.id == 1) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(الافتراضي)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SpiritualEmeraldBright,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = "${selectedReciter.rewayaArabic} • ${selectedReciter.nameEnglish}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = appColors.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(Color(0x1F10B981))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "تغيير القارئ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SpiritualEmeraldBright,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

/**
 * One-Click Batch Download Banner for the Complete 114 Surahs.
 */
@Composable
fun BatchDownloadBanner(
    isBatchDownloading: Boolean,
    batchProgress: BatchDownloadProgress?,
    downloadedCount: Int,
    onStartBatchDownload: () -> Unit,
    onCancelBatchDownload: () -> Unit
) {
    val appColors = LocalAppColors.current
    val isAllDownloaded = downloadedCount >= 114

    AppleGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = SquircleShapeMedium,
        accentGlow = if (isBatchDownloading) SpiritualEmeraldBright else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (isBatchDownloading && batchProgress != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "جاري تحميل المصحف كاملاً (${batchProgress.completedCount}/114)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary
                            )
                        )
                        Text(
                            text = "تحميل: ${batchProgress.currentSurahName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SpiritualEmeraldBright,
                                fontSize = 11.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = onCancelBatchDownload,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إلغاء التحميل",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { batchProgress.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SpiritualEmeraldBright,
                    trackColor = Color(0x3310B981),
                    strokeCap = StrokeCap.Round
                )
            } else if (isAllDownloaded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SpiritualEmeraldBright,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "تم تحميل المصحف كاملاً (114 سورة) متوفر أوفلاين بالكامل",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpiritualEmeraldBright
                        )
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تحميل المصحف كاملاً للمصحف الحالي",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary
                            )
                        )
                        Text(
                            text = "زر واحد لحفظ جميع السور الـ 114 للاستماع في أي وقت بدون إنترنت",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = appColors.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SpiritualEmerald, SpiritualEmeraldBright)
                                )
                            )
                            .clickable { onStartBatchDownload() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "تحميل الكل",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.Black,
                                    fontWeight = FontWeight.ExtraBold
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
 * Filter / Search Bar.
 */
@Composable
fun QuranSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val appColors = LocalAppColors.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        placeholder = {
            Text(
                text = "ابحث برقم أو اسم السورة (مثلاً: البقرة، الكهف، 018...)",
                style = MaterialTheme.typography.bodySmall.copy(color = appColors.textSecondary, fontSize = 12.sp)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = appColors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "مسح",
                        tint = appColors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = PillShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SpiritualEmeraldBright,
            unfocusedBorderColor = appColors.glassBorder,
            focusedContainerColor = appColors.inset,
            unfocusedContainerColor = appColors.inset,
            focusedTextColor = appColors.textPrimary,
            unfocusedTextColor = appColors.textPrimary
        )
    )
}

/**
 * Single Surah Row Item.
 */
@Composable
fun SurahListItem(
    surah: Surah,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    downloadState: SurahDownloadState,
    onPlayClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val appColors = LocalAppColors.current

    val cardBackground = if (isCurrentTrack) {
        if (appColors.isDark) Color(0x2A10B981) else Color(0x1810B981)
    } else {
        appColors.glassCard
    }
    val cardBorder = if (isCurrentTrack) {
        SpiritualEmeraldBright.copy(alpha = 0.6f)
    } else {
        appColors.glassBorder
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SquircleShapeMedium)
            .background(cardBackground)
            .border(1.dp, cardBorder, SquircleShapeMedium)
            .clickable(onClick = onPlayClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Play/Pause button + Number badge
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCurrentTrack) {
                                Brush.radialGradient(listOf(SpiritualEmeraldBright, SpiritualEmerald))
                            } else {
                                Brush.linearGradient(listOf(appColors.inset, appColors.inset))
                            }
                        )
                        .border(
                            1.dp,
                            if (isCurrentTrack) SpiritualEmeraldBright else appColors.glassBorder,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "إيقاف",
                            tint = if (isCurrentTrack) Color.Black else appColors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "تشغيل",
                            tint = if (isCurrentTrack) Color.Black else appColors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${surah.formattedNumber}. سورة ${surah.nameArabic}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentTrack) SpiritualEmeraldBright else appColors.textPrimary
                            ),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${surah.nameEnglish} • ${surah.ayahCount} آية • ${surah.revelationTypeArabic}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = appColors.textSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Offline status & Download button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when (downloadState) {
                    is SurahDownloadState.Downloaded -> {
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(Color(0x2210B981))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "محلي",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SpiritualEmeraldBright,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "حذف التنزيل",
                                tint = appColors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    is SurahDownloadState.Downloading -> {
                        CircularProgressIndicator(
                            progress = { downloadState.progress },
                            modifier = Modifier.size(20.dp),
                            color = SpiritualEmeraldBright,
                            strokeWidth = 2.dp
                        )
                    }

                    is SurahDownloadState.Failed -> {
                        IconButton(
                            onClick = onDownloadClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "إعادة المحاولة",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    SurahDownloadState.NotDownloaded -> {
                        IconButton(
                            onClick = onDownloadClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "تحميل السورة",
                                tint = appColors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Isolated hairline progress bar for MiniPlayer.
 * Collecting [positionFlow] here prevents the entire MiniPlayer from recomposing every 200ms.
 */
@Composable
fun MiniPlayerScrubber(
    positionFlow: StateFlow<Long>,
    durationMs: Long,
    modifier: Modifier = Modifier
) {
    val positionMs by positionFlow.collectAsStateWithLifecycle()
    val progress = remember(positionMs, durationMs) {
        if (durationMs > 0L) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp),
        color = SpiritualEmeraldBright,
        trackColor = Color(0x2210B981)
    )
}

/**
 * Sticky Bottom Mini Player pinned above the navigation bar.
 */
@Composable
fun QuranMiniPlayer(
    playerState: QuranPlayerState,
    positionFlow: StateFlow<Long>,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onOpenFullPlayer: () -> Unit
) {
    val appColors = LocalAppColors.current
    val surah = playerState.currentSurah ?: return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = SquircleShapeLarge,
            elevation = 12.dp,
            accentGlow = SpiritualEmeraldBright,
            onClick = onOpenFullPlayer
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Progress indicator hairline isolated in leaf composable
                MiniPlayerScrubber(
                    positionFlow = positionFlow,
                    durationMs = playerState.durationMs
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(SpiritualEmeraldBright, SpiritualEmerald))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "سورة ${surah.nameArabic}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${playerState.currentReciter.nameArabic} • ${if (playerState.isLocalFile) "بدون إنترنت" else "بث مباشر"}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = appColors.textSecondary,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier.size(38.dp)
                        ) {
                            if (playerState.isBuffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = SpiritualEmeraldBright,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playerState.isPlaying) "إيقاف مؤقت" else "تشغيل",
                                    tint = SpiritualEmeraldBright,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onSkipNext,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "السورة التالية",
                                tint = appColors.textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Isolated scrubber slider and timestamp indicators for FullPlayerSheet.
 * Collecting [positionFlow] here prevents the large disk, reciter details, and playback
 * control buttons from recomposing on every 200ms audio clock tick.
 */
@Composable
fun FullPlayerScrubber(
    positionFlow: StateFlow<Long>,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val currentPositionMs by positionFlow.collectAsStateWithLifecycle()

    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    val displayPositionMs = if (isDraggingSlider) {
        (sliderPosition * durationMs).toLong()
    } else {
        currentPositionMs
    }

    val sliderValue = if (isDraggingSlider) {
        sliderPosition
    } else {
        if (durationMs > 0L) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = sliderValue,
            onValueChange = {
                isDraggingSlider = true
                sliderPosition = it
            },
            onValueChangeFinished = {
                val targetMs = (sliderPosition * durationMs).toLong()
                onSeekTo(targetMs)
                isDraggingSlider = false
            },
            enabled = durationMs > 0L,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = SpiritualEmeraldBright,
                activeTrackColor = SpiritualEmeraldBright,
                inactiveTrackColor = Color(0x3310B981)
            )
        )

        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(displayPositionMs),
                style = MaterialTheme.typography.labelSmall.copy(color = appColors.textSecondary)
            )
            Text(
                text = formatDuration(durationMs),
                style = MaterialTheme.typography.labelSmall.copy(color = appColors.textSecondary)
            )
        }
    }
}

/**
 * Full Screen / Modal Sheet Player with Complete Controls.
 */
@Composable
fun QuranFullPlayerSheet(
    playerState: QuranPlayerState,
    positionFlow: StateFlow<Long>,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleRepeat: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onClose: () -> Unit
) {
    val appColors = LocalAppColors.current
    val surah = playerState.currentSurah ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "إغلاق",
                    tint = appColors.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(if (playerState.isLocalFile) Color(0x3310B981) else Color(0x2210B981))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (playerState.isLocalFile) "تشغيل محلي بدون إنترنت" else "بث مباشر عبر الإنترنت",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SpiritualEmeraldBright,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            IconButton(onClick = onOpenSleepTimer) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = "مؤقت النوم",
                    tint = if (playerState.isSleepTimerActive) SpiritualEmeraldBright else appColors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large AMOLED Center Disk
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF0F5132), Color(0xFF041E15))))
                .border(2.dp, SpiritualEmeraldBright, CircleShape)
                .shadow(20.dp, CircleShape, spotColor = SpiritualEmeraldBright),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = surah.formattedNumber,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SpiritualEmeraldBright,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "سورة",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = surah.nameArabic,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                )
                Text(
                    text = surah.nameEnglish,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SpiritualEmeraldBright,
                        fontSize = 12.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reciter Info
        Text(
            text = playerState.currentReciter.nameArabic,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )
        )
        Text(
            text = "${playerState.currentReciter.rewayaArabic} • ${surah.ayahCount} آية (${surah.revelationTypeArabic})",
            style = MaterialTheme.typography.bodySmall.copy(
                color = appColors.textSecondary,
                fontSize = 12.sp
            )
        )

        // Active Sleep Timer Banner
        if (playerState.isSleepTimerActive) {
            Spacer(modifier = Modifier.height(8.dp))
            val timerText = if (playerState.sleepAtEndOfSurah) {
                "سيتوقف التشغيل عند نهاية السورة الحالية"
            } else {
                val secLeft = playerState.sleepTimerSecondsRemaining ?: 0L
                val m = secLeft / 60
                val s = secLeft % 60
                String.format(Locale.getDefault(), "مؤقت النوم نشط: %02d:%02d متبقية", m, s)
            }
            Text(
                text = timerText,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SpiritualEmeraldBright,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Isolated Seek Bar & Time labels
        FullPlayerScrubber(
            positionFlow = positionFlow,
            durationMs = playerState.durationMs,
            onSeekTo = onSeekTo
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Playback Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Repeat button
            IconButton(onClick = onToggleRepeat) {
                Icon(
                    imageVector = when (playerState.repeatMode) {
                        RepeatMode.OFF -> Icons.Default.Repeat
                        RepeatMode.ALL -> Icons.Default.Repeat
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                    },
                    contentDescription = playerState.repeatMode.labelArabic,
                    tint = if (playerState.repeatMode != RepeatMode.OFF) SpiritualEmeraldBright else appColors.textSecondary
                )
            }

            // Rewind 10s
            IconButton(
                onClick = {
                    val currentPos = positionFlow.value
                    val newPos = (currentPos - 10000L).coerceAtLeast(0L)
                    onSeekTo(newPos)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Replay10,
                    contentDescription = "ترجيع 10 ثوان",
                    tint = appColors.textPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Previous surah
            IconButton(onClick = onSkipPrevious) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "السورة السابقة",
                    tint = appColors.textPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Central Play / Pause Button
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(SpiritualEmeraldBright, SpiritualEmerald)
                        )
                    )
                    .clickable { onTogglePlay() },
                contentAlignment = Alignment.Center
            ) {
                if (playerState.isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = Color.Black,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "إيقاف مؤقت" else "تشغيل",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Next surah
            IconButton(onClick = onSkipNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "السورة التالية",
                    tint = appColors.textPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Forward 10s
            IconButton(
                onClick = {
                    val currentPos = positionFlow.value
                    val newPos = (currentPos + 10000L).coerceAtMost(playerState.durationMs)
                    onSeekTo(newPos)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Forward10,
                    contentDescription = "تقديم 10 ثوان",
                    tint = appColors.textPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * Reciter Picker Dialog with all 12 mp3quran reciters.
 */
@Composable
fun ReciterPickerDialog(
    currentReciter: Reciter,
    onSelect: (Reciter) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    var selectedRiwayah by remember { mutableStateOf("الكل") }
    val riwayatList = remember { ReciterCatalog.getAllRewayat() }
    val filteredReciters = remember(selectedRiwayah) {
        ReciterCatalog.getRecitersByRewaya(selectedRiwayah)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "اختر القارئ والقراءة الشريفة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "حفص • ورش • قالون • الدوري (${ReciterCatalog.RECITERS.size} قارئ)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SpiritualEmeraldBright,
                        fontSize = 11.sp
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Riwayah Filter Chips
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(riwayatList) { riwayah ->
                        val isFilterActive = selectedRiwayah == riwayah
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(if (isFilterActive) SpiritualEmeraldBright.copy(alpha = 0.25f) else appColors.inset)
                                .border(
                                    1.dp,
                                    if (isFilterActive) SpiritualEmeraldBright else Color.Transparent,
                                    PillShape
                                )
                                .clickable { selectedRiwayah = riwayah }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = riwayah,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isFilterActive) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isFilterActive) SpiritualEmeraldBright else appColors.textSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Reciters List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredReciters, key = { it.id }) { reciter ->
                        val isSelected = reciter.id == currentReciter.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SquircleShapeSmall)
                                .background(if (isSelected) Color(0x2E10B981) else appColors.inset)
                                .clickable { onSelect(reciter) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = reciter.nameArabic,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) SpiritualEmeraldBright else appColors.textPrimary
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(PillShape)
                                            .background(Color(0x1F10B981))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = reciter.rewayaArabic,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = SpiritualEmeraldBright,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                    if (reciter.id == 1) {
                                        Text(
                                            text = "(الافتراضي)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = SpiritualEmeraldBright,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }
                                Text(
                                    text = reciter.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = appColors.textSecondary,
                                        fontSize = 10.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "محدد",
                                    tint = SpiritualEmeraldBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = SpiritualEmeraldBright)
            }
        }
    )
}

/**
 * Sleep Timer Configuration Dialog.
 */
@Composable
fun SleepTimerDialog(
    currentState: QuranPlayerState,
    onSelectDuration: (SleepTimerDuration?) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "مؤقت النوم الذكي",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "حدد وقتاً لإيقاف تلاوة القرآن الكريم تلقائياً عند النوم:",
                    style = MaterialTheme.typography.bodySmall.copy(color = appColors.textSecondary)
                )

                Spacer(modifier = Modifier.height(4.dp))

                SleepTimerDuration.entries.forEach { duration ->
                    val isSelected = when (duration) {
                        SleepTimerDuration.END_OF_SURAH -> currentState.sleepAtEndOfSurah
                        else -> currentState.sleepTimerMinutesLeft == duration.minutes && !currentState.sleepAtEndOfSurah
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleShapeSmall)
                            .background(if (isSelected) Color(0x2E10B981) else appColors.inset)
                            .clickable { onSelectDuration(duration) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = duration.labelArabic,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SpiritualEmeraldBright else appColors.textPrimary
                            )
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SpiritualEmeraldBright,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (currentState.isSleepTimerActive) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { onSelectDuration(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "إيقاف مؤقت النوم",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = appColors.textSecondary)
            }
        }
    )
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSeconds = ms / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
