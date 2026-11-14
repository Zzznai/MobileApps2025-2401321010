package com.naim.plantcare.data

import androidx.room.TypeConverter
import com.naim.plantcare.data.entity.Difficulty
import com.naim.plantcare.data.entity.LightLevel
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun fromLightLevel(level: LightLevel?): String? = level?.name

    @TypeConverter
    fun toLightLevel(name: String?): LightLevel? = name?.let { LightLevel.valueOf(it) }

    @TypeConverter
    fun fromDifficulty(difficulty: Difficulty?): String? = difficulty?.name

    @TypeConverter
    fun toDifficulty(name: String?): Difficulty? = name?.let { Difficulty.valueOf(it) }
}
