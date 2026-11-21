package com.naim.plantcare.logic

import com.naim.plantcare.data.entity.LightLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class LightCheckTest {
    @Test
    fun sameLevel_isGood() {
        LightLevel.entries.forEach { level ->
            assertEquals(LightVerdict.GOOD, LightCheck.verdict(level, level))
        }
    }

    @Test
    fun lessLightThanNeeded_isTooDark() {
        assertEquals(LightVerdict.TOO_DARK, LightCheck.verdict(LightLevel.BRIGHT_INDIRECT, LightLevel.SHADE))
        assertEquals(LightVerdict.TOO_DARK, LightCheck.verdict(LightLevel.FULL_SUN, LightLevel.BRIGHT_INDIRECT))
    }

    @Test
    fun moreLightThanNeeded_isTooBright() {
        assertEquals(LightVerdict.TOO_BRIGHT, LightCheck.verdict(LightLevel.SHADE, LightLevel.FULL_SUN))
        assertEquals(LightVerdict.TOO_BRIGHT, LightCheck.verdict(LightLevel.DARK, LightLevel.SHADE))
    }
}
