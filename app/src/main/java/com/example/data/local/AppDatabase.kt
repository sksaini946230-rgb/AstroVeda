package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

@Database(
    entities = [KundaliEntity::class, PanchangCacheEntity::class, HoroscopeCacheEntity::class, SavedReportEntity::class, RecentSearchEntity::class],
    version = 5,
    // Schemas are recorded under app/schemas from here on. Without them a
    // migration cannot be written, let alone tested, which is how this database
    // ended up on a destructive fallback in the first place.
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kundaliDao(): KundaliDao
    abstract fun panchangCacheDao(): PanchangCacheDao
    abstract fun horoscopeCacheDao(): HoroscopeCacheDao
    abstract fun savedReportDao(): SavedReportDao
    abstract fun recentSearchDao(): RecentSearchDao

    companion object {
        /**
         * Every schema change from version 5 onward adds its migration here.
         * Empty is correct today: 5 is the shipped version.
         */
        private val MIGRATIONS: Array<Migration> = emptyArray()

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "astroveda_database"
                )
                    // This used to be fallbackToDestructiveMigration(dropAllTables
                    // = true): every schema change silently dropped the user's
                    // saved profiles, their reports and their recent searches.
                    // Nothing warned anyone, because a wipe is not an error.
                    //
                    // Upgrades now require a real Migration in MIGRATIONS below.
                    // A missing one fails loudly in development rather than
                    // quietly destroying data in production. Downgrades — only
                    // reachable by sideloading an older build over a newer one —
                    // still fall back, since there is no forward schema to
                    // migrate from.
                    .addMigrations(*MIGRATIONS)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
