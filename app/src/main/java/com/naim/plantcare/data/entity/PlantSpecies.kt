package com.naim.plantcare.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.naim.plantcare.logic.Season

@Entity(
    tableName = "plant_species",
    foreignKeys = [
        ForeignKey(
            entity = PlantCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["latinName"], unique = true), Index("categoryId")]
)
data class PlantSpecies(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latinName: String,
    val categoryId: Long?,
    val light: LightLevel,
    val waterSpringDays: Int,
    val waterSummerDays: Int,
    val waterAutumnDays: Int,
    val waterWinterDays: Int,
    val difficulty: Difficulty,
    val toxicToPets: Boolean
) {
    fun wateringDays(season: Season): Int = when (season) {
        Season.SPRING -> waterSpringDays
        Season.SUMMER -> waterSummerDays
        Season.AUTUMN -> waterAutumnDays
        Season.WINTER -> waterWinterDays
    }
}
