package com.naim.plantcare.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.naim.plantcare.data.repository.PlantRepository
import com.naim.plantcare.logic.WateringCalculator
import com.naim.plantcare.ui.PlantItem

class PlantDetailViewModel(
    private val repository: PlantRepository,
    private val calculator: WateringCalculator = WateringCalculator()
) : ViewModel() {
    private val plantId = MutableLiveData<Long>()

    val plant: LiveData<PlantItem?> = plantId.switchMap { id ->
        repository.plant(id).map { found ->
            found?.let { PlantItem(it.plant, it.category, calculator.statusOf(it.plant)) }
        }
    }

    fun loadPlant(id: Long) {
        if (plantId.value != id) plantId.value = id
    }

    class Factory(private val repository: PlantRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlantDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlantDetailViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
