package com.naim.plantcare.ui

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.naim.plantcare.data.PhotoFiles
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
import java.io.File
import java.time.LocalDate

class FakePhotoFiles : PhotoFiles {
    private var counter = 0
    val deleted = mutableListOf<String>()

    override fun newPhotoFile(): File = File("photos/plant_${++counter}.jpg")

    override fun copyFrom(source: Uri): File = newPhotoFile()

    override fun delete(path: String?) {
        path?.let { deleted += it }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlantPhotoViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val today = LocalDate.of(2026, 11, 16)
    private val categories = PlantCareDatabase.PRESET_CATEGORIES.mapIndexed { index, category ->
        category.copy(id = index + 1L)
    }
    private val species = SpeciesCsvParserTest.loadCatalog().mapIndexed { index, item ->
        item.copy(id = index + 1L)
    }
    private val monstera = species.first { it.latinName == "Monstera deliciosa" }
    private lateinit var repository: PlantRepository
    private lateinit var photos: FakePhotoFiles

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = PlantRepository(
            FakePlantDao(categories, species),
            FakeCategoryDao(categories),
            FakeSpeciesDao(species),
        )
        photos = FakePhotoFiles()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newEditor() = AddPlantViewModel(repository, photos) { today }

    @Test
    fun cameraSuccess_setsPhoto() {
        val editor = newEditor()
        val file = editor.createCameraFile()!!

        editor.onCameraResult(success = true)

        assertEquals(file.absolutePath, editor.photoPath.awaitValue())
        assertNull(editor.pendingCameraPath)
    }

    @Test
    fun cameraCancelled_deletesEmptyFile() {
        val editor = newEditor()
        val file = editor.createCameraFile()!!

        editor.onCameraResult(success = false)

        assertNull(editor.photoPath.awaitValue())
        assertEquals(listOf(file.absolutePath), photos.deleted)
    }

    @Test
    fun replacingUnsavedPhoto_deletesThePreviousOne() {
        val editor = newEditor()
        editor.setPhoto("photos/first.jpg")

        editor.setPhoto("photos/second.jpg")

        assertEquals("photos/second.jpg", editor.photoPath.awaitValue())
        assertEquals(listOf("photos/first.jpg"), photos.deleted)
    }

    @Test
    fun removePhoto_clearsIt() {
        val editor = newEditor()
        editor.setPhoto("photos/first.jpg")

        editor.removePhoto()

        assertNull(editor.photoPath.awaitValue())
        assertEquals(listOf("photos/first.jpg"), photos.deleted)
    }

    @Test
    fun save_storesPhotoPathWithPlant() {
        val editor = newEditor()
        editor.awaitSpecies()
        editor.setPhoto("photos/first.jpg")

        editor.save("Монстерата в хола", monstera, customDays = null)

        val saved = PlantListViewModel(repository).plants.awaitValue().single()
        assertEquals("photos/first.jpg", saved.plant.photoPath)
        assertTrue(photos.deleted.isEmpty())
    }

    @Test
    fun editing_keepsOriginalUntilSaved_thenDeletesIt() {
        val first = newEditor()
        first.awaitSpecies()
        first.setPhoto("photos/original.jpg")
        first.save("Монстера", monstera, customDays = null)
        val plant = PlantListViewModel(repository).plants.awaitValue().single().plant

        val editor = newEditor()
        editor.startEditing(plant)
        assertEquals("photos/original.jpg", editor.photoPath.awaitValue())

        editor.setPhoto("photos/new.jpg")
        assertTrue("original must stay until saved", photos.deleted.isEmpty())

        editor.save("Монстера", monstera, customDays = null)

        assertEquals(listOf("photos/original.jpg"), photos.deleted)
        val updated = PlantListViewModel(repository).plants.awaitValue().single().plant
        assertEquals("photos/new.jpg", updated.photoPath)
    }

    private fun AddPlantViewModel.awaitSpecies() {
        species.awaitValue()
    }
}
