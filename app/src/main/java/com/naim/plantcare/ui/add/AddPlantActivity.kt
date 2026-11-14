package com.naim.plantcare.ui.add

import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.naim.plantcare.R
import com.naim.plantcare.data.PlantCareDatabase
import com.naim.plantcare.data.entity.PlantSpecies
import com.naim.plantcare.data.repository.PlantRepository
import com.naim.plantcare.logic.Season
import com.naim.plantcare.validation.Validatable
import com.naim.plantcare.validation.ValidationRule
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class AddPlantActivity : AppCompatActivity(), Validatable {
    companion object {
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private const val DATE_PICKER_TAG = "last_watered_picker"
    }

    private val viewModel: AddPlantViewModel by viewModels {
        val db = PlantCareDatabase.getInstance(applicationContext)
        AddPlantViewModel.Factory(PlantRepository(db.plantDao(), db.categoryDao(), db.speciesDao()))
    }

    private lateinit var tilName: TextInputLayout
    private lateinit var tilSpecies: TextInputLayout
    private lateinit var tilCustomDays: TextInputLayout
    private lateinit var tilLastWatered: TextInputLayout

    private lateinit var etName: TextInputEditText
    private lateinit var actvSpecies: AutoCompleteTextView
    private lateinit var etCustomDays: TextInputEditText
    private lateinit var etLastWatered: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_plant)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        tilName = findViewById(R.id.tilName)
        tilSpecies = findViewById(R.id.tilSpecies)
        tilCustomDays = findViewById(R.id.tilCustomDays)
        tilLastWatered = findViewById(R.id.tilLastWatered)

        etName = findViewById(R.id.etName)
        actvSpecies = findViewById(R.id.actvSpecies)
        etCustomDays = findViewById(R.id.etCustomDays)
        etLastWatered = findViewById(R.id.etLastWatered)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        viewModel.species.observe(this) { list ->
            actvSpecies.setAdapter(SpeciesAdapter(this, list.map(AddPlantViewModel::speciesLabel)))
            showSpeciesHint()
        }

        actvSpecies.setOnItemClickListener { _, _, _, _ ->
            tilSpecies.error = null
            showSpeciesHint()
        }

        viewModel.lastWatered.observe(this) { date ->
            etLastWatered.setText(date.format(DATE_FORMAT))
            showSpeciesHint()
        }
        etLastWatered.setOnClickListener { showDatePicker() }
        tilLastWatered.setEndIconOnClickListener { showDatePicker() }

        viewModel.saved.observe(this) { saved ->
            if (saved) {
                Toast.makeText(this, R.string.plant_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener { savePlant() }
    }

    override fun validationRules(): List<ValidationRule> = listOf(
        ValidationRule(
            layout = tilName,
            regex = Validatable.PLANT_NAME_REGEX,
            errorMessage = getString(R.string.error_plant_name),
            allowEmpty = false
        ),
        ValidationRule(
            layout = tilCustomDays,
            regex = Validatable.WATERING_DAYS_REGEX,
            errorMessage = getString(R.string.error_watering_days),
            allowEmpty = true
        )
    )

    private fun selectedSpecies(): PlantSpecies? = viewModel.findSpecies(actvSpecies.text?.toString().orEmpty())

    private fun showSpeciesHint() {
        val species = selectedSpecies()
        tilSpecies.helperText = species?.let {
            val season = Season.of(viewModel.lastWatered.value ?: LocalDate.now())
            val days = viewModel.currentWateringDays(it)
            getString(
                R.string.species_hint,
                getString(it.light.label),
                getString(season.label),
                resources.getQuantityString(R.plurals.every_n_days, days, days)
            )
        }
    }

    private fun showDatePicker() {
        if (supportFragmentManager.findFragmentByTag(DATE_PICKER_TAG) != null) return
        val selected = viewModel.lastWatered.value ?: LocalDate.now()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.hint_last_watered)
            .setSelection(selected.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
            .setCalendarConstraints(
                CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build()
            )
            .build()
        picker.addOnPositiveButtonClickListener { millis ->
            viewModel.setLastWatered(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
        }
        picker.show(supportFragmentManager, DATE_PICKER_TAG)
    }

    private fun savePlant() {
        clearValidationErrors()
        tilSpecies.error = null

        val nameValid = validateInputs()
        val species = selectedSpecies()
        if (species == null) {
            tilSpecies.error = getString(R.string.error_species_required)
        }
        if (!nameValid || species == null) return

        val customDays = etCustomDays.text?.toString()?.trim()?.toIntOrNull()
        viewModel.save(etName.text.toString(), species, customDays)
    }
}
