package com.naim.plantcare.data.entity

import androidx.annotation.StringRes
import com.naim.plantcare.R

private const val SHADE_FROM_LUX = 500
private const val PART_SHADE_FROM_LUX = 1500
private const val BRIGHT_FROM_LUX = 5000
private const val FULL_SUN_FROM_LUX = 15000

enum class LightLevel(@StringRes val label: Int, @StringRes val luxRange: Int, val minLux: Int, val maxLux: Int) {
    DARK(R.string.light_dark, R.string.lux_dark, 0, SHADE_FROM_LUX),
    SHADE(R.string.light_shade, R.string.lux_shade, SHADE_FROM_LUX, PART_SHADE_FROM_LUX),
    PART_SHADE(R.string.light_part_shade, R.string.lux_part_shade, PART_SHADE_FROM_LUX, BRIGHT_FROM_LUX),
    BRIGHT_INDIRECT(R.string.light_bright_indirect, R.string.lux_bright_indirect, BRIGHT_FROM_LUX, FULL_SUN_FROM_LUX),
    FULL_SUN(R.string.light_full_sun, R.string.lux_full_sun, FULL_SUN_FROM_LUX, Int.MAX_VALUE),
    ;

    companion object {
        fun fromLux(lux: Float): LightLevel = entries.first { lux < it.maxLux }
    }
}
