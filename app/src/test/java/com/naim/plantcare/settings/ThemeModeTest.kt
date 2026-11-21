package com.naim.plantcare.settings

import androidx.appcompat.app.AppCompatDelegate
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {
    @Test
    fun fromName_readsSavedMode() {
        assertEquals(ThemeMode.DARK, ThemeMode.fromName("DARK"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromName("LIGHT"))
    }

    @Test
    fun fromName_fallsBackToSystem() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName("PURPLE"))
    }

    @Test
    fun modes_mapToAppCompatNightModes() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ThemeMode.SYSTEM.nightMode)
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ThemeMode.LIGHT.nightMode)
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, ThemeMode.DARK.nightMode)
    }
}
