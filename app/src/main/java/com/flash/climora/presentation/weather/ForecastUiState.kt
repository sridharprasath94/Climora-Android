package com.flash.climora.presentation.weather

import com.flash.climora.domain.model.ForecastDay

sealed class ForecastUiState {
    object Idle : ForecastUiState()
    object Loading : ForecastUiState()
    data class Success(val days: List<ForecastDay>) : ForecastUiState()
    object Error : ForecastUiState()
}
