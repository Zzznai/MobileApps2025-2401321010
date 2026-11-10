package com.naim.plantcare.model

import java.time.LocalDate

data class Plant(
    val id: Long,
    val name: String,
    val species: String,
    val wateringIntervalDays: Int,
    val lastWatered: LocalDate
) {
    fun nextWatering(): LocalDate = lastWatered.plusDays(wateringIntervalDays.toLong())
}
