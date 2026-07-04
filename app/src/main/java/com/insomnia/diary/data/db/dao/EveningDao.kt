package com.insomnia.diary.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.insomnia.diary.data.db.entity.AttendeeEntity
import com.insomnia.diary.data.db.entity.CustomEventTypeEntity
import com.insomnia.diary.data.db.entity.CustomMoodEntity
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

    @Transaction
    @Query("SELECT * FROM evening_entry WHERE id = :id")
    suspend fun findById(id: Long): EveningEntryFull?

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

    @Delete
    suspend fun deleteEntry(entry: EveningEntryEntity)

    @Query("DELETE FROM evening_entry WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT label FROM custom_event_type ORDER BY label ASC")
    fun observeCustomTypes(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustomType(type: CustomEventTypeEntity)

    @Query("DELETE FROM custom_event_type WHERE label = :label")
    suspend fun deleteCustomType(label: String)

    @Query("SELECT * FROM custom_mood ORDER BY label ASC")
    fun observeCustomMoods(): Flow<List<CustomMoodEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustomMood(mood: CustomMoodEntity)

    @Query("DELETE FROM custom_mood WHERE label = :label")
    suspend fun deleteCustomMood(label: String)
}
