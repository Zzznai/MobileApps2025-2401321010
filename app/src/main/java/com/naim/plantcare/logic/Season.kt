package com.naim.plantcare.logic

import androidx.annotation.StringRes
import com.naim.plantcare.R
import java.time.LocalDate
import java.time.Month

enum class Season(@StringRes val label: Int) {
    SPRING(R.string.season_spring),
    SUMMER(R.string.season_summer),
    AUTUMN(R.string.season_autumn),
    WINTER(R.string.season_winter);

    companion object {
        fun of(date: LocalDate): Season = when (date.month) {
            Month.MARCH, Month.APRIL, Month.MAY -> SPRING
            Month.JUNE, Month.JULY, Month.AUGUST -> SUMMER
            Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER -> AUTUMN
            else -> WINTER
        }
    }
}
