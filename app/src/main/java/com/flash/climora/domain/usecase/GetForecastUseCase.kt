package com.flash.climora.domain.usecase

import com.flash.climora.core.Result
import com.flash.climora.domain.model.ForecastDay
import com.flash.climora.domain.repository.WeatherRepository
import javax.inject.Inject

class GetForecastUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(city: String, days: Int = 3): Result<List<ForecastDay>> =
        repository.getForecast(city, days)

    suspend operator fun invoke(lat: Double, lon: Double, days: Int = 3): Result<List<ForecastDay>> =
        repository.getForecastByCoordinates(lat, lon, days)
}
