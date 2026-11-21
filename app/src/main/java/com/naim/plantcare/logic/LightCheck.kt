package com.naim.plantcare.logic

import com.naim.plantcare.data.entity.LightLevel

enum class LightVerdict { TOO_DARK, GOOD, TOO_BRIGHT }

object LightCheck {
    fun verdict(needed: LightLevel, measured: LightLevel): LightVerdict = when {
        measured < needed -> LightVerdict.TOO_DARK
        measured > needed -> LightVerdict.TOO_BRIGHT
        else -> LightVerdict.GOOD
    }
}
