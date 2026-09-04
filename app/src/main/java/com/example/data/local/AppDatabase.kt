package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

@Database(
    entities = [KundaliEntity::class, PanchangCacheEntity::class, HoroscopeCacheEntity::class, SavedReportEntity::class, RecentSearchEntity::class],
    version = 6,
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
         * 5 → 6.
         *
         * Three things, all of them fixing something that was actively wrong:
         *
         *  - `saved_kundali_profiles.uuid`, backfilled for every existing row. The
         *    cloud backup keyed Firestore documents on the Room autoGenerate id,
         *    which starts at 1 on every device, so two phones on one account
         *    overwrote each other's first profile. See KundaliEntity.
         *  - `panchang_cache.planetsJson`, so a cache hit stops re-running the
         *    whole ephemeris just to recover the planet list.
         *  - Indices. There were none at all, on any table, while queries filtered
         *    on cachedAtTimestamp, period and reportType and ordered by createdAt.
         *
         * Backfill runs before the unique index is created — every pre-existing row
         * would otherwise collide on the empty-string default.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_kundali_profiles ADD COLUMN uuid TEXT NOT NULL DEFAULT ''")

                db.query("SELECT id FROM saved_kundali_profiles WHERE uuid = ''").use { cursor ->
                    val ids = ArrayList<Long>(cursor.count)
                    while (cursor.moveToNext()) ids.add(cursor.getLong(0))
                    ids.forEach { id ->
                        db.execSQL(
                            "UPDATE saved_kundali_profiles SET uuid = ? WHERE id = ?",
                            arrayOf<Any>(UUID.randomUUID().toString(), id)
                        )
                    }
                }

                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_saved_kundali_profiles_uuid " +
                        "ON saved_kundali_profiles (uuid)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_saved_kundali_profiles_createdAt " +
                        "ON saved_kundali_profiles (createdAt)"
                )

                db.execSQL("ALTER TABLE panchang_cache ADD COLUMN planetsJson TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_panchang_cache_cachedAtTimestamp " +
                        "ON panchang_cache (cachedAtTimestamp)"
                )

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_horoscope_cache_period " +
                        "ON horoscope_cache (period)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_horoscope_cache_cachedAtTimestamp " +
                        "ON horoscope_cache (cachedAtTimestamp)"
                )

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_saved_astrology_reports_reportType " +
                        "ON saved_astrology_reports (reportType)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_saved_astrology_reports_createdAt " +
                        "ON saved_astrology_reports (createdAt)"
                )

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_recent_searches_createdAt " +
                        "ON recent_searches (createdAt)"
                )
            }
        }

        /** Every schema change from version 5 onward adds its migration here. */
        private val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_5_6)

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // The inner re-check is not decoration. Without it, two threads that
            // both passed the outer null check each built a RoomDatabase and the
            // second overwrote the first — two open handles to one file.
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "astroveda_database"
                )
                    // This used to be fallbackToDestructiveMigration(dropAllTables
                    // = true): every schema change silently dropped the user's
                    // saved profiles, their reports and their recent searches.
                    // Nothing warned anyone, because a wipe is not an error.
                    //
                    // Upgrades now require a real Migration in MIGRATIONS above.
                    // A missing one fails loudly in development rather than
                    // quietly destroying data in production. Downgrades — only
                    // reachable by sideloading an older build over a newer one —
                    // still fall back, since there is no forward schema to
                    // migrate from.
                    .addMigrations(*MIGRATIONS)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
