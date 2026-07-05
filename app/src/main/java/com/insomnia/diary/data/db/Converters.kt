package com.insomnia.diary.data.db

import androidx.room.TypeConverter
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {
    @TypeConverter
    fun fromLocalDateTime(v: LocalDateTime?): Long? = v?.toInstant(ZoneOffset.UTC)?.toEpochMilli()

    @TypeConverter fun toLocalDateTime(v: Long?): LocalDateTime? =
        v?.let {
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(it),
                ZoneOffset.UTC,
            )
        }

    @TypeConverter fun fromDuration(v: Duration?): Long? = v?.seconds

    @TypeConverter fun toDuration(v: Long?): Duration? = v?.let { Duration.ofSeconds(it) }
}
