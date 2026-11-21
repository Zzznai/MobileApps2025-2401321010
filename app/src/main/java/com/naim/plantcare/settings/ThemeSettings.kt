package com.naim.plantcare.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

class ThemeSettings(context: Context) {
    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_THEME = "theme_mode"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var mode: ThemeMode
        get() = ThemeMode.fromName(prefs.getString(KEY_THEME, null))
        set(value) = prefs.edit { putString(KEY_THEME, value.name) }

    fun apply() {
        AppCompatDelegate.setDefaultNightMode(mode.nightMode)
    }

    fun change(newMode: ThemeMode) {
        mode = newMode
        apply()
    }
}
