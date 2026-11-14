package com.naim.plantcare.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PlantWithDetails(
    @Embedded val plant: Plant,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: PlantCategory?,
    @Relation(parentColumn = "speciesId", entityColumn = "id")
    val species: PlantSpecies?
)
