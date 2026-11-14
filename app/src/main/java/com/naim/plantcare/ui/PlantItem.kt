package com.naim.plantcare.ui

import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.entity.PlantCategory
import com.naim.plantcare.data.entity.PlantSpecies
import com.naim.plantcare.data.entity.PlantWithDetails
import com.naim.plantcare.logic.WateringCalculator
import com.naim.plantcare.logic.WateringCalculator.WateringStatus
import java.time.LocalDate

data class PlantItem(
    val plant: Plant,
    val category: PlantCategory?,
    val species: PlantSpecies?,
    val wateringDays: Int,
    val nextWatering: LocalDate,
    val status: WateringStatus
) {
    companion object {
        fun from(details: PlantWithDetails, calculator: WateringCalculator) = PlantItem(
            plant = details.plant,
            category = details.category,
            species = details.species,
            wateringDays = calculator.wateringDays(details),
            nextWatering = calculator.nextWatering(details),
            status = calculator.statusOf(details)
        )
    }
}
