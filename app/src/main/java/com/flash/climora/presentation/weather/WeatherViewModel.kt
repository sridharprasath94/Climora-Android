package com.flash.climora.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.climora.core.Result
import com.flash.climora.domain.location.LocationProvider
import com.flash.climora.domain.model.ForecastDay
import com.flash.climora.domain.model.Weather
import com.flash.climora.domain.usecase.GetCurrentWeatherUseCase
import com.flash.climora.domain.usecase.GetForecastUseCase
import com.flash.climora.presentation.error.toUiMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val locationProvider: LocationProvider,
    private val getWeatherUseCase: GetCurrentWeatherUseCase,
    private val getForecastUseCase: GetForecastUseCase
) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val weatherState: StateFlow<WeatherUiState> = _weatherState

    private val _forecastState = MutableStateFlow<ForecastUiState>(ForecastUiState.Idle)
    val forecastState: StateFlow<ForecastUiState> = _forecastState

    fun fetchWeatherByLocation() {
        viewModelScope.launch {
            _weatherState.update { WeatherUiState.Loading }
            _forecastState.update { ForecastUiState.Loading }

            when (val locationResult = locationProvider.getCurrentLocation()) {
                is Result.Success -> {
                    val coords = locationResult.data
                    loadWeatherAndForecastByCoordinates(coords.latitude, coords.longitude)
                }
                is Result.Error -> {
                    _weatherState.update { WeatherUiState.Error(locationResult.error.toUiMessage()) }
                    _forecastState.update { ForecastUiState.Error }
                }
            }
        }
    }

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            _weatherState.update { WeatherUiState.Loading }
            _forecastState.update { ForecastUiState.Loading }

            // Launch weather and forecast in parallel
            val weatherDeferred = async { getWeatherUseCase(city) }
            val forecastDeferred = async { getForecastUseCase(city) }

            handleWeatherResult(weatherDeferred.await())
            handleForecastResult(forecastDeferred.await())
        }
    }

    private suspend fun loadWeatherAndForecastByCoordinates(lat: Double, lon: Double) {
        val weatherDeferred = viewModelScope.async { getWeatherUseCase(lat, lon) }
        val forecastDeferred = viewModelScope.async { getForecastUseCase(lat, lon) }

        handleWeatherResult(weatherDeferred.await())
        handleForecastResult(forecastDeferred.await())
    }

    private fun handleWeatherResult(result: Result<Weather>) {
        _weatherState.update {
            when (result) {
                is Result.Success -> WeatherUiState.Success(result.data)
                is Result.Error -> WeatherUiState.Error(result.error.toUiMessage())
            }
        }
    }

    private fun handleForecastResult(result: Result<List<ForecastDay>>) {
        _forecastState.update {
            when (result) {
                is Result.Success -> ForecastUiState.Success(result.data)
                is Result.Error -> ForecastUiState.Error
            }
        }
    }
}
