package com.naim.plantcare.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.repository.PlantRepository
import com.naim.plantcare.logic.WateringCalculator
import com.naim.plantcare.ui.PlantItem
import kotlinx.coroutines.launch

class PlantListViewModel(
    private val repository: PlantRepository,
    private val calculator: WateringCalculator = WateringCalculator()
) : ViewModel() {
    val plants: LiveData<List<PlantItem>> = repository.plants.map { list ->
        list.map { PlantItem.from(it, calculator) }
    }

    fun delete(plant: Plant) {
        viewModelScope.launch { repository.delete(plant) }
    }

    fun restore(plant: Plant) {
        viewModelScope.launch { repository.insert(plant) }
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
