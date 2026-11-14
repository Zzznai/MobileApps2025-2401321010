package com.naim.plantcare.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.naim.plantcare.data.dao.CategoryDao
import com.naim.plantcare.data.dao.PlantDao
import com.naim.plantcare.data.dao.SpeciesDao
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.entity.PlantCategory
import com.naim.plantcare.data.entity.PlantSpecies
import com.naim.plantcare.data.entity.PlantWithDetails

class FakeCategoryDao(categories: List<PlantCategory>) : CategoryDao {
    val data = MutableLiveData(categories)

    override fun getAll(): LiveData<List<PlantCategory>> = data

    override suspend fun insertAll(categories: List<PlantCategory>) {
        data.value = data.value.orEmpty() + categories
    }
}

class FakeSpeciesDao(species: List<PlantSpecies>) : SpeciesDao {
    val data = MutableLiveData(species)

    override fun getAll(): LiveData<List<PlantSpecies>> = data

    override fun getById(id: Long): LiveData<PlantSpecies?> = data.map { list -> list.firstOrNull { it.id == id } }

    override fun count(): LiveData<Int> = data.map { it.size }

    override suspend fun insertAll(species: List<PlantSpecies>) {
        data.value = data.value.orEmpty() + species
    }
}

class FakePlantDao(
    private val categories: List<PlantCategory>,
    private val species: List<PlantSpecies>
) : PlantDao {
    private val plants = MutableLiveData<List<Plant>>(emptyList())
    private var nextId = 1L

    private fun withDetails(plant: Plant) = PlantWithDetails(
        plant,
        categories.firstOrNull { it.id == plant.categoryId },
        species.firstOrNull { it.id == plant.speciesId }
    )

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

    override fun getAll(): LiveData<List<PlantWithDetails>> =
        plants.map { list -> list.sortedBy { it.name }.map(::withDetails) }

    override fun getById(id: Long): LiveData<PlantWithDetails?> =
        plants.map { list -> list.firstOrNull { it.id == id }?.let(::withDetails) }

    override fun count(): LiveData<Int> = plants.map { it.size }
}
