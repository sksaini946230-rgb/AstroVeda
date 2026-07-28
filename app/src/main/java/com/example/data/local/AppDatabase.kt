package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [KundaliEntity::class, PanchangCacheEntity::class, HoroscopeCacheEntity::class, SavedReportEntity::class, RecentSearchEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kundaliDao(): KundaliDao
    abstract fun panchangCacheDao(): PanchangCacheDao
    abstract fun horoscopeCacheDao(): HoroscopeCacheDao
    abstract fun savedReportDao(): SavedReportDao
    abstract fun recentSearchDao(): RecentSearchDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "astroveda_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
