package com.naim.plantcare.ui.add

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.naim.plantcare.PlantCareApp
import com.naim.plantcare.R
import com.naim.plantcare.data.entity.PlantSpecies
import com.naim.plantcare.logic.Season
import com.naim.plantcare.ui.PlantPhoto
import com.naim.plantcare.validation.Validatable
import com.naim.plantcare.validation.ValidationRule
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class AddPlantActivity :
    AppCompatActivity(),
    Validatable {
    companion object {
        const val EXTRA_PLANT_ID = "com.naim.plantcare.EXTRA_EDIT_PLANT_ID"

        private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private const val LOG_TAG = "AddPlantActivity"
        private const val DATE_PICKER_TAG = "last_watered_picker"
        private const val KEY_PENDING_CAMERA = "pending_camera_path"
    }

    private val container by lazy { (application as PlantCareApp).container }
    private val photoStorage by lazy { container.photoStorage }

    private val viewModel: AddPlantViewModel by viewModels {
        AddPlantViewModel.Factory(container.plantRepository, photoStorage)
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        viewModel.onCameraResult(success)
    }

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::importFromGallery)
    }

    private lateinit var ivPhoto: ShapeableImageView
    private lateinit var btnRemovePhoto: MaterialButton

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
        ivPhoto = findViewById(R.id.ivPhoto)
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto)

        setUpPhoto(savedInstanceState)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        val editId = intent.getLongExtra(EXTRA_PLANT_ID, -1L)
        if (editId != -1L) loadPlantForEditing(editId, toolbar, btnSave)

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
                val message = if (viewModel.isEditing) R.string.plant_updated else R.string.plant_saved
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        btnSave.setOnClickListener { savePlant() }
    }

    private fun setUpPhoto(savedInstanceState: Bundle?) {
        if (viewModel.pendingCameraPath == null) {
            viewModel.pendingCameraPath = savedInstanceState?.getString(KEY_PENDING_CAMERA)
        }
        findViewById<MaterialButton>(R.id.btnTakePhoto).setOnClickListener { takePhoto() }
        findViewById<MaterialButton>(R.id.btnPickPhoto).setOnClickListener {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        btnRemovePhoto.setOnClickListener { viewModel.removePhoto() }
        viewModel.photoPath.observe(this) { path ->
            PlantPhoto.showInForm(ivPhoto, path)
            btnRemovePhoto.visibility = if (path == null) View.GONE else View.VISIBLE
        }
    }

    private fun loadPlantForEditing(editId: Long, toolbar: MaterialToolbar, btnSave: MaterialButton) {
        toolbar.setTitle(R.string.title_edit_plant)
        btnSave.setText(R.string.btn_save_changes)
        viewModel.plantToEdit(editId).observe(this) { details ->
            if (details != null && viewModel.startEditing(details.plant)) {
                etName.setText(details.plant.name)
                details.species?.let { actvSpecies.setText(AddPlantViewModel.speciesLabel(it), false) }
                etCustomDays.setText(details.plant.customWateringDays?.toString().orEmpty())
                showSpeciesHint()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_PENDING_CAMERA, viewModel.pendingCameraPath)
    }

    private fun takePhoto() {
        val file = viewModel.createCameraFile() ?: return
        try {
            takePicture.launch(photoStorage.uriFor(file))
        } catch (e: ActivityNotFoundException) {
            Log.w(LOG_TAG, "No camera app to take the photo", e)
            viewModel.onCameraResult(false)
            Toast.makeText(this, R.string.no_camera_app, Toast.LENGTH_SHORT).show()
        }
    }

    override fun validationRules(): List<ValidationRule> = listOf(
        ValidationRule(
            layout = tilName,
            regex = Validatable.PLANT_NAME_REGEX,
            errorMessage = getString(R.string.error_plant_name),
            allowEmpty = false,
        ),
        ValidationRule(
            layout = tilCustomDays,
            regex = Validatable.WATERING_DAYS_REGEX,
            errorMessage = getString(R.string.error_watering_days),
            allowEmpty = true,
        ),
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
                resources.getQuantityString(R.plurals.every_n_days, days, days),
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
                CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build(),
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
