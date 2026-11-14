package com.naim.plantcare.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.entity.PlantWithDetails

@Dao
interface PlantDao {
    @Insert
    suspend fun insert(plant: Plant): Long

    @Update
    suspend fun update(plant: Plant)

    @Delete
    suspend fun delete(plant: Plant)

    @Transaction
    @Query("SELECT * FROM plants ORDER BY name COLLATE NOCASE")
    fun getAll(): LiveData<List<PlantWithDetails>>

    @Transaction
    @Query("SELECT * FROM plants WHERE id = :id")
    fun getById(id: Long): LiveData<PlantWithDetails?>

    @Query("SELECT COUNT(*) FROM plants")
    fun count(): LiveData<Int>
}
