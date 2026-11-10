package com.naim.plantcare.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naim.plantcare.data.PlantRepository
import com.naim.plantcare.logic.WateringCalculator
import com.naim.plantcare.ui.PlantItem

class PlantListViewModel(
    private val repository: PlantRepository = PlantRepository(),
    private val calculator: WateringCalculator = WateringCalculator()
) : ViewModel() {
    private val _plants = MutableLiveData<List<PlantItem>>()

    val plants: LiveData<List<PlantItem>> = _plants

    init {
        loadPlants()
    }

    fun loadPlants() {
        _plants.value = repository.getPlants().map { PlantItem(it, calculator.statusOf(it)) }
    }
}
