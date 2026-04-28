package com.flash.climora.presentation.weather

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flash.climora.R
import com.flash.climora.databinding.FragmentWeatherBinding
import com.flash.climora.domain.model.Weather
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WeatherFragment : Fragment(R.layout.fragment_weather) {

    private val binding by viewBinding(FragmentWeatherBinding::bind)
    private val viewModel: WeatherViewModel by viewModels()

    private var errorDialog: AlertDialog? = null

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                viewModel.fetchWeatherByLocation()
            } else {
                showError(getString(R.string.error_location_denied))
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { renderState(it) }
            }
        }

        if (savedInstanceState == null) {
            requestLocationWeather()
        }
    }

    // --------------------------
    // Setup
    // --------------------------

    private fun setupClickListeners() {
        binding.buttonSearch.setOnClickListener { submitSearch() }

        binding.editCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                submitSearch()
                true
            } else false
        }

        binding.buttonLocation.setOnClickListener {
            binding.editCity.setText("")
            requestLocationWeather()
        }
    }

    private fun submitSearch() {
        val city = binding.editCity.text.toString().trim()
        if (city.isNotBlank() && binding.buttonSearch.isEnabled) {
            viewModel.fetchWeather(city)
        }
    }

    // --------------------------
    // State rendering
    // --------------------------

    private fun renderState(state: WeatherUiState) {
        when (state) {
            is WeatherUiState.Loading -> showLoading()
            is WeatherUiState.Success -> showSuccess(state.weather)
            is WeatherUiState.Error   -> showError(state.message)
            else                      -> Unit
        }
    }

    private fun showLoading() {
        setSearchEnabled(false)
        setWeatherVisible(false)
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    private fun showSuccess(weather: Weather) {
        binding.loadingOverlay.visibility = View.GONE
        dismissErrorDialog()

        binding.backgroundImage.setImageResource(
            if (weather.isDay) R.drawable.day_image else R.drawable.night_image
        )
        binding.imageCondition.setImageResource(
            weatherIconRes(conditionCode = weather.conditionCode, isDay = weather.isDay)
        )

        binding.textTemperature.text = getString(R.string.temperature, weather.temperature.toInt())
        binding.textCondition.text = weather.conditionText
        binding.textCity.text = weather.cityName
        binding.textRegion.text = if (weather.region.isNotBlank()) {
            getString(R.string.region_country, weather.region, weather.country)
        } else {
            getString(R.string.country_only, weather.country)
        }
        binding.textHumidity.text = weather.humidity
        binding.textFeelsLike.text = weather.feelsLike

        setWeatherVisible(true)
        setSearchEnabled(true)
    }

    private fun showError(message: String) {
        binding.loadingOverlay.visibility = View.GONE
        clearWeatherData()
        setWeatherVisible(false)
        setSearchEnabled(true)
        showErrorDialog(message)
    }

    // --------------------------
    // Permission
    // --------------------------

    private fun requestLocationWeather() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED
        ) {
            viewModel.fetchWeatherByLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // --------------------------
    // Helpers
    // --------------------------

    private fun setWeatherVisible(visible: Boolean) {
        val v = if (visible) View.VISIBLE else View.GONE
        binding.imageCondition.visibility = v
        binding.textTemperature.visibility = v
        binding.textCondition.visibility = v
        binding.textCity.visibility = v
        binding.textRegion.visibility = v
        binding.detailCard.visibility = v
    }

    private fun clearWeatherData() {
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
        errorDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.error_title)
            .setMessage(message.ifBlank { getString(R.string.error_something_went_wrong) })
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .create()
        errorDialog?.show()
    }

    private fun dismissErrorDialog() {
        errorDialog?.dismiss()
        errorDialog = null
    }

    override fun onDestroyView() {
        dismissErrorDialog()
        super.onDestroyView()
    }
}
