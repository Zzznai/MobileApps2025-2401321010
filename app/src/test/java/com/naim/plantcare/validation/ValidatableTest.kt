package com.naim.plantcare.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatableTest {
    private fun name(text: String) = Validatable.isValid(text, Validatable.PLANT_NAME_REGEX, allowEmpty = false)
    private fun days(text: String) = Validatable.isValid(text, Validatable.WATERING_DAYS_REGEX, allowEmpty = true)

    @Test
    fun plantName_acceptsNormalNames() {
        assertTrue(name("Монстера"))
        assertTrue(name("Монстерата в хола"))
        assertTrue(name("Фикус 2"))
        assertTrue(name("Aloe vera"))
        assertTrue(name("Кактус (малкият)"))
    }

    @Test
    fun plantName_rejectsEmptyAndTooShort() {
        assertFalse(name(""))
        assertFalse(name("М"))
    }

    @Test
    fun plantName_rejectsTooLong() {
        assertFalse(name("М".repeat(41)))
        assertTrue(name("М".repeat(40)))
    }

    @Test
    fun plantName_rejectsSymbolsAndLeadingSpace() {
        assertFalse(name("<script>"))
        assertFalse(name("Монстера!!"))
        assertFalse(name(" Монстера"))
    }

    @Test
    fun wateringDays_acceptsOneToNinety() {
        assertTrue(days("1"))
        assertTrue(days("7"))
        assertTrue(days("45"))
        assertTrue(days("90"))
    }

    @Test
    fun wateringDays_rejectsOutOfRangeAndText() {
        assertFalse(days("0"))
        assertFalse(days("91"))
        assertFalse(days("07"))
        assertFalse(days("-3"))
        assertFalse(days("седем"))
    }

    @Test
    fun wateringDays_emptyIsAllowed() {
        assertTrue(days(""))
    }
}
