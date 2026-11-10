package com.naim.plantcare.ui.list

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.naim.plantcare.R
import com.naim.plantcare.model.Plant
import com.naim.plantcare.ui.detail.PlantDetailActivity

class MainActivity : AppCompatActivity() {
    private val viewModel: PlantListViewModel by viewModels()

    private lateinit var rvPlants: RecyclerView
    private lateinit var adapter: PlantAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvPlants = findViewById(R.id.rvPlants)
        adapter = PlantAdapter { plant -> openDetails(plant) }
        rvPlants.adapter = adapter

        viewModel.plants.observe(this) { plants -> adapter.submitList(plants) }
    }

    private fun openDetails(plant: Plant) {
        val intent = Intent(this, PlantDetailActivity::class.java)
        intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plant.id)
        startActivity(intent)
    }
}
