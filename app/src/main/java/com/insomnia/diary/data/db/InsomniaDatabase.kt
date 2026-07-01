package com.insomnia.diary.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.insomnia.diary.data.db.dao.EveningDao
import com.insomnia.diary.data.db.dao.MorningDao
import com.insomnia.diary.data.db.entity.AttendeeEntity
import com.insomnia.diary.data.db.entity.BatteryCheckpointEntity
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
        BatteryCheckpointEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class InsomniaDatabase : RoomDatabase() {
    abstract fun morningDao(): MorningDao
    abstract fun eveningDao(): EveningDao

    companion object {
        @Volatile private var instance: InsomniaDatabase? = null

        fun getInstance(context: Context): InsomniaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    InsomniaDatabase::class.java,
                    "insomnia.db",
                ).build().also { instance = it }
            }
    }
}
