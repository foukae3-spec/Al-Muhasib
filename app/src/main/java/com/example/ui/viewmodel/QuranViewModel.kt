package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quran.BatchDownloadProgress
import com.example.quran.QuranDownloadManager
import com.example.quran.QuranPlaybackService
import com.example.quran.QuranPlayerState
import com.example.quran.Reciter
import com.example.quran.ReciterCatalog
import com.example.quran.RepeatMode
import com.example.quran.SleepTimerDuration
import com.example.quran.Surah
import com.example.quran.SurahCatalog
import com.example.quran.SurahDownloadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val downloadManager = QuranDownloadManager.getInstance(application)

    private val _selectedReciter = MutableStateFlow<Reciter>(ReciterCatalog.DEFAULT_RECITER)
    val selectedReciter: StateFlow<Reciter> = _selectedReciter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val surahs: StateFlow<List<Surah>> = _searchQuery
        .combine(MutableStateFlow(SurahCatalog.SURAHS)) { query, _ ->
            SurahCatalog.searchSurahs(query)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SurahCatalog.SURAHS)

    val playerState: StateFlow<QuranPlayerState> = QuranPlaybackService.playerState
    val playbackPositionMs: StateFlow<Long> = QuranPlaybackService.playbackPositionMs

    val downloadStates: StateFlow<Map<String, SurahDownloadState>> = downloadManager.downloadStates

    val isBatchDownloading: StateFlow<Boolean> = downloadManager.isBatchDownloading

    val batchProgress: StateFlow<BatchDownloadProgress?> = downloadManager.batchProgress

    val downloadedSurahIds: StateFlow<Set<Int>> = _selectedReciter
        .combine(downloadManager.downloadedSurahsCache) { reciter, cache ->
            cache[reciter.id] ?: downloadManager.getDownloadedSurahNumbers(reciter.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val downloadedCount: StateFlow<Int> = downloadedSurahIds
        .combine(downloadManager.downloadStates) { ids, _ ->
            ids.size
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            downloadManager.getDownloadedSurahsCount(ReciterCatalog.DEFAULT_RECITER.id)
        )

    private val _isFullscreenReaderActive = MutableStateFlow(false)
    val isFullscreenReaderActive: StateFlow<Boolean> = _isFullscreenReaderActive.asStateFlow()

    fun setIsFullscreenReaderActive(active: Boolean) {
        _isFullscreenReaderActive.value = active
    }

    fun selectReciter(reciter: Reciter) {
        _selectedReciter.value = reciter
        // If a track is already playing, user can choose to continue or switch
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun playSurah(surah: Surah) {
        QuranPlaybackService.playSurah(getApplication(), _selectedReciter.value, surah)
    }

    fun togglePlayPause() {
        QuranPlaybackService.togglePlayPause(getApplication())
    }

    fun skipNext() {
        QuranPlaybackService.skipNext(getApplication())
    }

    fun skipPrevious() {
        QuranPlaybackService.skipPrevious(getApplication())
    }

    fun seekTo(positionMs: Long) {
        QuranPlaybackService.seekTo(getApplication(), positionMs)
    }

    fun setRepeatMode(mode: RepeatMode) {
        QuranPlaybackService.setRepeatMode(getApplication(), mode)
    }

    fun setSleepTimer(duration: SleepTimerDuration?) {
        QuranPlaybackService.setSleepTimer(getApplication(), duration)
    }

    fun stop() {
        QuranPlaybackService.stop(getApplication())
    }

    fun downloadSurah(surah: Surah) {
        downloadManager.downloadSurah(_selectedReciter.value, surah)
    }

    fun deleteSurah(surah: Surah) {
        downloadManager.deleteSurah(_selectedReciter.value.id, surah.number)
    }

    fun startFullQuranDownload() {
        downloadManager.downloadFullQuran(_selectedReciter.value)
    }

    fun cancelBatchDownload() {
        downloadManager.cancelBatchDownload()
    }

    fun isSurahDownloaded(surahNumber: Int): Boolean {
        return downloadManager.isSurahDownloaded(_selectedReciter.value.id, surahNumber)
    }

    fun getDownloadedSurahsCount(): Int {
        return downloadManager.getDownloadedSurahsCount(_selectedReciter.value.id)
    }
}
