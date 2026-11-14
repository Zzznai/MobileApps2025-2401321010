package com.naim.plantcare.data

import com.naim.plantcare.data.entity.Difficulty
import com.naim.plantcare.data.entity.LightLevel
import com.naim.plantcare.data.entity.PlantSpecies

object SpeciesCsvParser {
    const val ASSET_FILE = "plant_species.csv"
    private const val COLUMNS = 10

    fun parse(lines: Sequence<String>): List<PlantSpecies> =
        lines
            .drop(1)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { parseLine(it) }
            .toList()

    fun parseLine(line: String): PlantSpecies {
        val cells = line.split(';').map { it.trim() }
        require(cells.size == COLUMNS) { "Expected $COLUMNS columns but got ${cells.size}: $line" }
        return PlantSpecies(
            name = cells[0],
            latinName = cells[1],
            categoryId = PlantCareDatabase.familyId(cells[2]),
            light = LightLevel.valueOf(cells[3]),
            waterSpringDays = cells[4].toInt(),
            waterSummerDays = cells[5].toInt(),
            waterAutumnDays = cells[6].toInt(),
            waterWinterDays = cells[7].toInt(),
            difficulty = Difficulty.valueOf(cells[8]),
            toxicToPets = cells[9].toBooleanStrict()
        )
    }
}
