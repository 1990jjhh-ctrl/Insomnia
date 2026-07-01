package com.insomnia.diary.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.insomnia.diary.data.db.entity.MorningEntryEntity
import com.insomnia.diary.data.db.entity.MorningEntryFull
import com.insomnia.diary.data.db.entity.MorningMedicationEntity
import com.insomnia.diary.data.db.entity.MorningMoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MorningDao {
    @Transaction
    @Query("SELECT * FROM morning_entry ORDER BY recordedAt DESC")
    fun observeAll(): Flow<List<MorningEntryFull>>

    @Transaction
    @Query("SELECT * FROM morning_entry ORDER BY recordedAt DESC LIMIT 1")
    fun observeLatest(): Flow<MorningEntryFull?>

    @Insert
    suspend fun insertEntry(entry: MorningEntryEntity): Long

    @Insert
    suspend fun insertMoods(moods: List<MorningMoodEntity>)

    @Insert
    suspend fun insertMedication(medication: List<MorningMedicationEntity>)

    @Delete
    suspend fun deleteEntry(entry: MorningEntryEntity)
}
