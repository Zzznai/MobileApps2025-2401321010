package com.naim.plantcare.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PlantRepositoryTest {
    private val today = LocalDate.of(2026, 11, 10)
    private val repository = PlantRepository(today)

    @Test
    fun getPlants_returnsAllPlantsSortedByName() {
        val names = repository.getPlants().map { it.name }
        assertEquals(5, names.size)
        assertEquals(names.sorted(), names)
    }

    @Test
    fun getPlants_lastWateredIsNeverInTheFuture() {
        assertTrue(repository.getPlants().all { !it.lastWatered.isAfter(today) })
    }

    @Test
    fun getPlant_findsExistingPlant() {
        val plant = repository.getPlant(1)
        assertNotNull(plant)
        assertEquals("Фикус", plant!!.name)
    }

    @Test
    fun getPlant_returnsNullForUnknownId() {
        assertNull(repository.getPlant(999))
    }
}
