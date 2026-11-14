package com.naim.plantcare.data.entity

import androidx.annotation.StringRes
import com.naim.plantcare.R

enum class LightLevel(@StringRes val label: Int, val minLux: Int, val maxLux: Int) {
    FULL_SHADE(R.string.light_full_shade, 50, 500),
    PARTIAL_SHADE(R.string.light_partial_shade, 500, 2000),
    BRIGHT_INDIRECT(R.string.light_bright_indirect, 2000, 10000),
    PARTIAL_SUN(R.string.light_partial_sun, 10000, 20000),
    FULL_SUN(R.string.light_full_sun, 20000, 100000)
}
