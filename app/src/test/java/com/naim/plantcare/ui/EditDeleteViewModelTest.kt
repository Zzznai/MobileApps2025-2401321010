package com.naim.plantcare.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.naim.plantcare.data.PlantCareDatabase
import com.naim.plantcare.data.SamplePlants
import com.naim.plantcare.data.SpeciesCsvParserTest
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.repository.PlantRepository
import com.naim.plantcare.logic.WateringCalculator
import com.naim.plantcare.logic.WateringCalculator.WateringStatus
import com.naim.plantcare.ui.add.AddPlantViewModel
import com.naim.plantcare.ui.detail.PlantDetailViewModel
import com.naim.plantcare.ui.list.PlantListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class EditDeleteViewModelTest {
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
    private lateinit var repository: PlantRepository
    private lateinit var list: PlantListViewModel

    @Before
    fun setUp() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val plantDao = FakePlantDao(categories, species)
        repository = PlantRepository(plantDao, FakeCategoryDao(categories), FakeSpeciesDao(species))
        SamplePlants.create(today).forEach { plant ->
            val speciesId = species.first { it.latinName == plant.species }.id
            plantDao.insert(plant.copy(speciesId = speciesId))
        }
        list = PlantListViewModel(repository, calculator)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun plantNamed(name: String): Plant =
        list.plants.awaitValue().first { it.plant.name == name }.plant

    private fun detailFor(plant: Plant): PlantDetailViewModel =
        PlantDetailViewModel(repository, calculator) { today }.also { it.loadPlant(plant.id) }

    @Test
    fun markWatered_setsLastWateredToToday() {
        val orchid = plantNamed("Орхидея")
        val detail = detailFor(orchid)
        assertTrue(detail.plant.awaitValue()!!.status is WateringStatus.Overdue)

        detail.markWatered(orchid)

        val item = detail.plant.awaitValue()!!
        assertEquals(today, item.plant.lastWatered)
        assertEquals(WateringStatus.InDays(item.wateringDays.toLong()), item.status)
    }

    @Test
    fun undoWatered_restoresPreviousDate() {
        val orchid = plantNamed("Орхидея")
        val detail = detailFor(orchid)

        detail.markWatered(orchid)
        detail.undoWatered(orchid)

        assertEquals(orchid.lastWatered, detail.plant.awaitValue()!!.plant.lastWatered)
    }

    @Test
    fun detailDelete_removesPlantAndReportsDeleted() {
        val ficus = plantNamed("Фикус")
        val detail = detailFor(ficus)

        detail.delete(ficus)

        assertTrue(detail.deleted.awaitValue())
        assertNull(detail.plant.awaitValue())
        assertEquals(4, list.plants.awaitValue().size)
    }

    @Test
    fun swipeDelete_thenUndo_restoresSamePlant() {
        val basil = plantNamed("Босилек")

        list.delete(basil)
        assertFalse(list.plants.awaitValue().any { it.plant.id == basil.id })

        list.restore(basil)
        val restored = list.plants.awaitValue().first { it.plant.id == basil.id }.plant
        assertEquals(basil, restored)
        assertEquals(5, list.plants.awaitValue().size)
    }

    @Test
    fun editMode_updatesExistingPlantInsteadOfAddingNew() {
        val monstera = plantNamed("Монстера")
        val editor = AddPlantViewModel(repository) { today }
        val catalog = editor.species.awaitValue()

        assertTrue(editor.startEditing(monstera))
        assertTrue(editor.isEditing)
        assertEquals(monstera.lastWatered, editor.lastWatered.awaitValue())

        val newSpecies = catalog.first { it.latinName == monstera.species }
        editor.save("Монстерата в хола", newSpecies, customDays = 6)

        assertTrue(editor.saved.awaitValue())
        val items = list.plants.awaitValue()
        assertEquals(5, items.size)
        val updated = items.first { it.plant.id == monstera.id }
        assertEquals("Монстерата в хола", updated.plant.name)
        assertEquals(6, updated.wateringDays)
    }

    @Test
    fun startEditing_onlyPrefillsOnce() {
        val monstera = plantNamed("Монстера")
        val editor = AddPlantViewModel(repository) { today }

        assertTrue(editor.startEditing(monstera))
        assertFalse(editor.startEditing(monstera.copy(name = "Друго")))
    }
}
