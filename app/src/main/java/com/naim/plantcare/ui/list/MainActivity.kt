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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.naim.plantcare.PlantCareApp
import com.naim.plantcare.R
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.settings.ThemeMode
import com.naim.plantcare.ui.add.AddPlantActivity
import com.naim.plantcare.ui.detail.PlantDetailActivity

class MainActivity : AppCompatActivity() {
    private val viewModel: PlantListViewModel by viewModels {
        PlantListViewModel.Factory(container.plantRepository)
    }

    private val container by lazy { (application as PlantCareApp).container }

    private lateinit var toolbar: MaterialToolbar
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

        toolbar = findViewById(R.id.toolbar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_theme) {
                chooseTheme()
                true
            } else {
                false
            }
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
            viewModel.cleanUnusedPhotos(container.photoStorage)
        }
    }

    private fun chooseTheme() {
        val settings = container.themeSettings
        val modes = ThemeMode.entries
        val labels = modes.map { getString(it.label) }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_theme)
            .setSingleChoiceItems(labels, modes.indexOf(settings.mode)) { dialog, which ->
                dialog.dismiss()
                settings.change(modes[which])
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
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
