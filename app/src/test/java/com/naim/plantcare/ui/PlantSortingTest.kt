package com.naim.plantcare.ui

import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.logic.WateringCalculator.WateringStatus
import com.naim.plantcare.ui.list.PlantListViewModel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PlantSortingTest {
    private val today = LocalDate.of(2026, 11, 15)

    private fun item(name: String, daysUntilWatering: Long) = PlantItem(
        plant = Plant(name = name, species = "Species", lastWatered = today, categoryId = null),
        category = null,
        species = null,
        wateringDays = 7,
        nextWatering = today.plusDays(daysUntilWatering),
        status = when {
            daysUntilWatering > 0 -> WateringStatus.InDays(daysUntilWatering)
            daysUntilWatering == 0L -> WateringStatus.Today
            else -> WateringStatus.Overdue(-daysUntilWatering)
        }
    )

    private fun sortedNames(vararg items: PlantItem) =
        PlantListViewModel.sortByWateringNeed(items.toList()).map { it.plant.name }

    @Test
    fun mostOverdueComesFirst_thenToday_thenUpcoming() {
        val names = sortedNames(
            item("Фикус", 4),
            item("Босилек", 0),
            item("Орхидея", -3),
            item("Кактус", 10),
            item("Монстера", -1)
        )
        assertEquals(listOf("Орхидея", "Монстера", "Босилек", "Фикус", "Кактус"), names)
    }

    @Test
    fun sameDay_isSortedAlphabetically() {
        val names = sortedNames(
            item("Фикус", 2),
            item("Алое", 2),
            item("Монстера", 2),
            item("Босилек", 2)
        )
        assertEquals(listOf("Алое", "Босилек", "Монстера", "Фикус"), names)
    }

    @Test
    fun sameDay_alphabeticalIgnoresCase() {
        val names = sortedNames(
            item("монстера", 0),
            item("Алое", 0),
            item("босилек", 0)
        )
        assertEquals(listOf("Алое", "босилек", "монстера"), names)
    }

    @Test
    fun dateWinsOverName() {
        val names = sortedNames(
            item("Алое", 5),
            item("Явор", 1)
        )
        assertEquals(listOf("Явор", "Алое"), names)
    }
}
