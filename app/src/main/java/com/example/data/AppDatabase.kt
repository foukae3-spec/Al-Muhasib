package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BookChunkDao
import com.example.data.dao.DhikrDao
import com.example.data.dao.IslamicBookDao
import com.example.data.dao.MuhasabahDao
import com.example.data.dao.PrayerDao
import com.example.data.dao.RecoveryTrackDao
import com.example.data.dao.SobrietyDao
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
import com.example.data.model.LifeOsDao
import com.example.data.seed.AdhkarSeedData
import com.example.data.seed.IslamicBooksSeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DhikrItem::class,
        PrayerLog::class,
        SobrietyLog::class,
        MuhasabahScore::class,
        IslamicBook::class,
        BookChunk::class,
        RecoveryTrack::class,
        DailyLifeMission::class,
        AppNiyyahSession::class,
        AppGuardRule::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dhikrDao(): DhikrDao
    abstract fun prayerDao(): PrayerDao
    abstract fun sobrietyDao(): SobrietyDao
    abstract fun muhasabahDao(): MuhasabahDao
    abstract fun islamicBookDao(): IslamicBookDao
    abstract fun bookChunkDao(): BookChunkDao
    abstract fun recoveryTrackDao(): RecoveryTrackDao
    abstract fun lifeOsDao(): LifeOsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration 1→2: adds separate tasbih counters to sobriety_state
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sobriety_state ADD COLUMN tasbi7TodayCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sobriety_state ADD COLUMN tasbi7DailyTarget INTEGER NOT NULL DEFAULT 200")
            }
        }

        // Migration 2→3: adds fully independent counters for each dhikr
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sobriety_state ADD COLUMN subhanAllahTodayCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sobriety_state ADD COLUMN alhamdulillahTodayCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sobriety_state ADD COLUMN allahuAkbarTodayCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sobriety_state ADD COLUMN laIlahaIllallahTodayCount INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration 3→4: adds Islamic Books library, Chunks, and 4-Front Recovery tracks
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS islamic_books (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        author TEXT NOT NULL,
                        category TEXT NOT NULL,
                        description TEXT NOT NULL,
                        totalChunks INTEGER NOT NULL,
                        currentChunkCursor INTEGER NOT NULL DEFAULT 0,
                        isDownloaded INTEGER NOT NULL DEFAULT 0,
                        lastReadTimestamp INTEGER NOT NULL DEFAULT 0,
                        coverAccentHex TEXT NOT NULL DEFAULT '#10B981'
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS book_chunks (
                        id TEXT NOT NULL PRIMARY KEY,
                        bookId TEXT NOT NULL,
                        chunkIndex INTEGER NOT NULL,
                        pageNumber INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        reflectionQuestion TEXT,
                        isRead INTEGER NOT NULL DEFAULT 0,
                        readTimestamp INTEGER
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS recovery_tracks (
                        trackType TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        startDateTimestamp INTEGER NOT NULL,
                        cleanStreakSeconds INTEGER NOT NULL DEFAULT 0,
                        relapseCount INTEGER NOT NULL DEFAULT 0,
                        moneySavedAmount REAL NOT NULL DEFAULT 0.0,
                        cigarettesAvoided INTEGER NOT NULL DEFAULT 0,
                        highestStreakSeconds INTEGER NOT NULL DEFAULT 0,
                        nightGuardEnabled INTEGER NOT NULL DEFAULT 0,
                        salahLockEnabled INTEGER NOT NULL DEFAULT 0,
                        lastCheckInTimestamp INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        // Migration 4→5: adds Whole-Phone Life OS Daily Missions, Niyyah Sessions, and Guard Rules
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_life_missions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dateString TEXT NOT NULL,
                        title TEXT NOT NULL,
                        prayerAnchor TEXT NOT NULL,
                        category TEXT NOT NULL,
                        estimatedMinutes INTEGER NOT NULL DEFAULT 25,
                        completedMinutes INTEGER NOT NULL DEFAULT 0,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        barakahPoints INTEGER NOT NULL DEFAULT 15,
                        createdAtEpochMs INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS app_niyyah_sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dateString TEXT NOT NULL,
                        packageName TEXT NOT NULL,
                        appLabel TEXT NOT NULL,
                        statedNiyyah TEXT NOT NULL,
                        allottedMinutes INTEGER NOT NULL,
                        startedAtEpochMs INTEGER NOT NULL,
                        endedAtEpochMs INTEGER,
                        honoredTimeBox INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS app_guard_rules (
                        packageName TEXT NOT NULL PRIMARY KEY,
                        appLabel TEXT NOT NULL,
                        sanctuaryTierName TEXT NOT NULL,
                        dailyLimitMinutes INTEGER NOT NULL DEFAULT 20,
                        requireNiyyahPrompt INTEGER NOT NULL DEFAULT 1,
                        blockDuringSalahWindow INTEGER NOT NULL DEFAULT 1,
                        blockDuringNightGuard INTEGER NOT NULL DEFAULT 1,
                        isPinnedToHomeDock INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "al_muhasib_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    seedBooksAndRecoveryIfNeeded(database)
                }
            }
        }

        private suspend fun seedBooksAndRecoveryIfNeeded(database: AppDatabase) {
            val books = database.islamicBookDao().getBookByIdDirect("al_daa_wal_dawaa")
            if (books == null) {
                database.islamicBookDao().insertAll(IslamicBooksSeedData.getInitialBooks())
                database.bookChunkDao().insertAll(IslamicBooksSeedData.getInitialChunks())
            }
            val recoverySalah = database.recoveryTrackDao().getTrackDirect("SALAH")
            if (recoverySalah == null) {
                database.recoveryTrackDao().insertAll(IslamicBooksSeedData.getInitialRecoveryTracks())
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            // Seed complete Adhkar from Hisn al-Muslim
            database.dhikrDao().insertAll(AdhkarSeedData.getInitialAdhkar())

            // Seed Islamic books & reading chunks
            database.islamicBookDao().insertAll(IslamicBooksSeedData.getInitialBooks())
            database.bookChunkDao().insertAll(IslamicBooksSeedData.getInitialChunks())

            // Seed 4-Front recovery tracks
            database.recoveryTrackDao().insertAll(IslamicBooksSeedData.getInitialRecoveryTracks())

            // Seed default Sobriety & Purity baseline
            database.sobrietyDao().insertOrUpdate(
                SobrietyLog(
                    id = 1,
                    purityStartEpochMs = System.currentTimeMillis() - (12L * 24 * 60 * 60 * 1000),
                    cleanDaysCount = 12,
                    workoutCompletedToday = false,
                    coldShowerToday = false,
                    relapsesCount = 0,
                    istighfarTodayCount = 145,
                    istighfarDailyTarget = 500
                )
            )

            // Seed today's prayer log
            val todayStr = java.time.LocalDate.now().toString()
            database.prayerDao().insertOrUpdate(
                PrayerLog(
                    dateString = todayStr,
                    fajrDone = true,
                    dhuhrDone = true,
                    asrDone = false,
                    maghribDone = false,
                    ishaDone = false
                )
            )

            // Seed recent muhasabah scores
            database.muhasabahDao().insert(
                MuhasabahScore(
                    dateString = java.time.LocalDate.now().minusDays(1).toString(),
                    scorePercentage = 84,
                    salahScore = 100,
                    dhikrScore = 75,
                    purityScore = 90
                )
            )
        }
    }
}
