package com.naim.plantcare.ui.detail

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.MaterialColors
import com.naim.plantcare.R
import com.naim.plantcare.data.PlantCareDatabase
import com.naim.plantcare.data.repository.PlantRepository
import com.naim.plantcare.logic.Season
import com.naim.plantcare.ui.PlantItem
import com.naim.plantcare.ui.WateringStatusText
import java.time.format.DateTimeFormatter

class PlantDetailActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PLANT_ID = "com.naim.plantcare.EXTRA_PLANT_ID"

        private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    private val viewModel: PlantDetailViewModel by viewModels {
        val db = PlantCareDatabase.getInstance(applicationContext)
        PlantDetailViewModel.Factory(PlantRepository(db.plantDao(), db.categoryDao(), db.speciesDao()))
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvInitial: TextView
    private lateinit var tvName: TextView
    private lateinit var tvSpecies: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvInterval: TextView
    private lateinit var tvLastWatered: TextView
    private lateinit var tvNextWatering: TextView
    private lateinit var cardCare: View
    private lateinit var tvLight: TextView
    private lateinit var tvLux: TextView
    private lateinit var tvSeason: TextView
    private lateinit var tvDifficulty: TextView
    private lateinit var tvToxic: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_plant_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbar)
        tvInitial = findViewById(R.id.tvInitial)
        tvName = findViewById(R.id.tvName)
        tvSpecies = findViewById(R.id.tvSpecies)
        tvCategory = findViewById(R.id.tvCategory)
        tvStatus = findViewById(R.id.tvStatus)
        tvInterval = findViewById(R.id.tvInterval)
        tvLastWatered = findViewById(R.id.tvLastWatered)
        tvNextWatering = findViewById(R.id.tvNextWatering)
        cardCare = findViewById(R.id.cardCare)
        tvLight = findViewById(R.id.tvLight)
        tvLux = findViewById(R.id.tvLux)
        tvSeason = findViewById(R.id.tvSeason)
        tvDifficulty = findViewById(R.id.tvDifficulty)
        tvToxic = findViewById(R.id.tvToxic)

        toolbar.setNavigationOnClickListener { finish() }

        viewModel.plant.observe(this) { item ->
            if (item == null) {
                Toast.makeText(this, R.string.plant_not_found, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                showPlant(item)
            }
        }

        viewModel.loadPlant(intent.getLongExtra(EXTRA_PLANT_ID, -1L))
    }

    private fun showPlant(item: PlantItem) {
        val plant = item.plant
        tvInitial.text = plant.name.take(1)
        tvName.text = plant.name
        tvSpecies.text = plant.species
        tvCategory.text = item.category?.let { getString(R.string.family_line, it.name, it.latinName) }
            ?: getString(R.string.no_category)
        WateringStatusText.show(tvStatus, item.status)
        tvInterval.text = everyNDays(item.wateringDays)
        tvLastWatered.text = plant.lastWatered.format(DATE_FORMAT)
        tvNextWatering.text = item.nextWatering.format(DATE_FORMAT)
        showCare(item)
    }

    private fun showCare(item: PlantItem) {
        val species = item.species
        if (species == null) {
            cardCare.visibility = View.GONE
            return
        }
        cardCare.visibility = View.VISIBLE
        tvLight.text = getString(species.light.label)
        tvLux.text = getString(species.light.luxRange)
        val season = Season.of(item.plant.lastWatered)
        tvSeason.text = getString(
            R.string.season_line,
            getString(season.label),
            everyNDays(species.wateringDays(season))
        )
        tvDifficulty.text = getString(species.difficulty.label)
        tvToxic.text = getString(if (species.toxicToPets) R.string.toxic_yes else R.string.toxic_no)
        val toxicColor = if (species.toxicToPets) {
            androidx.appcompat.R.attr.colorError
        } else {
            com.google.android.material.R.attr.colorOnSurface
        }
        tvToxic.setTextColor(MaterialColors.getColor(tvToxic, toxicColor))
    }

    private fun everyNDays(days: Int): String =
        resources.getQuantityString(R.plurals.every_n_days, days, days)
}
