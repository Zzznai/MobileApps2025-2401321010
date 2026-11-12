package com.naim.plantcare.logic

import com.naim.plantcare.data.entity.Plant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class WateringCalculator(private val today: () -> LocalDate = { LocalDate.now() }) {
    sealed class WateringStatus {
        data class InDays(val days: Long) : WateringStatus()
        object Today : WateringStatus()
        data class Overdue(val days: Long) : WateringStatus()
    }

    fun statusOf(plant: Plant): WateringStatus {
        val days = ChronoUnit.DAYS.between(today(), plant.nextWatering())
        return when {
            days > 0 -> WateringStatus.InDays(days)
            days == 0L -> WateringStatus.Today
            else -> WateringStatus.Overdue(-days)
        }
    }
}
