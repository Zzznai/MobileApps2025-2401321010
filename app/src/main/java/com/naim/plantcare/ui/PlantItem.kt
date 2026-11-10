package com.naim.plantcare.ui

import com.naim.plantcare.logic.WateringCalculator.WateringStatus
import com.naim.plantcare.model.Plant

data class PlantItem(val plant: Plant, val status: WateringStatus)
