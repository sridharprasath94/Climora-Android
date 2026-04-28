package com.flash.climora.data.repository

import com.flash.climora.BuildConfig
import com.flash.climora.core.Result
import com.flash.climora.data.remote.WeatherApi
import com.flash.climora.data.remote.dto.toDomain
import com.flash.climora.data.remote.error.NetworkErrorMapper
import com.flash.climora.data.remote.error.NetworkErrorMapper.toDomain
import com.flash.climora.domain.model.Weather
import com.flash.climora.domain.repository.WeatherRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi
) : WeatherRepository {

    override suspend fun getWeather(city: String): Result<Weather> {
        return try {
            val dto = api.getWeather(BuildConfig.API_KEY, city)
            Result.Success(dto.toDomain())
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            Result.Error(NetworkErrorMapper.fromThrowable(t).toDomain())
        }
    }

    override suspend fun getWeatherByCoordinates(
        lat: Double,
        lon: Double
    ): Result<Weather> {
        return try {
            val latLong = "$lat,$lon"
            val dto = api.getWeatherByCoordinates(BuildConfig.API_KEY, latLong)
            Result.Success(dto.toDomain())
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            Result.Error(NetworkErrorMapper.fromThrowable(t).toDomain())
        }
    }
}
