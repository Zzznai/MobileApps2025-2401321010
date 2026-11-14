package com.naim.plantcare.data

import com.naim.plantcare.data.entity.Difficulty
import com.naim.plantcare.data.entity.LightLevel
import com.naim.plantcare.data.entity.PlantSpecies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        val species = SpeciesCsvParser.parseLine("Монстера;Monstera deliciosa;Araceae;BRIGHT_INDIRECT;7;5;8;12;EASY;true")
        assertEquals("Монстера", species.name)
        assertEquals("Monstera deliciosa", species.latinName)
        assertEquals(PlantCareDatabase.familyId("Araceae"), species.categoryId)
        assertEquals(LightLevel.BRIGHT_INDIRECT, species.light)
        assertEquals(listOf(7, 5, 8, 12), listOf(species.waterSpringDays, species.waterSummerDays, species.waterAutumnDays, species.waterWinterDays))
        assertEquals(Difficulty.EASY, species.difficulty)
        assertTrue(species.toxicToPets)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseLine_rejectsUnknownFamily() {
        SpeciesCsvParser.parseLine("Монстера;Monstera deliciosa;Plasticaceae;BRIGHT_INDIRECT;7;5;8;12;EASY;true")
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseLine_rejectsInvalidToxicFlag() {
        SpeciesCsvParser.parseLine("Монстера;Monstera deliciosa;Araceae;BRIGHT_INDIRECT;7;5;8;12;EASY;maybe")
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseLine_rejectsMissingColumns() {
        SpeciesCsvParser.parseLine("Монстера;Monstera deliciosa;2")
    }

    @Test
    fun parse_skipsHeaderAndBlankLines() {
        val lines = sequenceOf(
            "name;latinName;family;light;spring;summer;autumn;winter;difficulty;toxicToPets",
            "",
            "Мента;Mentha spicata;Lamiaceae;FULL_SUN;2;1;3;5;EASY;true"
        )
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
    fun catalog_everyFamilyHasPlants() {
        val used = catalog.mapNotNull { it.categoryId }.toSet()
        assertEquals((1L..PlantCareDatabase.PRESET_CATEGORIES.size).toSet(), used)
    }

    @Test
    fun catalog_hasEveryDifficultyAndBothToxicityValues() {
        assertEquals(Difficulty.entries.toSet(), catalog.map { it.difficulty }.toSet())
        assertTrue(catalog.any { it.toxicToPets })
        assertTrue(catalog.any { !it.toxicToPets })
    }

    @Test
    fun catalog_knownFamiliesAndTags() {
        val byLatin = catalog.associateBy { it.latinName }
        val cactus = byLatin.getValue("Echinopsis")
        assertEquals("Кактусови", PlantCareDatabase.PRESET_CATEGORIES[(cactus.categoryId!! - 1).toInt()].name)
        assertFalse(cactus.toxicToPets)
        assertTrue(byLatin.getValue("Monstera deliciosa").toxicToPets)
        assertEquals(Difficulty.HARD, byLatin.getValue("Ficus lyrata").difficulty)
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
