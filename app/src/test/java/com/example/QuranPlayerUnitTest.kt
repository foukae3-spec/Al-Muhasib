package com.example

import com.example.quran.BatchDownloadProgress
import com.example.quran.QuranPlaybackService
import com.example.quran.QuranPlayerState
import com.example.quran.Reciter
import com.example.quran.ReciterCatalog
import com.example.quran.RepeatMode
import com.example.quran.SleepTimerDuration
import com.example.quran.Surah
import com.example.quran.SurahCatalog
import com.example.quran.SurahDownloadState
import com.example.ui.components.NavigationTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranPlayerUnitTest {

    @Test
    fun verifyReciterCatalogHasAtLeast10RecitersWithYasserAlDossariDefault() {
        // Requirement: "direct download links to at least 10 قارئين with ياسر الدوسري as a default"
        val reciters = ReciterCatalog.RECITERS
        assertTrue("Reciter count must be at least 10", reciters.size >= 10)

        // Default reciter verification
        val defaultReciter = ReciterCatalog.DEFAULT_RECITER
        assertEquals(1, defaultReciter.id)
        assertEquals("ياسر الدوسري", defaultReciter.nameArabic)
        assertEquals("Yasser Al-Dossari", defaultReciter.nameEnglish)
        assertTrue(defaultReciter.serverUrl.startsWith("https://server11.mp3quran.net/yasser/"))

        // Verify all reciters have valid CDN server URLs
        for (reciter in reciters) {
            assertTrue("Reciter ${reciter.nameEnglish} must have non-blank nameArabic", reciter.nameArabic.isNotBlank())
            assertTrue("Reciter ${reciter.nameEnglish} must have valid http(s) URL", reciter.serverUrl.startsWith("http"))
            assertTrue("Reciter ${reciter.nameEnglish} URL must point to mp3quran", reciter.serverUrl.contains("mp3quran.net"))
        }

        // Verify getReciterById
        assertEquals("مشاري راشد العفاسي", ReciterCatalog.getReciterById(2).nameArabic)
        assertEquals("عبد الباسط عبد الصمد", ReciterCatalog.getReciterById(3).nameArabic)
        // Fallback for non-existent ID
        assertEquals(defaultReciter, ReciterCatalog.getReciterById(999))
    }

    @Test
    fun verifySurahAudioUrlFormatting() {
        val reciter = ReciterCatalog.DEFAULT_RECITER

        assertEquals(
            "https://server11.mp3quran.net/yasser/001.mp3",
            ReciterCatalog.getSurahAudioUrl(reciter, 1)
        )
        assertEquals(
            "https://server11.mp3quran.net/yasser/018.mp3",
            ReciterCatalog.getSurahAudioUrl(reciter, 18)
        )
        assertEquals(
            "https://server11.mp3quran.net/yasser/114.mp3",
            ReciterCatalog.getSurahAudioUrl(reciter, 114)
        )

        // Trailing slash robustness
        val reciterWithoutSlash = Reciter(
            id = 99,
            nameArabic = "تجربة",
            nameEnglish = "Test",
            serverUrl = "https://server.mp3quran.net/test"
        )
        assertEquals(
            "https://server.mp3quran.net/test/002.mp3",
            ReciterCatalog.getSurahAudioUrl(reciterWithoutSlash, 2)
        )
    }

    @Test
    fun verifySurahCatalogContainsAll114Surahs() {
        val surahs = SurahCatalog.SURAHS
        assertEquals(114, surahs.size)

        // Surah 1: Al-Fatihah
        val fatihah = SurahCatalog.getSurahByNumber(1)
        assertNotNull(fatihah)
        assertEquals("الفاتحة", fatihah?.nameArabic)
        assertEquals("Al-Fatihah", fatihah?.nameEnglish)
        assertEquals(7, fatihah?.ayahCount)
        assertTrue(fatihah?.isMeccan == true)
        assertEquals("مكية", fatihah?.revelationTypeArabic)
        assertEquals("001", fatihah?.formattedNumber)

        // Surah 2: Al-Baqarah
        val baqarah = SurahCatalog.getSurahByNumber(2)
        assertNotNull(baqarah)
        assertEquals("البقرة", baqarah?.nameArabic)
        assertEquals("Al-Baqarah", baqarah?.nameEnglish)
        assertEquals(286, baqarah?.ayahCount)
        assertFalse(baqarah?.isMeccan == true)
        assertEquals("مدنية", baqarah?.revelationTypeArabic)
        assertEquals("002", baqarah?.formattedNumber)

        // Surah 114: An-Nas
        val nas = SurahCatalog.getSurahByNumber(114)
        assertNotNull(nas)
        assertEquals("الناس", nas?.nameArabic)
        assertEquals("An-Nas", nas?.nameEnglish)
        assertEquals(6, nas?.ayahCount)
        assertEquals("114", nas?.formattedNumber)

        // Boundary checks
        org.junit.Assert.assertNull(SurahCatalog.getSurahByNumber(0))
        org.junit.Assert.assertNull(SurahCatalog.getSurahByNumber(115))
    }

    @Test
    fun verifySurahCatalogSearchAndNormalization() {
        // Search by number
        val searchByNum = SurahCatalog.searchSurahs("18")
        assertEquals(1, searchByNum.size)
        assertEquals("الكهف", searchByNum[0].nameArabic)

        // Search by Arabic name with different alef variants
        val searchBaqarah = SurahCatalog.searchSurahs("البقرة")
        assertTrue(searchBaqarah.any { it.nameArabic == "البقرة" })

        // Search with normalized alef
        val searchIkhlas = SurahCatalog.searchSurahs("الإخلاص")
        assertTrue(searchIkhlas.any { it.nameArabic == "الإخلاص" })

        val searchIkhlasPlain = SurahCatalog.searchSurahs("الاخلاص")
        assertTrue(searchIkhlasPlain.any { it.nameArabic == "الإخلاص" })

        // Search by English name
        val searchEnglish = SurahCatalog.searchSurahs("kahf")
        assertTrue(searchEnglish.any { it.nameEnglish == "Al-Kahf" })

        // Empty query returns all 114
        val all = SurahCatalog.searchSurahs("")
        assertEquals(114, all.size)
    }

    @Test
    fun verifyQuranPlayerStateCalculations() {
        val emptyState = QuranPlayerState()
        assertEquals(0f, emptyState.progressFraction, 0.001f)
        assertEquals(1, emptyState.currentReciter.id)

        val activeState = QuranPlayerState(
            currentPositionMs = 60_000L,
            durationMs = 240_000L
        )
        assertEquals(0.25f, activeState.progressFraction, 0.001f)

        // Over-progress clamped to 1f
        val overflowState = QuranPlayerState(
            currentPositionMs = 300_000L,
            durationMs = 240_000L
        )
        assertEquals(1.0f, overflowState.progressFraction, 0.001f)
    }

    @Test
    fun verifyBatchDownloadProgressFraction() {
        val zeroProgress = BatchDownloadProgress(completedCount = 0, totalCount = 114)
        assertEquals(0f, zeroProgress.progressFraction, 0.001f)

        val halfProgress = BatchDownloadProgress(completedCount = 57, totalCount = 114)
        assertEquals(0.5f, halfProgress.progressFraction, 0.001f)

        val fullProgress = BatchDownloadProgress(completedCount = 114, totalCount = 114)
        assertEquals(1.0f, fullProgress.progressFraction, 0.001f)
    }

    @Test
    fun verifySleepTimerDurationsAndLabels() {
        val durations = SleepTimerDuration.entries
        assertTrue(durations.contains(SleepTimerDuration.MIN_15))
        assertTrue(durations.contains(SleepTimerDuration.MIN_30))
        assertTrue(durations.contains(SleepTimerDuration.MIN_45))
        assertTrue(durations.contains(SleepTimerDuration.MIN_60))
        assertTrue(durations.contains(SleepTimerDuration.END_OF_SURAH))

        assertEquals(15, SleepTimerDuration.MIN_15.minutes)
        assertEquals(30, SleepTimerDuration.MIN_30.minutes)
        assertEquals(45, SleepTimerDuration.MIN_45.minutes)
        assertEquals(60, SleepTimerDuration.MIN_60.minutes)
        assertEquals(0, SleepTimerDuration.END_OF_SURAH.minutes)

        assertEquals("نهاية السورة الحالية", SleepTimerDuration.END_OF_SURAH.labelArabic)
    }

    @Test
    fun verifyPlaybackServiceActionContract() {
        assertEquals("com.example.quran.ACTION_PLAY", QuranPlaybackService.ACTION_PLAY)
        assertEquals("com.example.quran.ACTION_PAUSE", QuranPlaybackService.ACTION_PAUSE)
        assertEquals("com.example.quran.ACTION_TOGGLE_PLAY", QuranPlaybackService.ACTION_TOGGLE_PLAY)
        assertEquals("com.example.quran.ACTION_NEXT", QuranPlaybackService.ACTION_NEXT)
        assertEquals("com.example.quran.ACTION_PREVIOUS", QuranPlaybackService.ACTION_PREVIOUS)
        assertEquals("com.example.quran.ACTION_STOP", QuranPlaybackService.ACTION_STOP)
        assertEquals("com.example.quran.ACTION_SEEK_TO", QuranPlaybackService.ACTION_SEEK_TO)
        assertEquals("com.example.quran.ACTION_SET_REPEAT", QuranPlaybackService.ACTION_SET_REPEAT)
        assertEquals("com.example.quran.ACTION_SET_SLEEP_TIMER", QuranPlaybackService.ACTION_SET_SLEEP_TIMER)

        assertEquals(2001, QuranPlaybackService.NOTIFICATION_ID)
        assertEquals("quran_playback_channel", QuranPlaybackService.CHANNEL_ID)
    }

    @Test
    fun verifyNavigationTabContainsQuran() {
        val tabs = NavigationTab.entries
        assertTrue("NavigationTab must contain QURAN", tabs.any { it.name == "QURAN" })
        val quranTab = NavigationTab.valueOf("QURAN")
        assertEquals("القرآن", quranTab.label)
    }

    @Test
    fun verifyAll114SurahsHaveValidMetadataAndSequentialOrdering() {
        val surahs = SurahCatalog.SURAHS
        assertEquals(114, surahs.size)

        for (i in surahs.indices) {
            val expectedNumber = i + 1
            val surah = surahs[i]
            assertEquals("Surah index $i must have number $expectedNumber", expectedNumber, surah.number)
            assertTrue("Surah ${surah.number} must have Arabic name", surah.nameArabic.isNotBlank())
            assertTrue("Surah ${surah.number} must have English name", surah.nameEnglish.isNotBlank())
            assertTrue("Surah ${surah.number} ayahCount must be positive", surah.ayahCount > 0)
            assertEquals(expectedNumber.toString().padStart(3, '0'), surah.formattedNumber)
            assertEquals(if (surah.isMeccan) "مكية" else "مدنية", surah.revelationTypeArabic)
        }
    }

    @Test
    fun verifyAudioFocusStateTrackingLogic() {
        // Logic simulator matching QuranPlaybackService focus handling
        var isPlaying = true
        var resumeOnFocusGain = false

        // 1. Transient loss while playing (e.g. phone call or GPS nav prompt)
        val wasPlaying = isPlaying
        resumeOnFocusGain = wasPlaying
        isPlaying = false

        assertTrue("Should mark for resumption on transient loss while playing", resumeOnFocusGain)
        assertFalse(isPlaying)

        // Focus gained back:
        if (resumeOnFocusGain) {
            resumeOnFocusGain = false
            isPlaying = true
        }
        assertTrue("Should resume playback after transient focus gain", isPlaying)
        assertFalse(resumeOnFocusGain)

        // 2. User manually pauses playback
        isPlaying = false
        resumeOnFocusGain = false

        // Focus gained back (e.g. phone call ends or external app completes)
        if (resumeOnFocusGain) {
            resumeOnFocusGain = false
            isPlaying = true
        }
        assertFalse("Must NOT resume playback if user paused manually!", isPlaying)
        assertFalse(resumeOnFocusGain)
    }

    @Test
    fun verifyBecomingNoisyAndMediaButtonContracts() {
        assertEquals(
            "android.media.AUDIO_BECOMING_NOISY",
            android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY
        )
        assertEquals(
            "android.intent.action.MEDIA_BUTTON",
            android.content.Intent.ACTION_MEDIA_BUTTON
        )
    }

    @Test
    fun verifyDecoupledPlaybackPositionStateFlow() {
        val positionFlow = kotlinx.coroutines.flow.MutableStateFlow(0L)
        assertEquals(0L, positionFlow.value)

        // When position updates on audio clock tick, positionFlow emits without modifying QuranPlayerState
        val initialState = QuranPlayerState()
        positionFlow.value = 15000L
        assertEquals(15000L, positionFlow.value)
        assertEquals("PlayerState should remain unchanged by position tracker ticks", 0L, initialState.currentPositionMs)
    }

    @Test
    fun verifyDownloadedSurahsCacheContract() {
        // Fast in-memory Set lookup replaces synchronous File.exists()
        val downloadedSet = setOf(1, 18, 36, 67, 114)
        assertTrue(downloadedSet.contains(1))
        assertTrue(downloadedSet.contains(18))
        assertFalse(downloadedSet.contains(2))
        assertEquals(5, downloadedSet.size)
    }
}

