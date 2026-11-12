package com.naim.plantcare.data.repository

import androidx.lifecycle.LiveData
import com.naim.plantcare.data.dao.CategoryDao
import com.naim.plantcare.data.dao.PlantDao
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.entity.PlantCategory
import com.naim.plantcare.data.entity.PlantWithCategory

class PlantRepository(
    private val plantDao: PlantDao,
    private val categoryDao: CategoryDao
) {
    val plants: LiveData<List<PlantWithCategory>> = plantDao.getAll()

    val categories: LiveData<List<PlantCategory>> = categoryDao.getAll()

    val plantCount: LiveData<Int> = plantDao.count()

    fun plant(id: Long): LiveData<PlantWithCategory?> = plantDao.getById(id)

    suspend fun insert(plant: Plant): Long = plantDao.insert(plant)

    suspend fun update(plant: Plant) = plantDao.update(plant)

    suspend fun delete(plant: Plant) = plantDao.delete(plant)
}
