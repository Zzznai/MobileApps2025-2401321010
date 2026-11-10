package com.naim.plantcare.logic

import com.naim.plantcare.logic.WateringCalculator.WateringStatus
import com.naim.plantcare.model.Plant
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class WateringCalculatorTest {
    private val today = LocalDate.of(2026, 11, 10)
    private val calculator = WateringCalculator { today }

    private fun plant(interval: Int, lastWatered: LocalDate) =
        Plant(1, "Фикус", "Ficus elastica", interval, lastWatered)

    @Test
    fun nextWatering_isLastWateredPlusInterval() {
        val plant = plant(7, LocalDate.of(2026, 11, 3))
        assertEquals(LocalDate.of(2026, 11, 10), plant.nextWatering())
    }

    @Test
    fun nextWatering_crossesMonthEnd() {
        val plant = plant(5, LocalDate.of(2026, 11, 28))
        assertEquals(LocalDate.of(2026, 12, 3), plant.nextWatering())
    }

    @Test
    fun status_inDays_whenWateringIsInTheFuture() {
        val plant = plant(7, today.minusDays(3))
        assertEquals(WateringStatus.InDays(4), calculator.statusOf(plant))
    }

    @Test
    fun status_today_whenWateringIsDueToday() {
        val plant = plant(5, today.minusDays(5))
        assertEquals(WateringStatus.Today, calculator.statusOf(plant))
    }

    @Test
    fun status_overdue_whenWateringWasMissed() {
        val plant = plant(6, today.minusDays(9))
        assertEquals(WateringStatus.Overdue(3), calculator.statusOf(plant))
    }

    @Test
    fun status_inDays_whenJustWatered() {
        val plant = plant(14, today)
        assertEquals(WateringStatus.InDays(14), calculator.statusOf(plant))
    }
}
