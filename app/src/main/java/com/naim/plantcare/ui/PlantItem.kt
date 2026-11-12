package com.naim.plantcare.ui

import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.entity.PlantCategory
import com.naim.plantcare.logic.WateringCalculator.WateringStatus

data class PlantItem(
    val plant: Plant,
    val category: PlantCategory?,
    val status: WateringStatus
)
