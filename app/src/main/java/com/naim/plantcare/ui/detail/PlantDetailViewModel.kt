package com.naim.plantcare.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naim.plantcare.data.PlantRepository
import com.naim.plantcare.logic.WateringCalculator
import com.naim.plantcare.ui.PlantItem

class PlantDetailViewModel(
    private val repository: PlantRepository = PlantRepository(),
    private val calculator: WateringCalculator = WateringCalculator()
) : ViewModel() {
    private val _plant = MutableLiveData<PlantItem?>()

    val plant: LiveData<PlantItem?> = _plant

    fun loadPlant(id: Long) {
        _plant.value = repository.getPlant(id)?.let { PlantItem(it, calculator.statusOf(it)) }
    }
}
