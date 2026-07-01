package com.insomnia.diary.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.insomnia.diary.data.db.entity.AttendeeEntity
import com.insomnia.diary.data.db.entity.BatteryCheckpointEntity
import com.insomnia.diary.data.db.entity.DayEventEntity
import com.insomnia.diary.data.db.entity.EveningAlcoholEntity
import com.insomnia.diary.data.db.entity.EveningEntryEntity
import com.insomnia.diary.data.db.entity.EveningEntryFull
import com.insomnia.diary.data.db.entity.EveningMoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EveningDao {
    @Transaction
    @Query("SELECT * FROM evening_entry ORDER BY recordedAt DESC")
    fun observeAll(): Flow<List<EveningEntryFull>>

    @Transaction
    @Query("SELECT * FROM evening_entry ORDER BY recordedAt DESC LIMIT 1")
    fun observeLatest(): Flow<EveningEntryFull?>

    @Insert
    suspend fun insertEntry(entry: EveningEntryEntity): Long

    @Insert
    suspend fun insertMoods(moods: List<EveningMoodEntity>)

    @Insert
    suspend fun insertAlcohol(alcohol: List<EveningAlcoholEntity>)

    @Insert
    suspend fun insertEvent(event: DayEventEntity): Long

    @Insert
    suspend fun insertAttendees(attendees: List<AttendeeEntity>)

    @Insert
    suspend fun insertBattery(battery: List<BatteryCheckpointEntity>)

    @Delete
    suspend fun deleteEntry(entry: EveningEntryEntity)
}
