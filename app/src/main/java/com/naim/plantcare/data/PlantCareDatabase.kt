package com.naim.plantcare.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.naim.plantcare.data.dao.CategoryDao
import com.naim.plantcare.data.dao.PlantDao
import com.naim.plantcare.data.dao.SpeciesDao
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.entity.PlantCategory
import com.naim.plantcare.data.entity.PlantSpecies
import java.time.LocalDate

@Database(
    entities = [Plant::class, PlantCategory::class, PlantSpecies::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PlantCareDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun categoryDao(): CategoryDao
    abstract fun speciesDao(): SpeciesDao

    private class PrepopulateCallback(private val context: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            prepopulate(db)
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            prepopulate(db)
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            if (isEmpty(db, "plants_cat")) {
                db.beginTransaction()
                try {
                    prepopulate(db)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

        private fun isEmpty(db: SupportSQLiteDatabase, table: String): Boolean =
            db.query("SELECT COUNT(*) FROM $table").use { cursor ->
                cursor.moveToFirst() && cursor.getLong(0) == 0L
            }

        private fun prepopulate(db: SupportSQLiteDatabase) {
            PRESET_CATEGORIES.forEach { category ->
                db.execSQL(
                    "INSERT INTO plants_cat (name, wateringIntervalDays) VALUES (?, ?)",
                    arrayOf(category.name, category.wateringIntervalDays)
                )
            }
            val species = context.assets.open(SpeciesCsvParser.ASSET_FILE).bufferedReader().useLines {
                SpeciesCsvParser.parse(it)
            }
            species.forEach { item ->
                db.execSQL(
                    "INSERT INTO plant_species (name, latinName, categoryId, light, waterSpringDays, " +
                        "waterSummerDays, waterAutumnDays, waterWinterDays) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf(
                        item.name,
                        item.latinName,
                        item.categoryId,
                        item.light.name,
                        item.waterSpringDays,
                        item.waterSummerDays,
                        item.waterAutumnDays,
                        item.waterWinterDays
                    )
                )
            }
            samplePlants(LocalDate.now()).forEach { plant ->
                db.execSQL(
                    "INSERT INTO plants (name, species, lastWatered, categoryId, speciesId) " +
                        "VALUES (?, ?, ?, ?, (SELECT id FROM plant_species WHERE latinName = ?))",
                    arrayOf(
                        plant.name,
                        plant.species,
                        plant.lastWatered.toEpochDay(),
                        plant.categoryId,
                        plant.species
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
            Plant(name = "Фикус", species = "Ficus elastica", lastWatered = today.minusDays(3), categoryId = 2),
            Plant(name = "Монстера", species = "Monstera deliciosa", lastWatered = today.minusDays(5), categoryId = 2),
            Plant(name = "Кактус", species = "Echinopsis", lastWatered = today.minusDays(2), categoryId = 1),
            Plant(name = "Орхидея", species = "Phalaenopsis", lastWatered = today.minusDays(9), categoryId = 3),
            Plant(name = "Босилек", species = "Ocimum basilicum", lastWatered = today.minusDays(1), categoryId = 4)
        )

        fun getInstance(context: Context): PlantCareDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlantCareDatabase::class.java,
                    "plantcare.db"
                )
                    .addCallback(PrepopulateCallback(context.applicationContext))
                    .fallbackToDestructiveMigrationFrom(true, 1)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
