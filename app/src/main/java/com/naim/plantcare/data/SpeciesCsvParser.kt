package com.naim.plantcare.data

import com.naim.plantcare.data.entity.Difficulty
import com.naim.plantcare.data.entity.LightLevel
import com.naim.plantcare.data.entity.PlantSpecies

object SpeciesCsvParser {
    const val ASSET_FILE = "plant_species.csv"
    private const val COLUMNS = 10

    fun parse(lines: Sequence<String>): List<PlantSpecies> = lines
        .drop(1)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { parseLine(it) }
        .toList()

    fun parseLine(line: String): PlantSpecies {
        val cells = line.split(';').map { it.trim() }
        require(cells.size == COLUMNS) { "Expected $COLUMNS columns but got ${cells.size}: $line" }
        val column = cells.iterator()
        return PlantSpecies(
            name = column.next(),
            latinName = column.next(),
            categoryId = PlantCareDatabase.familyId(column.next()),
            light = LightLevel.valueOf(column.next()),
            waterSpringDays = column.next().toInt(),
            waterSummerDays = column.next().toInt(),
            waterAutumnDays = column.next().toInt(),
            waterWinterDays = column.next().toInt(),
            difficulty = Difficulty.valueOf(column.next()),
            toxicToPets = column.next().toBooleanStrict(),
        )
    }
}
