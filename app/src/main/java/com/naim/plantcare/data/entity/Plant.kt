package com.naim.plantcare.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "plants",
    foreignKeys = [
        ForeignKey(
            entity = PlantCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId")]
)
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val species: String,
    val wateringIntervalDays: Int,
    val lastWatered: LocalDate,
    val categoryId: Long?
) {
    fun nextWatering(): LocalDate = lastWatered.plusDays(wateringIntervalDays.toLong())
}
