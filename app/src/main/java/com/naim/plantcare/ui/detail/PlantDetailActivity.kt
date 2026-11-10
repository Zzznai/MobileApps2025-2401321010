package com.naim.plantcare.ui.detail

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.naim.plantcare.R
import com.naim.plantcare.ui.PlantItem
import com.naim.plantcare.ui.WateringStatusText
import java.time.format.DateTimeFormatter

class PlantDetailActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PLANT_ID = "com.naim.plantcare.EXTRA_PLANT_ID"

        private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    private val viewModel: PlantDetailViewModel by viewModels()

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvInitial: TextView
    private lateinit var tvName: TextView
    private lateinit var tvSpecies: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvInterval: TextView
    private lateinit var tvLastWatered: TextView
    private lateinit var tvNextWatering: TextView

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
        tvStatus = findViewById(R.id.tvStatus)
        tvInterval = findViewById(R.id.tvInterval)
        tvLastWatered = findViewById(R.id.tvLastWatered)
        tvNextWatering = findViewById(R.id.tvNextWatering)

        toolbar.setNavigationOnClickListener { finish() }

        viewModel.plant.observe(this) { item ->
            if (item == null) {
                Toast.makeText(this, R.string.plant_not_found, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                showPlant(item)
            }
        }

        if (savedInstanceState == null) {
            viewModel.loadPlant(intent.getLongExtra(EXTRA_PLANT_ID, -1L))
        }
    }

    private fun showPlant(item: PlantItem) {
        val plant = item.plant
        tvInitial.text = plant.name.take(1)
        tvName.text = plant.name
        tvSpecies.text = plant.species
        WateringStatusText.show(tvStatus, item.status)
        tvInterval.text = resources.getQuantityString(
            R.plurals.every_n_days, plant.wateringIntervalDays, plant.wateringIntervalDays
        )
        tvLastWatered.text = plant.lastWatered.format(DATE_FORMAT)
        tvNextWatering.text = plant.nextWatering().format(DATE_FORMAT)
    }
}
