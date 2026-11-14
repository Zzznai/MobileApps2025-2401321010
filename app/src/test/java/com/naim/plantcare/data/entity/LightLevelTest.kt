package com.naim.plantcare.data.entity

import org.junit.Assert.assertEquals
import org.junit.Test

class LightLevelTest {
    @Test
    fun fromLux_mapsSensorReadingToLevel() {
        assertEquals(LightLevel.DARK, LightLevel.fromLux(120f))
        assertEquals(LightLevel.SHADE, LightLevel.fromLux(800f))
        assertEquals(LightLevel.PART_SHADE, LightLevel.fromLux(3000f))
        assertEquals(LightLevel.BRIGHT_INDIRECT, LightLevel.fromLux(9000f))
        assertEquals(LightLevel.FULL_SUN, LightLevel.fromLux(40000f))
    }

    @Test
    fun fromLux_boundaryBelongsToBrighterLevel() {
        assertEquals(LightLevel.SHADE, LightLevel.fromLux(500f))
        assertEquals(LightLevel.FULL_SUN, LightLevel.fromLux(15000f))
    }

    @Test
    fun levels_formContinuousRanges() {
        LightLevel.entries.zipWithNext().forEach { (lower, upper) ->
            assertEquals(lower.maxLux, upper.minLux)
        }
    }
}
