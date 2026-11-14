package com.naim.plantcare.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.naim.plantcare.data.PlantCareDatabase
import com.naim.plantcare.data.SpeciesCsvParserTest
import com.naim.plantcare.data.repository.PlantRepository
import com.naim.plantcare.ui.add.AddPlantViewModel
import com.naim.plantcare.ui.list.PlantListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AddPlantViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val today = LocalDate.of(2026, 11, 14)
    private val categories = PlantCareDatabase.PRESET_CATEGORIES.mapIndexed { index, category ->
        category.copy(id = index + 1L)
    }
    private val species = SpeciesCsvParserTest.loadCatalog().mapIndexed { index, item ->
        item.copy(id = index + 1L)
    }
    private val monstera = species.first { it.latinName == "Monstera deliciosa" }
    private lateinit var repository: PlantRepository
    private lateinit var viewModel: AddPlantViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = PlantRepository(FakePlantDao(categories, species), FakeCategoryDao(categories), FakeSpeciesDao(species))
        viewModel = AddPlantViewModel(repository) { today }
        viewModel.species.awaitValue()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun lastWatered_defaultsToToday() {
        assertEquals(today, viewModel.lastWatered.awaitValue())
    }

    @Test
    fun lastWatered_cannotBeInTheFuture() {
        viewModel.setLastWatered(today.plusDays(5))
        assertEquals(today, viewModel.lastWatered.awaitValue())
    }

    @Test
    fun findSpecies_matchesTheDropdownLabel() {
        val label = AddPlantViewModel.speciesLabel(monstera)
        assertEquals(monstera, viewModel.findSpecies(label))
        assertNull(viewModel.findSpecies("Несъществуващо растение"))
    }

    @Test
    fun currentWateringDays_followsSeasonOfLastWatered() {
        assertEquals(8, viewModel.currentWateringDays(monstera))
        viewModel.setLastWatered(LocalDate.of(2026, 7, 1))
        assertEquals(5, viewModel.currentWateringDays(monstera))
    }

    @Test
    fun buildPlant_copiesSpeciesData() {
        viewModel.setLastWatered(LocalDate.of(2026, 11, 10))
        val plant = viewModel.buildPlant("  Монстерата в хола  ", monstera, customDays = null)

        assertEquals("Монстерата в хола", plant.name)
        assertEquals("Monstera deliciosa", plant.species)
        assertEquals(monstera.id, plant.speciesId)
        assertEquals(2L, plant.categoryId)
        assertEquals(LocalDate.of(2026, 11, 10), plant.lastWatered)
        assertNull(plant.customWateringDays)
    }

    @Test
    fun save_insertsPlantAndReportsSaved() {
        viewModel.save("Монстерата в хола", monstera, customDays = 6)

        assertTrue(viewModel.saved.awaitValue())
        val items = PlantListViewModel(repository).plants.awaitValue()
        assertEquals(1, items.size)
        assertEquals("Монстерата в хола", items[0].plant.name)
        assertEquals(6, items[0].wateringDays)
        assertEquals("Тропически", items[0].category?.name)
    }
}
