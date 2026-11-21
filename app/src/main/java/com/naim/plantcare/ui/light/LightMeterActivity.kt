package com.naim.plantcare.ui.light

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.naim.plantcare.R
import com.naim.plantcare.data.entity.LightLevel
import com.naim.plantcare.logic.LightVerdict
import java.util.Locale
import kotlin.math.roundToInt

class LightMeterActivity :
    AppCompatActivity(),
    SensorEventListener {
    companion object {
        const val EXTRA_PLANT_NAME = "com.naim.plantcare.EXTRA_PLANT_NAME"
        const val EXTRA_LIGHT_LEVEL = "com.naim.plantcare.EXTRA_LIGHT_LEVEL"

        private val BG_LOCALE: Locale = Locale.forLanguageTag("bg")
    }

    private val viewModel: LightMeterViewModel by viewModels {
        LightMeterViewModel.Factory(neededLight())
    }

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvLux: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvHint: TextView
    private lateinit var cardVerdict: MaterialCardView
    private lateinit var tvVerdict: TextView
    private lateinit var layoutLevels: LinearLayout

    private val levelRows = mutableMapOf<LightLevel, View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_light_meter)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbar)
        tvLux = findViewById(R.id.tvLux)
        tvLevel = findViewById(R.id.tvLevel)
        tvHint = findViewById(R.id.tvHint)
        cardVerdict = findViewById(R.id.cardVerdict)
        tvVerdict = findViewById(R.id.tvVerdict)
        layoutLevels = findViewById(R.id.layoutLevels)

        toolbar.setNavigationOnClickListener { finish() }
        plantName()?.let { toolbar.subtitle = it }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        buildLevelRows()

        if (lightSensor == null) {
            tvLux.setText(R.string.no_light_sensor)
            tvLevel.visibility = View.GONE
            cardVerdict.visibility = View.GONE
            return
        }

        viewModel.lux.observe(this) { lux ->
            tvLux.text = getString(R.string.lux_value, String.format(BG_LOCALE, "%,d", lux.roundToInt()))
        }
        viewModel.level.observe(this) { level ->
            tvLevel.setText(level.label)
            highlightLevel(level)
        }
        viewModel.verdict.observe(this) { verdict -> showVerdict(verdict) }
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        viewModel.onReading(event.values[0])
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun neededLight(): LightLevel? =
        intent.getStringExtra(EXTRA_LIGHT_LEVEL)?.let { name -> LightLevel.entries.firstOrNull { it.name == name } }

    private fun plantName(): String? = intent.getStringExtra(EXTRA_PLANT_NAME)

    private fun buildLevelRows() {
        val inflater = LayoutInflater.from(this)
        LightLevel.entries.reversed().forEach { level ->
            val row = inflater.inflate(R.layout.item_light_level, layoutLevels, false)
            row.findViewById<TextView>(R.id.tvLevelName).setText(level.label)
            row.findViewById<TextView>(R.id.tvLevelRange).setText(level.luxRange)
            row.findViewById<TextView>(R.id.tvNeededTag).visibility =
                if (level == viewModel.needed) View.VISIBLE else View.GONE
            layoutLevels.addView(row)
            levelRows[level] = row
        }
    }

    private fun highlightLevel(current: LightLevel) {
        val highlight = MaterialColors.getColor(layoutLevels, com.google.android.material.R.attr.colorPrimaryContainer)
        levelRows.forEach { (level, row) ->
            if (level == current) row.setBackgroundColor(highlight) else row.background = null
        }
    }

    private fun showVerdict(verdict: LightVerdict?) {
        val name = plantName()
        if (verdict == null || name == null) {
            cardVerdict.visibility = View.GONE
            return
        }
        val (textRes, containerRes, onRes) = when (verdict) {
            LightVerdict.GOOD -> Triple(R.string.light_verdict_good, R.color.status_ok_container, R.color.status_ok_on)

            LightVerdict.TOO_DARK -> Triple(
                R.string.light_verdict_dark,
                R.color.status_overdue_container,
                R.color.status_overdue_on,
            )

            LightVerdict.TOO_BRIGHT -> Triple(
                R.string.light_verdict_bright,
                R.color.status_today_container,
                R.color.status_today_on,
            )
        }
        cardVerdict.visibility = View.VISIBLE
        cardVerdict.setCardBackgroundColor(ContextCompat.getColor(this, containerRes))
        tvVerdict.setTextColor(ContextCompat.getColor(this, onRes))
        tvVerdict.text = getString(textRes, name)
    }
}
