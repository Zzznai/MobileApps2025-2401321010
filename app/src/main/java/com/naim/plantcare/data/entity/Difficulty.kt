package com.naim.plantcare.data.entity

import androidx.annotation.StringRes
import com.naim.plantcare.R

enum class Difficulty(@StringRes val label: Int) {
    EASY(R.string.difficulty_easy),
    MEDIUM(R.string.difficulty_medium),
    HARD(R.string.difficulty_hard)
}
