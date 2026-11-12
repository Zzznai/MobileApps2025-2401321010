package com.naim.plantcare.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.naim.plantcare.data.dao.CategoryDao
import com.naim.plantcare.data.dao.PlantDao
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.entity.PlantCategory
import java.time.LocalDate

@Database(
    entities = [Plant::class, PlantCategory::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PlantCareDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun categoryDao(): CategoryDao

    private class PrepopulateCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            PRESET_CATEGORIES.forEach { category ->
                db.execSQL(
                    "INSERT INTO plants_cat (name, wateringIntervalDays) VALUES (?, ?)",
                    arrayOf(category.name, category.wateringIntervalDays)
                )
            }
            samplePlants(LocalDate.now()).forEach { plant ->
                db.execSQL(
                    "INSERT INTO plants (name, species, wateringIntervalDays, lastWatered, categoryId) VALUES (?, ?, ?, ?, ?)",
                    arrayOf(
                        plant.name,
                        plant.species,
                        plant.wateringIntervalDays,
                        plant.lastWatered.toEpochDay(),
                        plant.categoryId
                    )
                )
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PlantCareDatabase? = null

        val PRESET_CATEGORIES = listOf(
            PlantCategory(name = "Сукуленти и кактуси", wateringIntervalDays = 14),
            PlantCategory(name = "Тропически", wateringIntervalDays = 7),
            PlantCategory(name = "Цъфтящи", wateringIntervalDays = 5),
            PlantCategory(name = "Подправки", wateringIntervalDays = 2),
            PlantCategory(name = "Папрати", wateringIntervalDays = 3),
            PlantCategory(name = "Сенколюбиви", wateringIntervalDays = 10)
        )

        fun samplePlants(today: LocalDate): List<Plant> = listOf(
            Plant(name = "Фикус", species = "Ficus elastica", wateringIntervalDays = 7, lastWatered = today.minusDays(3), categoryId = 2),
            Plant(name = "Монстера", species = "Monstera deliciosa", wateringIntervalDays = 5, lastWatered = today.minusDays(5), categoryId = 2),
            Plant(name = "Кактус", species = "Echinopsis", wateringIntervalDays = 14, lastWatered = today.minusDays(2), categoryId = 1),
            Plant(name = "Орхидея", species = "Phalaenopsis", wateringIntervalDays = 6, lastWatered = today.minusDays(9), categoryId = 3),
            Plant(name = "Босилек", species = "Ocimum basilicum", wateringIntervalDays = 2, lastWatered = today.minusDays(1), categoryId = 4)
        )

        fun getInstance(context: Context): PlantCareDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlantCareDatabase::class.java,
                    "plantcare.db"
                )
                    .addCallback(PrepopulateCallback())
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
