package com.naim.plantcare.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.naim.plantcare.data.entity.LightLevel
import com.naim.plantcare.logic.LightVerdict
import com.naim.plantcare.ui.light.LightMeterViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LightMeterViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun reading_isShownAsLuxAndLevel() {
        val viewModel = LightMeterViewModel(needed = null)

        viewModel.onReading(8000f)

        assertEquals(8000f, viewModel.lux.awaitValue(), 0.01f)
        assertEquals(LightLevel.BRIGHT_INDIRECT, viewModel.level.awaitValue())
    }

    @Test
    fun readings_areAveragedOverLastWindow() {
        val viewModel = LightMeterViewModel(needed = null)

        listOf(100f, 200f, 300f, 400f, 500f, 600f).forEach(viewModel::onReading)

        assertEquals(400f, viewModel.lux.awaitValue(), 0.01f)
    }

    @Test
    fun invalidReadings_areIgnored() {
        val viewModel = LightMeterViewModel(needed = null)

        viewModel.onReading(1000f)
        viewModel.onReading(-5f)
        viewModel.onReading(Float.NaN)

        assertEquals(1000f, viewModel.lux.awaitValue(), 0.01f)
    }

    @Test
    fun verdict_comparesWithPlantNeed() {
        val viewModel = LightMeterViewModel(needed = LightLevel.BRIGHT_INDIRECT)

        viewModel.onReading(800f)
        assertEquals(LightVerdict.TOO_DARK, viewModel.verdict.awaitValue())

        repeat(LightMeterViewModel.WINDOW) { viewModel.onReading(9000f) }
        assertEquals(LightVerdict.GOOD, viewModel.verdict.awaitValue())

        repeat(LightMeterViewModel.WINDOW) { viewModel.onReading(30000f) }
        assertEquals(LightVerdict.TOO_BRIGHT, viewModel.verdict.awaitValue())
    }

    @Test
    fun verdict_isNullWithoutPlant() {
        val viewModel = LightMeterViewModel(needed = null)

        viewModel.onReading(9000f)

        assertNull(viewModel.verdict.awaitValue())
    }
}
