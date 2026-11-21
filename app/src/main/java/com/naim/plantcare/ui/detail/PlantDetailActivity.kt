package com.naim.plantcare.ui.detail

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.naim.plantcare.PlantCareApp
import com.naim.plantcare.R
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.logic.Season
import com.naim.plantcare.logic.WateringCalculator.WateringStatus
import com.naim.plantcare.ui.PlantItem
import com.naim.plantcare.ui.PlantPhoto
import com.naim.plantcare.ui.WateringStatusText
import com.naim.plantcare.ui.add.AddPlantActivity
import com.naim.plantcare.ui.light.LightMeterActivity
import java.time.format.DateTimeFormatter

class PlantDetailActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PLANT_ID = "com.naim.plantcare.EXTRA_PLANT_ID"

        private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    private val viewModel: PlantDetailViewModel by viewModels {
        PlantDetailViewModel.Factory((application as PlantCareApp).container.plantRepository)
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvInitial: TextView
    private lateinit var ivPhoto: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var cardStatus: MaterialCardView
    private lateinit var tvStatus: TextView
    private lateinit var tvInterval: TextView
    private lateinit var btnWatered: MaterialButton
    private lateinit var chipsCare: ChipGroup
    private lateinit var chipLight: Chip
    private lateinit var chipDifficulty: Chip
    private lateinit var chipToxic: Chip
    private lateinit var btnMeasureLight: MaterialButton
    private lateinit var tvMoreToggle: TextView
    private lateinit var layoutMore: LinearLayout
    private lateinit var tvLastWatered: TextView
    private lateinit var tvNextWatering: TextView
    private lateinit var tvLux: TextView

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
        ivPhoto = findViewById(R.id.ivPhoto)
        tvName = findViewById(R.id.tvName)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        cardStatus = findViewById(R.id.cardStatus)
        tvStatus = findViewById(R.id.tvStatus)
        tvInterval = findViewById(R.id.tvInterval)
        btnWatered = findViewById(R.id.btnWatered)
        chipsCare = findViewById(R.id.chipsCare)
        chipLight = findViewById(R.id.chipLight)
        chipDifficulty = findViewById(R.id.chipDifficulty)
        chipToxic = findViewById(R.id.chipToxic)
        btnMeasureLight = findViewById(R.id.btnMeasureLight)
        tvMoreToggle = findViewById(R.id.tvMoreToggle)
        layoutMore = findViewById(R.id.layoutMore)
        tvLastWatered = findViewById(R.id.tvLastWatered)
        tvNextWatering = findViewById(R.id.tvNextWatering)
        tvLux = findViewById(R.id.tvLux)

        toolbar.setNavigationOnClickListener { finish() }
        toolbar.setOnMenuItemClickListener { menuItem -> onMenuItem(menuItem.itemId) }

        btnWatered.setOnClickListener { markWatered() }
        btnMeasureLight.setOnClickListener { openLightMeter() }
        tvMoreToggle.setOnClickListener { viewModel.toggleDetails() }

        viewModel.detailsExpanded.observe(this) { expanded -> showDetailsExpanded(expanded) }

        viewModel.plant.observe(this) { item ->
            when {
                item != null -> showPlant(item)

                viewModel.deleted.value == true -> Unit

                else -> {
                    Toast.makeText(this, R.string.plant_not_found, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        viewModel.deleted.observe(this) { deleted ->
            if (deleted) finish()
        }

        viewModel.loadPlant(plantId())
    }

    private fun onMenuItem(itemId: Int): Boolean = when (itemId) {
        R.id.action_edit -> {
            openEditor()
            true
        }

        R.id.action_delete -> {
            confirmDelete()
            true
        }

        else -> false
    }

    private fun showDetailsExpanded(expanded: Boolean) {
        layoutMore.visibility = if (expanded) View.VISIBLE else View.GONE
        tvMoreToggle.setText(if (expanded) R.string.less_details else R.string.more_details)
        tvMoreToggle.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            if (expanded) R.drawable.ic_collapse else R.drawable.ic_expand,
            0,
        )
    }

    private fun plantId(): Long = intent.getLongExtra(EXTRA_PLANT_ID, -1L)

    private fun currentPlant(): Plant? = viewModel.plant.value?.plant

    private fun openEditor() {
        val intent = Intent(this, AddPlantActivity::class.java)
        intent.putExtra(AddPlantActivity.EXTRA_PLANT_ID, plantId())
        startActivity(intent)
    }

    private fun openLightMeter() {
        val item = viewModel.plant.value ?: return
        val intent = Intent(this, LightMeterActivity::class.java)
        intent.putExtra(LightMeterActivity.EXTRA_PLANT_NAME, item.plant.name)
        intent.putExtra(LightMeterActivity.EXTRA_LIGHT_LEVEL, item.species?.light?.name)
        startActivity(intent)
    }

    private fun markWatered() {
        val plant = currentPlant() ?: return
        viewModel.markWatered(plant)
        Snackbar.make(toolbar, getString(R.string.watered_today_message, plant.name), Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) { viewModel.undoWatered(plant) }
            .show()
    }

    private fun confirmDelete() {
        val plant = currentPlant() ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_title)
            .setMessage(getString(R.string.delete_message, plant.name))
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.delete(plant)
                Toast.makeText(this, getString(R.string.plant_deleted, plant.name), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showPlant(item: PlantItem) {
        val plant = item.plant
        tvInitial.text = plant.name.take(1)
        PlantPhoto.showInBadge(ivPhoto, tvInitial, plant.photoPath)
        tvName.text = plant.name
        tvSubtitle.text = subtitle(item)
        showStatus(item)
        showCare(item)
        tvLastWatered.text = getString(R.string.last_watered_line, plant.lastWatered.format(DATE_FORMAT))
        tvNextWatering.text = getString(R.string.next_watering_line, item.nextWatering.format(DATE_FORMAT))
    }

    private fun subtitle(item: PlantItem): CharSequence {
        val text = SpannableStringBuilder()
        text.append(item.plant.species, StyleSpan(Typeface.ITALIC), 0)
        item.category?.let { text.append(" · ").append(it.name) }
        return text
    }

    private fun showStatus(item: PlantItem) {
        val (containerRes, onRes) = when (item.status) {
            is WateringStatus.InDays -> R.color.status_ok_container to R.color.status_ok_on
            is WateringStatus.Today -> R.color.status_today_container to R.color.status_today_on
            is WateringStatus.Overdue -> R.color.status_overdue_container to R.color.status_overdue_on
        }
        val container = ContextCompat.getColor(this, containerRes)
        val onColor = ContextCompat.getColor(this, onRes)

        cardStatus.setCardBackgroundColor(container)
        tvStatus.text = WateringStatusText.text(this, item.status)
        tvStatus.setTextColor(onColor)
        tvInterval.setTextColor(onColor)

        val interval = everyNDays(item.wateringDays)
        tvInterval.text = if (item.plant.customWateringDays == null && item.species != null) {
            val season = getString(Season.of(item.plant.lastWatered).label).lowercase()
            getString(R.string.interval_with_season, interval, season)
        } else {
            interval
        }

        btnWatered.backgroundTintList = ColorStateList.valueOf(onColor)
        btnWatered.setTextColor(container)
        btnWatered.iconTint = ColorStateList.valueOf(container)
    }

    private fun showCare(item: PlantItem) {
        val species = item.species
        if (species == null) {
            chipsCare.visibility = View.GONE
            btnMeasureLight.visibility = View.GONE
            tvLux.visibility = View.GONE
            return
        }
        chipsCare.visibility = View.VISIBLE
        btnMeasureLight.visibility = View.VISIBLE
        chipLight.text = getString(species.light.label)
        chipDifficulty.text = getString(species.difficulty.label)
        chipToxic.visibility = if (species.toxicToPets) View.VISIBLE else View.GONE
        tvLux.visibility = View.VISIBLE
        tvLux.text = getString(R.string.light_need_line, getString(species.light.luxRange))
    }

    private fun everyNDays(days: Int): String = resources.getQuantityString(R.plurals.every_n_days, days, days)
}
