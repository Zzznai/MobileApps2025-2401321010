package com.naim.plantcare.settings

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import com.naim.plantcare.R

enum class ThemeMode(@StringRes val label: Int, val nightMode: Int) {
    SYSTEM(R.string.theme_system, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT(R.string.theme_light, AppCompatDelegate.MODE_NIGHT_NO),
    DARK(R.string.theme_dark, AppCompatDelegate.MODE_NIGHT_YES),
    ;

    companion object {
        fun fromName(name: String?): ThemeMode = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}
