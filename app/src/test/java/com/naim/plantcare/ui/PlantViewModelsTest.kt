package com.naim.plantcare.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.naim.plantcare.data.PlantRepository
import com.naim.plantcare.logic.WateringCalculator
import com.naim.plantcare.logic.WateringCalculator.WateringStatus
import com.naim.plantcare.ui.detail.PlantDetailViewModel
import com.naim.plantcare.ui.list.PlantListViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class PlantViewModelsTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val today = LocalDate.of(2026, 11, 10)
    private val repository = PlantRepository(today)
    private val calculator = WateringCalculator { today }

    @Test
    fun listViewModel_loadsAllPlantsWithStatus() {
        val viewModel = PlantListViewModel(repository, calculator)
        val items = viewModel.plants.value!!

        assertEquals(5, items.size)
        val orchid = items.first { it.plant.name == "Орхидея" }
        assertEquals(WateringStatus.Overdue(3), orchid.status)
    }

    @Test
    fun detailViewModel_loadsRequestedPlant() {
        val viewModel = PlantDetailViewModel(repository, calculator)
        viewModel.loadPlant(2)

        val item = viewModel.plant.value!!
        assertEquals("Монстера", item.plant.name)
        assertEquals(WateringStatus.Today, item.status)
    }

    @Test
    fun detailViewModel_givesNullForUnknownPlant() {
        val viewModel = PlantDetailViewModel(repository, calculator)
        viewModel.loadPlant(999)

        assertNull(viewModel.plant.value)
    }
}
