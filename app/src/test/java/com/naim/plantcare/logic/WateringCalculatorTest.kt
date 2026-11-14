package com.naim.plantcare.logic

import com.naim.plantcare.data.entity.Difficulty
import com.naim.plantcare.data.entity.LightLevel
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.entity.PlantCategory
import com.naim.plantcare.data.entity.PlantSpecies
import com.naim.plantcare.data.entity.PlantWithDetails
import com.naim.plantcare.logic.WateringCalculator.WateringStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class WateringCalculatorTest {
    private val today = LocalDate.of(2026, 11, 10)
    private val calculator = WateringCalculator { today }

    private val tropical = PlantCategory(id = 1, name = "Ароидни", latinName = "Araceae", wateringIntervalDays = 7)
    private val monstera = PlantSpecies(
        id = 1,
        name = "Монстера",
        latinName = "Monstera deliciosa",
        categoryId = 1,
        light = LightLevel.BRIGHT_INDIRECT,
        waterSpringDays = 7,
        waterSummerDays = 5,
        waterAutumnDays = 8,
        waterWinterDays = 12,
        difficulty = Difficulty.EASY,
        toxicToPets = true
    )

    private fun details(
        lastWatered: LocalDate,
        species: PlantSpecies? = monstera,
        category: PlantCategory? = tropical,
        custom: Int? = null
    ) = PlantWithDetails(
        plant = Plant(
            id = 1,
            name = "Монстера",
            species = "Monstera deliciosa",
            lastWatered = lastWatered,
            categoryId = category?.id,
            speciesId = species?.id,
            customWateringDays = custom
        ),
        category = category,
        species = species
    )

    @Test
    fun autumn_usesAutumnInterval() {
        val item = details(LocalDate.of(2026, 11, 3))
        assertEquals(8, calculator.wateringDays(item))
        assertEquals(LocalDate.of(2026, 11, 11), calculator.nextWatering(item))
    }

    @Test
    fun winter_usesWinterInterval() {
        assertEquals(12, calculator.wateringDays(details(LocalDate.of(2026, 12, 20))))
    }

    @Test
    fun summer_usesSummerInterval() {
        assertEquals(5, calculator.wateringDays(details(LocalDate.of(2026, 7, 1))))
    }

    @Test
    fun spring_usesSpringInterval() {
        assertEquals(7, calculator.wateringDays(details(LocalDate.of(2026, 4, 15))))
    }

    @Test
    fun customInterval_winsOverSpecies() {
        assertEquals(3, calculator.wateringDays(details(LocalDate.of(2026, 11, 3), custom = 3)))
    }

    @Test
    fun withoutSpecies_usesCategoryDefault() {
        assertEquals(7, calculator.wateringDays(details(LocalDate.of(2026, 11, 3), species = null)))
    }

    @Test
    fun withoutSpeciesAndCategory_usesDefault() {
        val item = details(LocalDate.of(2026, 11, 3), species = null, category = null)
        assertEquals(WateringCalculator.DEFAULT_WATERING_DAYS, calculator.wateringDays(item))
    }

    @Test
    fun nextWatering_crossesMonthEnd() {
        val item = details(LocalDate.of(2026, 11, 28))
        assertEquals(LocalDate.of(2026, 12, 6), calculator.nextWatering(item))
    }

    @Test
    fun status_inDays_whenWateringIsInTheFuture() {
        assertEquals(WateringStatus.InDays(5), calculator.statusOf(details(today.minusDays(3))))
    }

    @Test
    fun status_today_whenWateringIsDueToday() {
        assertEquals(WateringStatus.Today, calculator.statusOf(details(today.minusDays(8))))
    }

    @Test
    fun status_overdue_whenWateringWasMissed() {
        assertEquals(WateringStatus.Overdue(2), calculator.statusOf(details(today.minusDays(10))))
    }

    @Test
    fun currentSeason_followsToday() {
        assertEquals(Season.AUTUMN, calculator.currentSeason())
    }
}
