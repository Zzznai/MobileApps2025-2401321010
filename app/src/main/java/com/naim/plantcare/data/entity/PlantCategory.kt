package com.naim.plantcare.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plants_cat",
    indices = [Index(value = ["name"], unique = true), Index(value = ["latinName"], unique = true)]
)
data class PlantCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latinName: String,
    val wateringIntervalDays: Int
)
