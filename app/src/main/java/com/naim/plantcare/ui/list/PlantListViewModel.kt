package com.naim.plantcare.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import com.naim.plantcare.data.repository.PlantRepository
import com.naim.plantcare.logic.WateringCalculator
import com.naim.plantcare.ui.PlantItem

class PlantListViewModel(
    repository: PlantRepository,
    private val calculator: WateringCalculator = WateringCalculator()
) : ViewModel() {
    val plants: LiveData<List<PlantItem>> = repository.plants.map { list ->
        list.map { PlantItem.from(it, calculator) }
    }

    class Factory(private val repository: PlantRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlantListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlantListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
