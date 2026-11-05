package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.DhikrDao
import com.example.data.dao.MuhasabahDao
import com.example.data.dao.PrayerDao
import com.example.data.dao.SobrietyDao
import com.example.data.model.DhikrItem
import com.example.data.model.MuhasabahScore
import com.example.data.model.PrayerLog
import com.example.data.model.SobrietyLog
import com.example.data.seed.AdhkarSeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DhikrItem::class,
        PrayerLog::class,
        SobrietyLog::class,
        MuhasabahScore::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dhikrDao(): DhikrDao
    abstract fun prayerDao(): PrayerDao
    abstract fun sobrietyDao(): SobrietyDao
    abstract fun muhasabahDao(): MuhasabahDao

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

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "al_muhasib_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigrationOnDowngrade()
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

        private suspend fun populateInitialData(database: AppDatabase) {
            // Seed complete Adhkar from Hisn al-Muslim
            database.dhikrDao().insertAll(AdhkarSeedData.getInitialAdhkar())

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
