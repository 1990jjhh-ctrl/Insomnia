package com.insomnia.diary.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.insomnia.diary.data.db.dao.EveningDao
import com.insomnia.diary.data.db.dao.MorningDao
import com.insomnia.diary.data.db.entity.AttendeeEntity
import com.insomnia.diary.data.db.entity.CustomEventTypeEntity
import com.insomnia.diary.data.db.entity.CustomMoodEntity
import com.insomnia.diary.data.db.entity.DayEventEntity
import com.insomnia.diary.data.db.entity.EveningAlcoholEntity
import com.insomnia.diary.data.db.entity.EveningEntryEntity
import com.insomnia.diary.data.db.entity.EveningMoodEntity
import com.insomnia.diary.data.db.entity.MorningEntryEntity
import com.insomnia.diary.data.db.entity.MorningMedicationEntity
import com.insomnia.diary.data.db.entity.MorningMoodEntity

@Database(
    entities = [
        MorningEntryEntity::class,
        MorningMoodEntity::class,
        MorningMedicationEntity::class,
        EveningEntryEntity::class,
        EveningMoodEntity::class,
        EveningAlcoholEntity::class,
        DayEventEntity::class,
        AttendeeEntity::class,
        CustomEventTypeEntity::class,
        CustomMoodEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class InsomniaDatabase : RoomDatabase() {
    abstract fun morningDao(): MorningDao
    abstract fun eveningDao(): EveningDao

    companion object {
        @Volatile private var instance: InsomniaDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE day_event ADD COLUMN batteryLevel INTEGER")
                db.execSQL("DROP TABLE IF EXISTS battery_checkpoint")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE day_event SET batteryLevel = 70 WHERE batteryLevel IS NULL")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS custom_event_type (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "label TEXT NOT NULL" +
                        ")"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                        "`index_custom_event_type_label` " +
                        "ON `custom_event_type` (`label`)"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Retroactively add the named index that MIGRATION_3_4 missed.
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                        "`index_custom_event_type_label` " +
                        "ON `custom_event_type` (`label`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS custom_mood (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "label TEXT NOT NULL, " +
                        "valence REAL NOT NULL DEFAULT 0.0, " +
                        "arousal REAL NOT NULL DEFAULT 0.0" +
                        ")"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                        "`index_custom_mood_label` " +
                        "ON `custom_mood` (`label`)"
                )
            }
        }

        fun getInstance(context: Context): InsomniaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    InsomniaDatabase::class.java,
                    "insomnia.db",
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build().also { instance = it }
            }
    }
}
