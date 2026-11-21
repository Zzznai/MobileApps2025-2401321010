package com.naim.plantcare

import android.app.Application
import com.naim.plantcare.di.AppContainer

class PlantCareApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.themeSettings.apply()
    }
}
