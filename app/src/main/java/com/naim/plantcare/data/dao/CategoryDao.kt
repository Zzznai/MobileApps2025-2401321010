package com.naim.plantcare.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.naim.plantcare.data.entity.PlantCategory

@Dao
interface CategoryDao {
    @Query("SELECT * FROM plants_cat ORDER BY name COLLATE NOCASE")
    fun getAll(): LiveData<List<PlantCategory>>

    @Insert
    suspend fun insertAll(categories: List<PlantCategory>)
}
