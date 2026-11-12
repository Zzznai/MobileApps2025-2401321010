package com.naim.plantcare.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.naim.plantcare.data.dao.CategoryDao
import com.naim.plantcare.data.dao.PlantDao
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.entity.PlantCategory
import com.naim.plantcare.data.entity.PlantWithCategory

class FakeCategoryDao(categories: List<PlantCategory>) : CategoryDao {
    val data = MutableLiveData(categories)

    override fun getAll(): LiveData<List<PlantCategory>> = data

    override suspend fun insertAll(categories: List<PlantCategory>) {
        data.value = data.value.orEmpty() + categories
    }
}

class FakePlantDao(private val categories: List<PlantCategory>) : PlantDao {
    private val plants = MutableLiveData<List<Plant>>(emptyList())
    private var nextId = 1L

    private fun withCategory(plant: Plant) =
        PlantWithCategory(plant, categories.firstOrNull { it.id == plant.categoryId })

    override suspend fun insert(plant: Plant): Long {
        val saved = plant.copy(id = nextId++)
        plants.value = plants.value.orEmpty() + saved
        return saved.id
    }

    override suspend fun update(plant: Plant) {
        plants.value = plants.value.orEmpty().map { if (it.id == plant.id) plant else it }
    }

    override suspend fun delete(plant: Plant) {
        plants.value = plants.value.orEmpty().filterNot { it.id == plant.id }
    }

    override fun getAll(): LiveData<List<PlantWithCategory>> =
        plants.map { list -> list.sortedBy { it.name }.map(::withCategory) }

    override fun getById(id: Long): LiveData<PlantWithCategory?> =
        plants.map { list -> list.firstOrNull { it.id == id }?.let(::withCategory) }

    override fun count(): LiveData<Int> = plants.map { it.size }
}
