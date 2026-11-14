package com.naim.plantcare.ui.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.entity.PlantSpecies
import com.naim.plantcare.data.repository.PlantRepository
import com.naim.plantcare.logic.Season
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddPlantViewModel(
    private val repository: PlantRepository,
    private val today: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {
    val species: LiveData<List<PlantSpecies>> = repository.species

    private val _lastWatered = MutableLiveData(today())
    val lastWatered: LiveData<LocalDate> = _lastWatered

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    fun setLastWatered(date: LocalDate) {
        _lastWatered.value = if (date.isAfter(today())) today() else date
    }

    fun findSpecies(label: String): PlantSpecies? =
        species.value?.firstOrNull { speciesLabel(it) == label.trim() }

    fun currentWateringDays(species: PlantSpecies): Int =
        species.wateringDays(Season.of(_lastWatered.value ?: today()))

    fun buildPlant(name: String, species: PlantSpecies, customDays: Int?): Plant = Plant(
        name = name.trim(),
        species = species.latinName,
        lastWatered = _lastWatered.value ?: today(),
        categoryId = species.categoryId,
        speciesId = species.id,
        customWateringDays = customDays
    )

    fun save(name: String, species: PlantSpecies, customDays: Int?) {
        val plant = buildPlant(name, species, customDays)
        viewModelScope.launch {
            repository.insert(plant)
            _saved.value = true
        }
    }

    companion object {
        fun speciesLabel(species: PlantSpecies): String = "${species.name} (${species.latinName})"
    }

    class Factory(private val repository: PlantRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddPlantViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AddPlantViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
