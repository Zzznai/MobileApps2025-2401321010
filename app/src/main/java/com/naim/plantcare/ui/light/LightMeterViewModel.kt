package com.naim.plantcare.ui.light

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import com.naim.plantcare.data.entity.LightLevel
import com.naim.plantcare.logic.LightCheck
import com.naim.plantcare.logic.LightVerdict

class LightMeterViewModel(val needed: LightLevel?) : ViewModel() {
    companion object {
        const val WINDOW = 5
    }

    private val readings = ArrayDeque<Float>()

    private val _lux = MutableLiveData<Float>()
    val lux: LiveData<Float> = _lux

    val level: LiveData<LightLevel> = _lux.map { LightLevel.fromLux(it) }

    val verdict: LiveData<LightVerdict?> = level.map { measured ->
        needed?.let { LightCheck.verdict(it, measured) }
    }

    fun onReading(value: Float) {
        if (value.isNaN() || value < 0f) return
        readings.addLast(value)
        if (readings.size > WINDOW) readings.removeFirst()
        _lux.value = readings.average().toFloat()
    }

    class Factory(private val needed: LightLevel?) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LightMeterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LightMeterViewModel(needed) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
