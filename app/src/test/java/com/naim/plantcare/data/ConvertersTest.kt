package com.naim.plantcare.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun localDate_roundTrip() {
        val date = LocalDate.of(2026, 11, 10)
        assertEquals(date, converters.toLocalDate(converters.fromLocalDate(date)))
    }

    @Test
    fun localDate_storedAsEpochDay() {
        assertEquals(0L, converters.fromLocalDate(LocalDate.of(1970, 1, 1)))
    }

    @Test
    fun nullStaysNull() {
        assertNull(converters.fromLocalDate(null))
        assertNull(converters.toLocalDate(null))
    }
}
