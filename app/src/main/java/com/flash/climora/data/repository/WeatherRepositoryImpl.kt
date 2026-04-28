package com.flash.climora.data.repository

import com.flash.climora.BuildConfig
import com.flash.climora.core.Result
import com.flash.climora.data.remote.WeatherApi
import com.flash.climora.data.remote.dto.toDomain
import com.flash.climora.data.remote.dto.toDomainForecast
import com.flash.climora.data.remote.dto.toDomainWeather
import com.flash.climora.data.remote.error.NetworkErrorMapper
import com.flash.climora.data.remote.error.NetworkErrorMapper.toDomain
import com.flash.climora.domain.model.ForecastDay
import com.flash.climora.domain.model.Weather
import com.flash.climora.domain.repository.WeatherRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi
) : WeatherRepository {

    override suspend fun getWeather(city: String): Result<Weather> = runCatching(
        apiCall = { api.getWeather(BuildConfig.API_KEY, city).toDomain() }
    )

    override suspend fun getWeatherByCoordinates(lat: Double, lon: Double): Result<Weather> = runCatching(
        apiCall = { api.getWeather(BuildConfig.API_KEY, "$lat,$lon").toDomain() }
    )

    override suspend fun getForecast(city: String, days: Int): Result<List<ForecastDay>> = runCatching(
        apiCall = { api.getForecast(BuildConfig.API_KEY, city, days).toDomainForecast() }
    )

    override suspend fun getForecastByCoordinates(lat: Double, lon: Double, days: Int): Result<List<ForecastDay>> = runCatching(
        apiCall = { api.getForecast(BuildConfig.API_KEY, "$lat,$lon", days).toDomainForecast() }
    )

    private suspend fun <T> runCatching(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.Success(apiCall())
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            Result.Error(NetworkErrorMapper.fromThrowable(t).toDomain())
        }
    }
}
