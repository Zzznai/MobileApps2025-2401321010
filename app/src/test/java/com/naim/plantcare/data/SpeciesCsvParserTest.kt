package com.naim.plantcare.data

import com.naim.plantcare.data.entity.LightLevel
import com.naim.plantcare.data.entity.PlantSpecies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SpeciesCsvParserTest {
    companion object {
        fun loadCatalog(): List<PlantSpecies> =
            File("src/main/assets/${SpeciesCsvParser.ASSET_FILE}").bufferedReader().useLines {
                SpeciesCsvParser.parse(it)
            }
    }

    private val catalog = loadCatalog()

    @Test
    fun parseLine_readsAllColumns() {
        val species = SpeciesCsvParser.parseLine("Монстера;Monstera deliciosa;2;BRIGHT_INDIRECT;7;5;8;12")
        assertEquals("Монстера", species.name)
        assertEquals("Monstera deliciosa", species.latinName)
        assertEquals(2L, species.categoryId)
        assertEquals(LightLevel.BRIGHT_INDIRECT, species.light)
        assertEquals(listOf(7, 5, 8, 12), listOf(species.waterSpringDays, species.waterSummerDays, species.waterAutumnDays, species.waterWinterDays))
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseLine_rejectsMissingColumns() {
        SpeciesCsvParser.parseLine("Монстера;Monstera deliciosa;2")
    }

    @Test
    fun parse_skipsHeaderAndBlankLines() {
        val lines = sequenceOf("name;latinName;categoryId;light;spring;summer;autumn;winter", "", "Мента;Mentha spicata;4;PARTIAL_SUN;2;1;3;5")
        assertEquals(1, SpeciesCsvParser.parse(lines).size)
    }

    @Test
    fun catalog_hasAtLeast100Plants() {
        assertTrue("only ${catalog.size} plants", catalog.size >= 100)
    }

    @Test
    fun catalog_latinNamesAreUnique() {
        val duplicates = catalog.groupBy { it.latinName }.filterValues { it.size > 1 }.keys
        assertTrue("duplicates: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun catalog_usesOnlyPresetCategories() {
        val validIds = 1L..PlantCareDatabase.PRESET_CATEGORIES.size
        assertTrue(catalog.all { it.categoryId in validIds })
    }

    @Test
    fun catalog_everyCategoryHasPlants() {
        val used = catalog.mapNotNull { it.categoryId }.toSet()
        assertEquals((1L..6L).toSet(), used)
    }

    @Test
    fun catalog_wateringIntervalsAreRealistic() {
        val bad = catalog.filter { species ->
            listOf(species.waterSpringDays, species.waterSummerDays, species.waterAutumnDays, species.waterWinterDays)
                .any { it !in 1..90 }
        }
        assertTrue("bad intervals: ${bad.map { it.name }}", bad.isEmpty())
    }

    @Test
    fun catalog_containsSamplePlants() {
        val latinNames = catalog.map { it.latinName }.toSet()
        PlantCareDatabase.samplePlants(java.time.LocalDate.now()).forEach {
            assertTrue("missing ${it.species}", it.species in latinNames)
        }
    }
}
