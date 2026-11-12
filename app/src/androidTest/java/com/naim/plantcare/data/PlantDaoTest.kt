package com.naim.plantcare.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.naim.plantcare.data.dao.CategoryDao
import com.naim.plantcare.data.dao.PlantDao
import com.naim.plantcare.data.entity.Plant
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class PlantDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: PlantCareDatabase
    private lateinit var plantDao: PlantDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setUp() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, PlantCareDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        plantDao = db.plantDao()
        categoryDao = db.categoryDao()
        categoryDao.insertAll(PlantCareDatabase.PRESET_CATEGORIES)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun <T> LiveData<T>.awaitValue(): T {
        var result: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                result = value
                latch.countDown()
                removeObserver(this)
            }
        }
        observeForever(observer)
        latch.await(2, TimeUnit.SECONDS)
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    private fun plant(name: String, categoryId: Long?) = Plant(
        name = name,
        species = "Species",
        wateringIntervalDays = 7,
        lastWatered = LocalDate.of(2026, 11, 10),
        categoryId = categoryId
    )

    @Test
    fun insertAndReadWithCategory() = runBlocking {
        val id = plantDao.insert(plant("Фикус", 2))

        val loaded = plantDao.getById(id).awaitValue()!!
        assertEquals("Фикус", loaded.plant.name)
        assertEquals(LocalDate.of(2026, 11, 10), loaded.plant.lastWatered)
        assertEquals("Тропически", loaded.category?.name)
    }

    @Test
    fun getAll_isSortedByName() = runBlocking {
        plantDao.insert(plant("Монстера", 2))
        plantDao.insert(plant("Босилек", 4))
        plantDao.insert(plant("Кактус", 1))

        val names = plantDao.getAll().awaitValue().map { it.plant.name }
        assertEquals(listOf("Босилек", "Кактус", "Монстера"), names)
    }

    @Test
    fun update_changesStoredPlant() = runBlocking {
        val id = plantDao.insert(plant("Фикус", 2))
        val stored = plantDao.getById(id).awaitValue()!!.plant

        plantDao.update(stored.copy(wateringIntervalDays = 10))

        assertEquals(10, plantDao.getById(id).awaitValue()!!.plant.wateringIntervalDays)
    }

    @Test
    fun delete_removesPlant() = runBlocking {
        val id = plantDao.insert(plant("Фикус", 2))
        val stored = plantDao.getById(id).awaitValue()!!.plant

        plantDao.delete(stored)

        assertNull(plantDao.getById(id).awaitValue())
        assertEquals(0, plantDao.count().awaitValue())
    }
}
