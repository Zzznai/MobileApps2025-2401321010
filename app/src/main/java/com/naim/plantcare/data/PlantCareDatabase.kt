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
    version = 3,
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
                    "INSERT INTO plants_cat (name, latinName, wateringIntervalDays) VALUES (?, ?, ?)",
                    arrayOf(category.name, category.latinName, category.wateringIntervalDays)
                )
            }
            val species = context.assets.open(SpeciesCsvParser.ASSET_FILE).bufferedReader().useLines {
                SpeciesCsvParser.parse(it)
            }
            species.forEach { item ->
                db.execSQL(
                    "INSERT INTO plant_species (name, latinName, categoryId, light, waterSpringDays, " +
                        "waterSummerDays, waterAutumnDays, waterWinterDays, difficulty, toxicToPets) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf(
                        item.name,
                        item.latinName,
                        item.categoryId,
                        item.light.name,
                        item.waterSpringDays,
                        item.waterSummerDays,
                        item.waterAutumnDays,
                        item.waterWinterDays,
                        item.difficulty.name,
                        if (item.toxicToPets) 1 else 0
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
            PlantCategory(name = "Ароидни", latinName = "Araceae", wateringIntervalDays = 7),
            PlantCategory(name = "Кактусови", latinName = "Cactaceae", wateringIntervalDays = 14),
            PlantCategory(name = "Дебелецови", latinName = "Crassulaceae", wateringIntervalDays = 11),
            PlantCategory(name = "Асфоделови", latinName = "Asphodelaceae", wateringIntervalDays = 14),
            PlantCategory(name = "Аизоонови", latinName = "Aizoaceae", wateringIntervalDays = 21),
            PlantCategory(name = "Аспержови", latinName = "Asparagaceae", wateringIntervalDays = 10),
            PlantCategory(name = "Сложноцветни", latinName = "Asteraceae", wateringIntervalDays = 10),
            PlantCategory(name = "Млечкови", latinName = "Euphorbiaceae", wateringIntervalDays = 8),
            PlantCategory(name = "Дидиереови", latinName = "Didiereaceae", wateringIntervalDays = 10),
            PlantCategory(name = "Черничеви", latinName = "Moraceae", wateringIntervalDays = 7),
            PlantCategory(name = "Марантови", latinName = "Marantaceae", wateringIntervalDays = 5),
            PlantCategory(name = "Палмови", latinName = "Arecaceae", wateringIntervalDays = 7),
            PlantCategory(name = "Бръшлянови", latinName = "Araliaceae", wateringIntervalDays = 6),
            PlantCategory(name = "Копривови", latinName = "Urticaceae", wateringIntervalDays = 7),
            PlantCategory(name = "Пиперови", latinName = "Piperaceae", wateringIntervalDays = 10),
            PlantCategory(name = "Олеандрови", latinName = "Apocynaceae", wateringIntervalDays = 8),
            PlantCategory(name = "Комелинови", latinName = "Commelinaceae", wateringIntervalDays = 6),
            PlantCategory(name = "Бегониеви", latinName = "Begoniaceae", wateringIntervalDays = 6),
            PlantCategory(name = "Акантови", latinName = "Acanthaceae", wateringIntervalDays = 4),
            PlantCategory(name = "Бананови", latinName = "Musaceae", wateringIntervalDays = 4),
            PlantCategory(name = "Стрелициеви", latinName = "Strelitziaceae", wateringIntervalDays = 7),
            PlantCategory(name = "Орхидееви", latinName = "Orchidaceae", wateringIntervalDays = 7),
            PlantCategory(name = "Геснериеви", latinName = "Gesneriaceae", wateringIntervalDays = 6),
            PlantCategory(name = "Слезови", latinName = "Malvaceae", wateringIntervalDays = 4),
            PlantCategory(name = "Здравецови", latinName = "Geraniaceae", wateringIntervalDays = 5),
            PlantCategory(name = "Бромелиеви", latinName = "Bromeliaceae", wateringIntervalDays = 7),
            PlantCategory(name = "Игликови", latinName = "Primulaceae", wateringIntervalDays = 7),
            PlantCategory(name = "Пиренови", latinName = "Ericaceae", wateringIntervalDays = 4),
            PlantCategory(name = "Брошови", latinName = "Rubiaceae", wateringIntervalDays = 5),
            PlantCategory(name = "Маслинови", latinName = "Oleaceae", wateringIntervalDays = 5),
            PlantCategory(name = "Нощенкови", latinName = "Nyctaginaceae", wateringIntervalDays = 7),
            PlantCategory(name = "Кокичеви", latinName = "Amaryllidaceae", wateringIntervalDays = 7),
            PlantCategory(name = "Върбовкови", latinName = "Onagraceae", wateringIntervalDays = 3),
            PlantCategory(name = "Устноцветни", latinName = "Lamiaceae", wateringIntervalDays = 5),
            PlantCategory(name = "Картофови", latinName = "Solanaceae", wateringIntervalDays = 2),
            PlantCategory(name = "Розоцветни", latinName = "Rosaceae", wateringIntervalDays = 3),
            PlantCategory(name = "Хортензиеви", latinName = "Hydrangeaceae", wateringIntervalDays = 3),
            PlantCategory(name = "Сенникови", latinName = "Apiaceae", wateringIntervalDays = 3),
            PlantCategory(name = "Житни", latinName = "Poaceae", wateringIntervalDays = 3),
            PlantCategory(name = "Лаврови", latinName = "Lauraceae", wateringIntervalDays = 7),
            PlantCategory(name = "Нефролеписови", latinName = "Nephrolepidaceae", wateringIntervalDays = 3),
            PlantCategory(name = "Птерисови", latinName = "Pteridaceae", wateringIntervalDays = 3),
            PlantCategory(name = "Асплениеви", latinName = "Aspleniaceae", wateringIntervalDays = 5),
            PlantCategory(name = "Многоножкови", latinName = "Polypodiaceae", wateringIntervalDays = 6),
            PlantCategory(name = "Давалиеви", latinName = "Davalliaceae", wateringIntervalDays = 4)
        )

        fun familyId(latinName: String): Long {
            val index = PRESET_CATEGORIES.indexOfFirst { it.latinName == latinName }
            require(index >= 0) { "Unknown plant family: $latinName" }
            return index + 1L
        }

        fun samplePlants(today: LocalDate): List<Plant> = listOf(
            samplePlant("Фикус", "Ficus elastica", "Moraceae", today.minusDays(3)),
            samplePlant("Монстера", "Monstera deliciosa", "Araceae", today.minusDays(5)),
            samplePlant("Кактус", "Echinopsis", "Cactaceae", today.minusDays(2)),
            samplePlant("Орхидея", "Phalaenopsis", "Orchidaceae", today.minusDays(9)),
            samplePlant("Босилек", "Ocimum basilicum", "Lamiaceae", today.minusDays(1))
        )

        private fun samplePlant(name: String, species: String, family: String, lastWatered: LocalDate) =
            Plant(name = name, species = species, lastWatered = lastWatered, categoryId = familyId(family))

        fun getInstance(context: Context): PlantCareDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlantCareDatabase::class.java,
                    "plantcare.db"
                )
                    .addCallback(PrepopulateCallback(context.applicationContext))
                    .fallbackToDestructiveMigrationFrom(true, 1, 2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
