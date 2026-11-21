package com.naim.plantcare.ui.add

import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.naim.plantcare.data.PhotoFiles
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.entity.PlantSpecies
import com.naim.plantcare.data.entity.PlantWithDetails
import com.naim.plantcare.data.repository.PlantRepository
import com.naim.plantcare.logic.Season
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

class AddPlantViewModel(
    private val repository: PlantRepository,
    private val photos: PhotoFiles? = null,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {
    val species: LiveData<List<PlantSpecies>> = repository.species

    private val _lastWatered = MutableLiveData(today())
    val lastWatered: LiveData<LocalDate> = _lastWatered

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    private val _photoPath = MutableLiveData<String?>(null)
    val photoPath: LiveData<String?> = _photoPath

    private var originalPhoto: String? = null

    var pendingCameraPath: String? = null

    private var editing: Plant? = null

    val isEditing: Boolean
        get() = editing != null

    fun plantToEdit(id: Long): LiveData<PlantWithDetails?> = repository.plant(id)

    fun startEditing(plant: Plant): Boolean {
        if (editing != null) return false
        editing = plant
        _lastWatered.value = plant.lastWatered
        originalPhoto = plant.photoPath
        _photoPath.value = plant.photoPath
        return true
    }

    fun setLastWatered(date: LocalDate) {
        _lastWatered.value = if (date.isAfter(today())) today() else date
    }

    fun createCameraFile(): File? = photos?.newPhotoFile()?.also { pendingCameraPath = it.absolutePath }

    fun onCameraResult(success: Boolean) {
        val path = pendingCameraPath ?: return
        pendingCameraPath = null
        if (success) setPhoto(path) else photos?.delete(path)
    }

    fun importFromGallery(source: Uri) {
        val storage = photos ?: return
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) { runCatching { storage.copyFrom(source) }.getOrNull() }
            file?.let { setPhoto(it.absolutePath) }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun setPhoto(path: String) {
        discardUnsavedPhoto()
        _photoPath.value = path
    }

    fun removePhoto() {
        discardUnsavedPhoto()
        _photoPath.value = null
    }

    private fun discardUnsavedPhoto() {
        val current = _photoPath.value
        if (current != null && current != originalPhoto) photos?.delete(current)
    }

    fun findSpecies(label: String): PlantSpecies? = species.value?.firstOrNull { speciesLabel(it) == label.trim() }

    fun currentWateringDays(species: PlantSpecies): Int = species.wateringDays(Season.of(_lastWatered.value ?: today()))

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun buildPlant(name: String, species: PlantSpecies, customDays: Int?): Plant {
        val base = editing ?: Plant(
            name = "",
            species = "",
            lastWatered = today(),
            categoryId = null,
        )
        return base.copy(
            name = name.trim(),
            species = species.latinName,
            lastWatered = _lastWatered.value ?: today(),
            categoryId = species.categoryId,
            speciesId = species.id,
            customWateringDays = customDays,
            photoPath = _photoPath.value,
        )
    }

    fun save(name: String, species: PlantSpecies, customDays: Int?) {
        val plant = buildPlant(name, species, customDays)
        val update = isEditing
        val replacedPhoto = originalPhoto?.takeIf { it != plant.photoPath }
        viewModelScope.launch {
            if (update) repository.update(plant) else repository.insert(plant)
            replacedPhoto?.let { photos?.delete(it) }
            originalPhoto = plant.photoPath
            _saved.value = true
        }
    }

    override fun onCleared() {
        if (_saved.value != true) discardUnsavedPhoto()
        pendingCameraPath?.let { photos?.delete(it) }
    }

    companion object {
        fun speciesLabel(species: PlantSpecies): String = "${species.name} (${species.latinName})"

        fun filterLabels(labels: List<String>, query: CharSequence?): List<String> {
            val text = query?.toString()?.trim().orEmpty()
            if (text.isEmpty()) return labels
            return labels.filter { it.contains(text, ignoreCase = true) }
        }
    }

    class Factory(private val repository: PlantRepository, private val photos: PhotoFiles) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddPlantViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AddPlantViewModel(repository, photos) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
