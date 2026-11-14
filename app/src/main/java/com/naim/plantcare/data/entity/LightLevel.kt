package com.naim.plantcare.data.entity

import androidx.annotation.StringRes
import com.naim.plantcare.R

enum class LightLevel(@StringRes val label: Int, @StringRes val luxRange: Int, val minLux: Int, val maxLux: Int) {
    DARK(R.string.light_dark, R.string.lux_dark, 0, 500),
    SHADE(R.string.light_shade, R.string.lux_shade, 500, 1500),
    PART_SHADE(R.string.light_part_shade, R.string.lux_part_shade, 1500, 5000),
    BRIGHT_INDIRECT(R.string.light_bright_indirect, R.string.lux_bright_indirect, 5000, 15000),
    FULL_SUN(R.string.light_full_sun, R.string.lux_full_sun, 15000, Int.MAX_VALUE);

    companion object {
        fun fromLux(lux: Float): LightLevel = entries.first { lux < it.maxLux }
    }
}
