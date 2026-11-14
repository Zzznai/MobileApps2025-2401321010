package com.naim.plantcare.logic

import com.naim.plantcare.data.entity.PlantWithDetails
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class WateringCalculator(private val today: () -> LocalDate = { LocalDate.now() }) {
    sealed class WateringStatus {
        data class InDays(val days: Long) : WateringStatus()
        object Today : WateringStatus()
        data class Overdue(val days: Long) : WateringStatus()
    }

    fun currentSeason(): Season = Season.of(today())

    fun wateringDays(details: PlantWithDetails): Int =
        details.plant.customWateringDays
            ?: details.species?.wateringDays(Season.of(details.plant.lastWatered))
            ?: details.category?.wateringIntervalDays
            ?: DEFAULT_WATERING_DAYS

    fun nextWatering(details: PlantWithDetails): LocalDate =
        details.plant.lastWatered.plusDays(wateringDays(details).toLong())

    fun statusOf(details: PlantWithDetails): WateringStatus {
        val days = ChronoUnit.DAYS.between(today(), nextWatering(details))
        return when {
            days > 0 -> WateringStatus.InDays(days)
            days == 0L -> WateringStatus.Today
            else -> WateringStatus.Overdue(-days)
        }
    }

    companion object {
        const val DEFAULT_WATERING_DAYS = 7
    }
}
