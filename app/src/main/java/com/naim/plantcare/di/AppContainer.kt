package com.naim.plantcare.di

import android.content.Context
import com.naim.plantcare.data.PhotoStorage
import com.naim.plantcare.data.PlantCareDatabase
import com.naim.plantcare.data.repository.PlantRepository

class AppContainer(context: Context) {
    private val database = PlantCareDatabase.getInstance(context)

    val plantRepository = PlantRepository(database.plantDao(), database.categoryDao(), database.speciesDao())

    val photoStorage = PhotoStorage(context)
}
