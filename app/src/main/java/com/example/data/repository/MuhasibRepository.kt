package com.example.data.repository

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.model.BookChunk
import com.example.data.model.DhikrItem
import com.example.data.model.IslamicBook
import com.example.data.model.MuhasabahScore
import com.example.data.model.PrayerLog
import com.example.data.model.RecoveryTrack
import com.example.data.model.SobrietyLog
import com.example.data.model.DailyLifeMission
import com.example.data.model.AppNiyyahSession
import com.example.data.model.AppGuardRule
import com.example.data.seed.AdhkarSeedData
import com.example.data.seed.IslamicBooksSeedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MuhasibRepository(private val database: AppDatabase) {

    private val dhikrDao = database.dhikrDao()
    private val prayerDao = database.prayerDao()
    private val sobrietyDao = database.sobrietyDao()
    private val muhasabahDao = database.muhasabahDao()
    private val islamicBookDao = database.islamicBookDao()
    private val bookChunkDao = database.bookChunkDao()
    private val recoveryTrackDao = database.recoveryTrackDao()
    private val lifeOsDao = database.lifeOsDao()

    // Islamic Books Library
    val allBooks: Flow<List<IslamicBook>> = islamicBookDao.getAllBooks()

    fun getBooksByCategory(category: String): Flow<List<IslamicBook>> =
        islamicBookDao.getBooksByCategory(category)

    fun getBookById(id: String): Flow<IslamicBook?> =
        islamicBookDao.getBookById(id)

    suspend fun getBookByIdDirect(id: String): IslamicBook? = withContext(Dispatchers.IO) {
        islamicBookDao.getBookByIdDirect(id)
    }

    fun getChunksForBook(bookId: String): Flow<List<BookChunk>> =
        bookChunkDao.getChunksForBook(bookId)

    fun getChunkByIndex(bookId: String, chunkIndex: Int): Flow<BookChunk?> =
        bookChunkDao.getChunkByIndex(bookId, chunkIndex)

    suspend fun getChunkByIndexDirect(bookId: String, chunkIndex: Int): BookChunk? = withContext(Dispatchers.IO) {
        bookChunkDao.getChunkByIndexDirect(bookId, chunkIndex)
    }

    suspend fun getNextUnreadChunk(bookId: String): BookChunk? = withContext(Dispatchers.IO) {
        bookChunkDao.getNextUnreadChunk(bookId)
    }

    suspend fun updateBookCursor(bookId: String, cursor: Int) = withContext(Dispatchers.IO) {
        islamicBookDao.updateCursor(bookId, cursor, System.currentTimeMillis())
    }

    suspend fun markChunkRead(chunkId: String, isRead: Boolean) = withContext(Dispatchers.IO) {
        bookChunkDao.markChunkRead(chunkId, isRead, System.currentTimeMillis())
    }

    suspend fun markBookDownloaded(bookId: String, downloaded: Boolean) = withContext(Dispatchers.IO) {
        islamicBookDao.markDownloaded(bookId, downloaded)
    }

    suspend fun getRandomBookQuote(): BookChunk? = withContext(Dispatchers.IO) {
        bookChunkDao.getRandomQuote()
    }

    suspend fun insertBookChunks(chunks: List<BookChunk>) = withContext(Dispatchers.IO) {
        bookChunkDao.insertAll(chunks)
    }

    suspend fun insertBook(book: IslamicBook) = withContext(Dispatchers.IO) {
        islamicBookDao.insertOrUpdate(book)
    }

    /**
     * توحيد مؤشر العلامة المرجعية (currentChunkCursor):
     * تحديث العلامة المرجعية في قاعدة البيانات، وإتمام الفقرة، ومزامنة QuotePreferences وودجات الهاتف وشاشة AOD.
     */
    suspend fun advanceBookChunkCursor(bookId: String): BookChunk? = withContext(Dispatchers.IO) {
        val canonicalId = com.example.library.OpenSourceBookCatalog.resolveCanonicalId(bookId)
        var book = islamicBookDao.getBookByIdDirect(canonicalId)
        if (book == null && canonicalId != bookId) {
            book = islamicBookDao.getBookByIdDirect(bookId)
        }
        if (book == null) {
            book = com.example.data.seed.IslamicBooksSeedData.getInitialBooks().find { it.id == canonicalId || it.id == bookId }
        }

        val currentCursor = book?.currentChunkCursor ?: 0
        val total = (book?.totalChunks ?: 50).coerceAtLeast(1)
        val nextCursor = (currentCursor + 1).coerceAtMost(total)

        // Mark current chunk as read
        val currentChunk = bookChunkDao.getChunkByIndexDirect(book?.id ?: canonicalId, currentCursor)
        if (currentChunk != null) {
            bookChunkDao.markChunkRead(currentChunk.id, true, System.currentTimeMillis())
        }

        // Update database cursor for both IDs if aliased
        if (book != null) {
            islamicBookDao.updateCursor(book.id, nextCursor, System.currentTimeMillis())
            if (canonicalId != book.id) {
                islamicBookDao.updateCursor(canonicalId, nextCursor, System.currentTimeMillis())
            }
        }

        // Retrieve new chunk at nextCursor
        val newChunk = bookChunkDao.getChunkByIndexDirect(book?.id ?: canonicalId, nextCursor)
            ?: bookChunkDao.getNextUnreadChunk(book?.id ?: canonicalId)

        // Sync with QuotePreferences and widgets
        try {
            val context = com.example.MuhasibApplication.instance
            if (newChunk != null) {
                com.example.util.QuotePreferences.setPinnedQuote(
                    context = context,
                    title = newChunk.title,
                    content = newChunk.content,
                    bookTitle = book?.title ?: "وِرد العلم",
                    bookId = book?.id ?: canonicalId,
                    author = book?.author ?: "الإمام ابن قيم الجوزية",
                    pageNumber = newChunk.pageNumber,
                    dailyChunkIndex = (nextCursor % 5) + 1,
                    dailyTargetChunks = 5
                )
            }
            com.example.widget.WidgetUpdateHelper.updateAllWidgets(context)
        } catch (_: Exception) {}

        newChunk
    }

    // 4-Front Recovery Engine
    val allRecoveryTracks: Flow<List<RecoveryTrack>> = recoveryTrackDao.getAllTracks()

    fun getRecoveryTrack(trackType: String): Flow<RecoveryTrack?> =
        recoveryTrackDao.getTrack(trackType)

    suspend fun getRecoveryTrackDirect(trackType: String): RecoveryTrack? = withContext(Dispatchers.IO) {
        recoveryTrackDao.getTrackDirect(trackType)
    }

    suspend fun updateRecoveryStreak(trackType: String, streakSeconds: Long, highestStreak: Long) = withContext(Dispatchers.IO) {
        recoveryTrackDao.updateStreak(trackType, streakSeconds, highestStreak, System.currentTimeMillis())
    }

    suspend fun recordTrackRelapse(trackType: String) = withContext(Dispatchers.IO) {
        recoveryTrackDao.recordRelapse(trackType, System.currentTimeMillis())
    }

    suspend fun toggleNightGuard(trackType: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        recoveryTrackDao.toggleNightGuard(trackType, enabled)
    }

    suspend fun toggleSalahLock(trackType: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        recoveryTrackDao.toggleSalahLock(trackType, enabled)
    }

    suspend fun updateMoneyAndCigarettes(trackType: String, moneySaved: Double, cigarettesAvoided: Int) = withContext(Dispatchers.IO) {
        recoveryTrackDao.updateMoneyAndCigarettes(trackType, moneySaved, cigarettesAvoided)
    }

    val allDhikr: Flow<List<DhikrItem>> = dhikrDao.getAllDhikr()

    fun getDhikrByCategory(category: String): Flow<List<DhikrItem>> =
        dhikrDao.getDhikrByCategory(category)

    suspend fun getDhikrByCategoryDirect(category: String): List<DhikrItem> = withContext(Dispatchers.IO) {
        dhikrDao.getDhikrByCategoryDirect(category)
    }

    suspend fun incrementDhikr(id: Int) = withContext(Dispatchers.IO) {
        val item = dhikrDao.getDhikrById(id) ?: return@withContext
        val newCount = item.currentCount + 1
        val isCompleted = newCount >= item.targetCount
        dhikrDao.updateCount(id, newCount, isCompleted)
        val text = item.arabic + " " + item.title + " " + item.category
        when {
            text.contains("أستغفر") || text.contains("استغفار") || text.contains("أَسْتَغْفِرُ") -> {
                sobrietyDao.incrementIstighfar()
            }
            text.contains("سبحان الله") || text.contains("سُبْحَانَ اللَّه") -> {
                sobrietyDao.incrementSubhanAllah()
            }
            text.contains("الحمد لله") || text.contains("الْحَمْدُ لِلَّه") -> {
                sobrietyDao.incrementAlhamdulillah()
            }
            text.contains("الله أكبر") || text.contains("اللَّهُ أَكْبَر") -> {
                sobrietyDao.incrementAllahuAkbar()
            }
            text.contains("لا إله إلا الله") || text.contains("لَا إِلَهَ إِلَّا اللَّه") -> {
                sobrietyDao.incrementLaIlahaIllallah()
            }
            else -> {
                sobrietyDao.incrementTasbi7()
            }
        }
    }

    // ponytail: mark dhikr as completed directly and advance to next
    suspend fun markDhikrDone(id: Int) = withContext(Dispatchers.IO) {
        val item = dhikrDao.getDhikrById(id) ?: return@withContext
        dhikrDao.updateCount(id, item.targetCount, true)
        val text = item.arabic + " " + item.title + " " + item.category
        when {
            text.contains("أستغفر") || text.contains("استغفار") || text.contains("أَسْتَغْفِرُ") -> {
                sobrietyDao.incrementIstighfar()
            }
            text.contains("سبحان الله") || text.contains("سُبْحَانَ اللَّه") -> {
                sobrietyDao.incrementSubhanAllah()
            }
            text.contains("الحمد لله") || text.contains("الْحَمْدُ لِلَّه") -> {
                sobrietyDao.incrementAlhamdulillah()
            }
            text.contains("الله أكبر") || text.contains("اللَّهُ أَكْبَر") -> {
                sobrietyDao.incrementAllahuAkbar()
            }
            text.contains("لا إله إلا الله") || text.contains("لَا إِلَهَ إِلَّا اللَّه") -> {
                sobrietyDao.incrementLaIlahaIllallah()
            }
            else -> {
                sobrietyDao.incrementTasbi7()
            }
        }
    }

    suspend fun resetCategoryDhikr(category: String) = withContext(Dispatchers.IO) {
        dhikrDao.resetCategoryCounts(category)
    }

    suspend fun resetAllDhikr() = withContext(Dispatchers.IO) {
        dhikrDao.resetAllCounts()
    }

    fun getTodayPrayerLog(): Flow<PrayerLog> {
        val today = LocalDate.now().toString()
        return prayerDao.getPrayerLogForDate(today).map { log ->
            log ?: PrayerLog(dateString = today)
        }
    }

    suspend fun togglePrayer(prayerName: String, done: Boolean) = withContext(Dispatchers.IO) {
        val today = LocalDate.now().toString()
        val current = prayerDao.getPrayerLogDirect(today) ?: PrayerLog(dateString = today)
        val updated = when (prayerName.lowercase()) {
            "fajr" -> current.copy(fajrDone = done)
            "dhuhr" -> current.copy(dhuhrDone = done)
            "asr" -> current.copy(asrDone = done)
            "maghrib" -> current.copy(maghribDone = done)
            "isha" -> current.copy(ishaDone = done)
            else -> current
        }
        prayerDao.insertOrUpdate(updated)
    }

    fun getSobrietyState(): Flow<SobrietyLog> =
        sobrietyDao.getSobrietyState().map { it ?: SobrietyLog() }

    suspend fun getSobrietyStateDirect(): SobrietyLog? = withContext(Dispatchers.IO) {
        sobrietyDao.getSobrietyStateDirect()
    }

    suspend fun incrementIstighfarCount() = withContext(Dispatchers.IO) {
        sobrietyDao.incrementIstighfar()
    }

    suspend fun setIstighfarCount(count: Int) = withContext(Dispatchers.IO) {
        sobrietyDao.setIstighfarCount(count)
    }

    // ponytail: tasbih is its own counter, never mixed with istighfar
    suspend fun incrementTasbi7Count() = withContext(Dispatchers.IO) {
        sobrietyDao.incrementTasbi7()
    }

    suspend fun incrementSubhanAllahCount() = withContext(Dispatchers.IO) {
        sobrietyDao.incrementSubhanAllah()
    }

    suspend fun incrementAlhamdulillahCount() = withContext(Dispatchers.IO) {
        sobrietyDao.incrementAlhamdulillah()
    }

    suspend fun incrementAllahuAkbarCount() = withContext(Dispatchers.IO) {
        sobrietyDao.incrementAllahuAkbar()
    }

    suspend fun incrementLaIlahaIllallahCount() = withContext(Dispatchers.IO) {
        sobrietyDao.incrementLaIlahaIllallah()
    }

    suspend fun toggleWorkout(done: Boolean) = withContext(Dispatchers.IO) {
        val current = sobrietyDao.getSobrietyStateDirect() ?: SobrietyLog()
        sobrietyDao.insertOrUpdate(current.copy(workoutCompletedToday = done))
    }

    suspend fun toggleColdShower(done: Boolean) = withContext(Dispatchers.IO) {
        val current = sobrietyDao.getSobrietyStateDirect() ?: SobrietyLog()
        sobrietyDao.insertOrUpdate(current.copy(coldShowerToday = done))
    }

    suspend fun recordRelapse() = withContext(Dispatchers.IO) {
        val current = sobrietyDao.getSobrietyStateDirect() ?: SobrietyLog()
        val updated = current.copy(
            purityStartEpochMs = System.currentTimeMillis(),
            cleanDaysCount = 0,
            relapsesCount = current.relapsesCount + 1,
            workoutCompletedToday = false,
            coldShowerToday = false
        )
        sobrietyDao.insertOrUpdate(updated)
    }

    fun getMuhasabahHistory(): Flow<List<MuhasabahScore>> =
        muhasabahDao.getHistory()

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (dhikrDao.getCount() == 0) {
            dhikrDao.insertAll(AdhkarSeedData.getInitialAdhkar())
        }
        val currentSobriety = sobrietyDao.getSobrietyStateDirect()
        if (currentSobriety == null) {
            sobrietyDao.insertOrUpdate(SobrietyLog())
        }
        val today = LocalDate.now().toString()
        val currentPrayer = prayerDao.getPrayerLogDirect(today)
        if (currentPrayer == null) {
            prayerDao.insertOrUpdate(
                PrayerLog(
                    dateString = today,
                    fajrDone = true,
                    dhuhrDone = true,
                    asrDone = false,
                    maghribDone = false,
                    ishaDone = false
                )
            )
        }
        val currentBook = islamicBookDao.getBookByIdDirect("al_daa_wal_dawaa")
        if (currentBook == null) {
            islamicBookDao.insertAll(IslamicBooksSeedData.getInitialBooks())
            bookChunkDao.insertAll(IslamicBooksSeedData.getInitialChunks())
        }
        val currentTrack = recoveryTrackDao.getTrackDirect("SALAH")
        if (currentTrack == null) {
            recoveryTrackDao.insertAll(IslamicBooksSeedData.getInitialRecoveryTracks())
        }

        // Whole-Phone Life OS Initial Missions Seed
        val todayMissions = lifeOsDao.getMissionsForDate(today)
        if (todayMissions.isEmpty()) {
            lifeOsDao.insertMission(
                DailyLifeMission(
                    dateString = today,
                    title = "Read Chunk #4 of Al-Daa wa Al-Dawaa & Reflect",
                    prayerAnchor = "POST_FAJR",
                    category = "DEEN",
                    estimatedMinutes = 15,
                    isCompleted = false
                )
            )
            lifeOsDao.insertMission(
                DailyLifeMission(
                    dateString = today,
                    title = "90m Deep Craft Block (Zero Feeds / Zero Browser)",
                    prayerAnchor = "DUHA_BLOCK",
                    category = "PROVISION",
                    estimatedMinutes = 90,
                    isCompleted = false
                )
            )
            lifeOsDao.insertMission(
                DailyLifeMission(
                    dateString = today,
                    title = "Physical Stewardship & Family Presence Before Maghrib",
                    prayerAnchor = "ASR_BLOCK",
                    category = "HEALTH",
                    estimatedMinutes = 45,
                    isCompleted = false
                )
            )
        }
    }

    // Whole-Phone Life OS Operations
    fun observeDailyMissions(dateString: String): Flow<List<DailyLifeMission>> =
        lifeOsDao.observeMissionsForDate(dateString)

    suspend fun insertDailyMission(mission: DailyLifeMission): Long = withContext(Dispatchers.IO) {
        lifeOsDao.insertMission(mission)
    }

    suspend fun setMissionCompleted(missionId: Int, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        lifeOsDao.setMissionCompleted(missionId, isCompleted)
    }

    suspend fun deleteDailyMission(missionId: Int) = withContext(Dispatchers.IO) {
        lifeOsDao.deleteMission(missionId)
    }

    fun observeNiyyahSessions(dateString: String): Flow<List<AppNiyyahSession>> =
        lifeOsDao.observeNiyyahSessionsForDate(dateString)

    suspend fun recordNiyyahSession(session: AppNiyyahSession): Long = withContext(Dispatchers.IO) {
        lifeOsDao.insertNiyyahSession(session)
    }

    suspend fun closeNiyyahSession(sessionId: Int, endedAtMs: Long, honored: Boolean) = withContext(Dispatchers.IO) {
        lifeOsDao.closeNiyyahSession(sessionId, endedAtMs, honored)
    }

    fun observeAllAppGuardRules(): Flow<List<AppGuardRule>> =
        lifeOsDao.observeAllAppGuardRules()

    suspend fun upsertAppGuardRule(rule: AppGuardRule) = withContext(Dispatchers.IO) {
        lifeOsDao.upsertAppGuardRule(rule)
    }

    fun isOnboardingCompleted(context: Context): Boolean =
        OnboardingPreferences.isOnboardingCompleted(context)

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        OnboardingPreferences.setOnboardingCompleted(context, completed)
    }
}
