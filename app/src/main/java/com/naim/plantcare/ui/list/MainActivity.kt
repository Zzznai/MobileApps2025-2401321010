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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.naim.plantcare.R
import com.naim.plantcare.data.PhotoStorage
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
    private lateinit var fabAdd: FloatingActionButton

    private val swipeToDelete = object : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return
            deleteWithUndo(adapter.currentList[position].plant)
        }
    }

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

        fabAdd = findViewById(R.id.fabAdd)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddPlantActivity::class.java))
        }
        ItemTouchHelper(swipeToDelete).attachToRecyclerView(rvPlants)

        viewModel.plants.observe(this) { plants ->
            adapter.submitList(plants)
            tvEmpty.visibility = if (plants.isEmpty()) View.VISIBLE else View.GONE
        }

        if (savedInstanceState == null) {
            viewModel.cleanUnusedPhotos(PhotoStorage(applicationContext))
        }
    }

    private fun deleteWithUndo(plant: Plant) {
        viewModel.delete(plant)
        Snackbar.make(rvPlants, getString(R.string.plant_deleted, plant.name), Snackbar.LENGTH_LONG)
            .setAnchorView(fabAdd)
            .setAction(R.string.undo) { viewModel.restore(plant) }
            .show()
    }

    private fun openDetails(plant: Plant) {
        val intent = Intent(this, PlantDetailActivity::class.java)
        intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plant.id)
        startActivity(intent)
    }
}
