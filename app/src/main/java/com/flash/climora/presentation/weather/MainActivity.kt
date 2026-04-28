package com.flash.climora.presentation.weather

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flash.climora.R
import com.flash.climora.databinding.ActivityMainBinding
import com.flash.climora.domain.model.Weather
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()

    private var errorDialog: AlertDialog? = null

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                viewModel.fetchWeatherByLocation()
            } else {
                setSearchEnabled(true)
                showErrorDialog("Location permission denied. You can still search by city.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setWeatherViewsVisible(false)
        setupListeners()
        observeState()
        requestLocationWeather()
    }

    // --------------------------
    // Setup
    // --------------------------

    private fun setupListeners() {
        binding.buttonSearch.setOnClickListener { performCitySearch() }

        binding.editCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performCitySearch()
                true
            } else false
        }

        binding.buttonLocation.setOnClickListener {
            binding.editCity.setText("")
            requestLocationWeather()
        }
    }

    private fun performCitySearch() {
        val city = binding.editCity.text.toString().trim()
        if (city.isNotBlank() && binding.buttonSearch.isEnabled) {
            viewModel.fetchWeather(city)
        }
    }

    // --------------------------
    // State Rendering
    // --------------------------

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { renderState(it) }
            }
        }
    }

    private fun renderState(state: WeatherUiState) {
        when (state) {
            is WeatherUiState.Loading -> showLoading()
            is WeatherUiState.Success -> showSuccess(state.weather)
            is WeatherUiState.Error -> showError(state.message)
            else -> Unit
        }
    }

    private fun showLoading() {
        setSearchEnabled(false)
        setWeatherViewsVisible(false)
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun showSuccess(weather: Weather) {
        binding.loadingOverlay.visibility = View.GONE
        dismissErrorDialog()

        // Swap background photo based on weather day/night.
        // Overlay colour and text colour are resolved automatically
        // from values/ (light mode) and values-night/ (dark mode).
        binding.backgroundImage.setImageResource(
            if (weather.isDay) R.drawable.day_image else R.drawable.night_image
        )

        // Weather icon
        binding.imageCondition.setImageResource(
            weatherIconRes(conditionCode = weather.conditionCode, isDay = weather.isDay)
        )

        // Main info
        binding.textTemperature.text = "${weather.temperature.toInt()}°C"
        binding.textCondition.text = weather.conditionText
        binding.textCity.text = weather.cityName
        binding.textRegion.text = buildString {
            if (weather.region.isNotBlank()) append("${weather.region}, ")
            append(weather.country)
        }

        // Detail card
        binding.textHumidity.text = weather.humidity
        binding.textFeelsLike.text = weather.feelsLike

        setWeatherViewsVisible(true)
        setSearchEnabled(true)
    }

    private fun showError(message: String) {
        binding.loadingOverlay.visibility = View.GONE
        clearWeatherUi()
        setWeatherViewsVisible(false)
        setSearchEnabled(true)
        showErrorDialog(message)
    }

    // --------------------------
    // Location
    // --------------------------

    private fun requestLocationWeather() {
        setSearchEnabled(false)
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.fetchWeatherByLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // --------------------------
    // UI Helpers
    // --------------------------

    private fun setWeatherViewsVisible(visible: Boolean) {
        val v = if (visible) View.VISIBLE else View.GONE
        binding.imageCondition.visibility = v
        binding.textTemperature.visibility = v
        binding.textCondition.visibility = v
        binding.textCity.visibility = v
        binding.textRegion.visibility = v
        binding.detailCard.visibility = v
    }

    private fun clearWeatherUi() {
        binding.textTemperature.text = ""
        binding.textCondition.text = ""
        binding.textCity.text = ""
        binding.textRegion.text = ""
        binding.textHumidity.text = ""
        binding.textFeelsLike.text = ""
        binding.imageCondition.setImageDrawable(null)
    }

    private fun setSearchEnabled(enabled: Boolean) {
        binding.buttonSearch.isEnabled = enabled
        binding.editCity.isEnabled = enabled
        binding.buttonSearch.alpha = if (enabled) 1.0f else 0.4f
        binding.editCity.alpha = if (enabled) 1.0f else 0.7f
    }

    private fun showErrorDialog(message: String) {
        if (errorDialog?.isShowing == true) return
        errorDialog = AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message.ifBlank { "Something went wrong" })
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
        errorDialog?.show()
    }

    private fun dismissErrorDialog() {
        errorDialog?.dismiss()
        errorDialog = null
    }

    override fun onDestroy() {
        dismissErrorDialog()
        super.onDestroy()
    }
}
