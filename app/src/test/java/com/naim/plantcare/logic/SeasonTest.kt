package com.naim.plantcare.logic

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SeasonTest {
    private fun season(month: Int, day: Int) = Season.of(LocalDate.of(2026, month, day))

    @Test
    fun spring_isMarchToMay() {
        assertEquals(Season.SPRING, season(3, 1))
        assertEquals(Season.SPRING, season(5, 31))
    }

    @Test
    fun summer_isJuneToAugust() {
        assertEquals(Season.SUMMER, season(6, 1))
        assertEquals(Season.SUMMER, season(8, 31))
    }

    @Test
    fun autumn_isSeptemberToNovember() {
        assertEquals(Season.AUTUMN, season(9, 1))
        assertEquals(Season.AUTUMN, season(11, 30))
    }

    @Test
    fun winter_isDecemberToFebruary() {
        assertEquals(Season.WINTER, season(12, 1))
        assertEquals(Season.WINTER, season(1, 15))
        assertEquals(Season.WINTER, season(2, 28))
    }
}
