package com.naim.plantcare.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.naim.plantcare.data.entity.PlantSpecies

@Dao
interface SpeciesDao {
    @Query("SELECT * FROM plant_species ORDER BY name COLLATE NOCASE")
    fun getAll(): LiveData<List<PlantSpecies>>

    @Query("SELECT * FROM plant_species WHERE id = :id")
    fun getById(id: Long): LiveData<PlantSpecies?>

    @Query("SELECT COUNT(*) FROM plant_species")
    fun count(): LiveData<Int>

    @Insert
    suspend fun insertAll(species: List<PlantSpecies>)
}
