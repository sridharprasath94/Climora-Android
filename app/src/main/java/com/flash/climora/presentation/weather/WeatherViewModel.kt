package com.flash.climora.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.climora.core.Result
import com.flash.climora.domain.location.LocationProvider
import com.flash.climora.domain.model.Weather
import com.flash.climora.domain.usecase.GetCurrentWeatherUseCase
import com.flash.climora.presentation.error.toUiMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val locationProvider: LocationProvider,
    private val getWeatherUseCase: GetCurrentWeatherUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val state: StateFlow<WeatherUiState> = _state

    fun fetchWeatherByLocation() {
        viewModelScope.launch {
            _state.value = WeatherUiState.Loading
            when (val locationResult = locationProvider.getCurrentLocation()) {
                is Result.Success -> {
                    val coords = locationResult.data
                    handleWeatherResult(getWeatherUseCase(coords.latitude, coords.longitude))
                }
                is Result.Error -> {
                    _state.value = WeatherUiState.Error(locationResult.error.toUiMessage())
                }
            }
        }
    }

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            _state.value = WeatherUiState.Loading
            handleWeatherResult(getWeatherUseCase(city))
        }
    }

    fun fetchWeatherByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = WeatherUiState.Loading
            handleWeatherResult(getWeatherUseCase(lat, lon))
        }
    }

    private fun handleWeatherResult(result: Result<Weather>) {
        _state.value = when (result) {
            is Result.Success -> WeatherUiState.Success(result.data)
            is Result.Error -> WeatherUiState.Error(result.error.toUiMessage())
        }
    }
}
