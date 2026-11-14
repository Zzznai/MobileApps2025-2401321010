package com.naim.plantcare.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.naim.plantcare.data.PlantCareDatabase
import com.naim.plantcare.data.SpeciesCsvParserTest
import com.naim.plantcare.data.entity.LightLevel
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.repository.PlantRepository
import com.naim.plantcare.logic.WateringCalculator
import com.naim.plantcare.logic.WateringCalculator.WateringStatus
import com.naim.plantcare.ui.detail.PlantDetailViewModel
import com.naim.plantcare.ui.list.PlantListViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class PlantViewModelsTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val today = LocalDate.of(2026, 11, 10)
    private val calculator = WateringCalculator { today }
    private val categories = PlantCareDatabase.PRESET_CATEGORIES.mapIndexed { index, category ->
        category.copy(id = index + 1L)
    }
    private val species = SpeciesCsvParserTest.loadCatalog().mapIndexed { index, item ->
        item.copy(id = index + 1L)
    }
    private lateinit var plantDao: FakePlantDao
    private lateinit var repository: PlantRepository

    @Before
    fun setUp() = runBlocking {
        plantDao = FakePlantDao(categories, species)
        repository = PlantRepository(plantDao, FakeCategoryDao(categories), FakeSpeciesDao(species))
        PlantCareDatabase.samplePlants(today).forEach { plant ->
            val speciesId = species.first { it.latinName == plant.species }.id
            plantDao.insert(plant.copy(speciesId = speciesId))
        }
    }

    @Test
    fun listViewModel_showsAllPlantsSortedWithSeasonalStatus() {
        val items = PlantListViewModel(repository, calculator).plants.awaitValue()

        assertEquals(5, items.size)
        assertEquals(items.map { it.plant.name }.sorted(), items.map { it.plant.name })
        val orchid = items.first { it.plant.name == "Орхидея" }
        assertEquals(8, orchid.wateringDays)
        assertEquals(WateringStatus.Overdue(1), orchid.status)
        assertEquals("Цъфтящи", orchid.category?.name)
    }

    @Test
    fun listViewModel_updatesAfterInsert() = runBlocking {
        val viewModel = PlantListViewModel(repository, calculator)
        repository.insert(
            Plant(
                name = "Алое",
                species = "Aloe vera",
                lastWatered = today,
                categoryId = 1,
                customWateringDays = 14
            )
        )

        val items = viewModel.plants.awaitValue()
        assertEquals(6, items.size)
        assertEquals(WateringStatus.InDays(14), items.first { it.plant.name == "Алое" }.status)
    }

    @Test
    fun detailViewModel_loadsPlantWithSpeciesCare() {
        val viewModel = PlantDetailViewModel(repository, calculator)
        viewModel.loadPlant(2)

        val item = viewModel.plant.awaitValue()!!
        assertEquals("Монстера", item.plant.name)
        assertEquals(WateringStatus.InDays(3), item.status)
        assertEquals(LightLevel.BRIGHT_INDIRECT, item.species?.light)
        assertEquals("Тропически", item.category?.name)
    }

    @Test
    fun detailViewModel_givesNullForUnknownPlant() {
        val viewModel = PlantDetailViewModel(repository, calculator)
        viewModel.loadPlant(999)

        assertNull(viewModel.plant.awaitValue())
    }

    @Test
    fun detailViewModel_givesNullAfterDelete() = runBlocking {
        val viewModel = PlantDetailViewModel(repository, calculator)
        viewModel.loadPlant(1)
        val plant = viewModel.plant.awaitValue()!!.plant

        repository.delete(plant)

        assertNull(viewModel.plant.awaitValue())
    }
}
