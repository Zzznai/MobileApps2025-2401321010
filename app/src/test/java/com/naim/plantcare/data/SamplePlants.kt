package com.naim.plantcare.data

import com.naim.plantcare.data.entity.Plant
import java.time.LocalDate

object SamplePlants {
    fun create(today: LocalDate): List<Plant> = listOf(
        plant("Фикус", "Ficus elastica", "Moraceae", today.minusDays(3)),
        plant("Монстера", "Monstera deliciosa", "Araceae", today.minusDays(5)),
        plant("Кактус", "Echinopsis", "Cactaceae", today.minusDays(2)),
        plant("Орхидея", "Phalaenopsis", "Orchidaceae", today.minusDays(9)),
        plant("Босилек", "Ocimum basilicum", "Lamiaceae", today.minusDays(1))
    )

    private fun plant(name: String, species: String, family: String, lastWatered: LocalDate) =
        Plant(name = name, species = species, lastWatered = lastWatered, categoryId = PlantCareDatabase.familyId(family))
}
