package com.naim.plantcare.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.repository.PlantRepository
import com.naim.plantcare.logic.WateringCalculator
import com.naim.plantcare.ui.PlantItem
import kotlinx.coroutines.launch
import java.time.LocalDate

class PlantDetailViewModel(
    private val repository: PlantRepository,
    private val calculator: WateringCalculator = WateringCalculator(),
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {
    private val plantId = MutableLiveData<Long>()

    val plant: LiveData<PlantItem?> = plantId.switchMap { id ->
        repository.plant(id).map { found ->
            found?.let { PlantItem.from(it, calculator) }
        }
    }

    private val _deleted = MutableLiveData(false)
    val deleted: LiveData<Boolean> = _deleted

    private val _detailsExpanded = MutableLiveData(false)
    val detailsExpanded: LiveData<Boolean> = _detailsExpanded

    fun toggleDetails() {
        _detailsExpanded.value = _detailsExpanded.value != true
    }

    fun loadPlant(id: Long) {
        if (plantId.value != id) plantId.value = id
    }

    fun markWatered(plant: Plant) {
        viewModelScope.launch { repository.update(plant.copy(lastWatered = today())) }
    }

    fun undoWatered(previous: Plant) {
        viewModelScope.launch { repository.update(previous) }
    }

    fun delete(plant: Plant) {
        _deleted.value = true
        viewModelScope.launch { repository.delete(plant) }
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
