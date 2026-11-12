package com.naim.plantcare.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.naim.plantcare.data.PlantCareDatabase
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
    private lateinit var plantDao: FakePlantDao
    private lateinit var repository: PlantRepository

    @Before
    fun setUp() = runBlocking {
        plantDao = FakePlantDao(categories)
        repository = PlantRepository(plantDao, FakeCategoryDao(categories))
        PlantCareDatabase.samplePlants(today).forEach { plantDao.insert(it) }
    }

    @Test
    fun listViewModel_showsAllPlantsSortedWithStatusAndCategory() {
        val items = PlantListViewModel(repository, calculator).plants.awaitValue()

        assertEquals(5, items.size)
        assertEquals(items.map { it.plant.name }.sorted(), items.map { it.plant.name })
        val orchid = items.first { it.plant.name == "Орхидея" }
        assertEquals(WateringStatus.Overdue(3), orchid.status)
        assertEquals("Цъфтящи", orchid.category?.name)
    }

    @Test
    fun listViewModel_updatesAfterInsert() = runBlocking {
        val viewModel = PlantListViewModel(repository, calculator)
        repository.insert(
            Plant(
                name = "Алое",
                species = "Aloe vera",
                wateringIntervalDays = 14,
                lastWatered = today,
                categoryId = 1
            )
        )

        val items = viewModel.plants.awaitValue()
        assertEquals(6, items.size)
        assertEquals(WateringStatus.InDays(14), items.first { it.plant.name == "Алое" }.status)
    }

    @Test
    fun detailViewModel_loadsRequestedPlant() {
        val viewModel = PlantDetailViewModel(repository, calculator)
        viewModel.loadPlant(2)

        val item = viewModel.plant.awaitValue()!!
        assertEquals("Монстера", item.plant.name)
        assertEquals(WateringStatus.Today, item.status)
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
