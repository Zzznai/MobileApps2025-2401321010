package com.naim.plantcare.data

import com.naim.plantcare.model.Plant
import java.time.LocalDate

class PlantRepository(today: LocalDate = LocalDate.now()) {
    private val plants = listOf(
        Plant(1, "Фикус", "Ficus elastica", 7, today.minusDays(3)),
        Plant(2, "Монстера", "Monstera deliciosa", 5, today.minusDays(5)),
        Plant(3, "Кактус", "Echinopsis", 14, today.minusDays(2)),
        Plant(4, "Орхидея", "Phalaenopsis", 6, today.minusDays(9)),
        Plant(5, "Босилек", "Ocimum basilicum", 2, today.minusDays(1))
    )

    fun getPlants(): List<Plant> = plants.sortedBy { it.name }

    fun getPlant(id: Long): Plant? = plants.find { it.id == id }
}
