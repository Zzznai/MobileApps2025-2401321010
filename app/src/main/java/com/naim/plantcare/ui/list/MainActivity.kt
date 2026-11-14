package com.naim.plantcare.ui.list

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.naim.plantcare.R
import com.naim.plantcare.data.PlantCareDatabase
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.data.repository.PlantRepository
import com.naim.plantcare.ui.add.AddPlantActivity
import com.naim.plantcare.ui.detail.PlantDetailActivity

class MainActivity : AppCompatActivity() {
    private val viewModel: PlantListViewModel by viewModels {
        val db = PlantCareDatabase.getInstance(applicationContext)
        PlantListViewModel.Factory(PlantRepository(db.plantDao(), db.categoryDao(), db.speciesDao()))
    }

    private lateinit var rvPlants: RecyclerView
    private lateinit var tvEmpty: TextView
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
        tvEmpty = findViewById(R.id.tvEmpty)
        adapter = PlantAdapter { plant -> openDetails(plant) }
        rvPlants.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddPlantActivity::class.java))
        }

        viewModel.plants.observe(this) { plants ->
            adapter.submitList(plants)
            tvEmpty.visibility = if (plants.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun openDetails(plant: Plant) {
        val intent = Intent(this, PlantDetailActivity::class.java)
        intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plant.id)
        startActivity(intent)
    }
}
