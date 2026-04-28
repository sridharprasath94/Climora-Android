package com.flash.climora.data.remote.dto

import com.flash.climora.domain.model.ForecastDay
import com.google.gson.annotations.SerializedName

data class ForecastResponseDto(
    val location: LocationDto,
    val current: CurrentDto,
    val forecast: ForecastDto
)

data class ForecastDto(
    val forecastday: List<ForecastDayDto>
)

data class ForecastDayDto(
    val date: String,
    val day: DayDto
)

data class DayDto(
    @SerializedName("mintemp_c") val minTempC: Double,
    @SerializedName("maxtemp_c") val maxTempC: Double,
    val condition: ConditionDto
)

fun ForecastResponseDto.toDomainForecast(): List<ForecastDay> =
    forecast.forecastday.map { day ->
        ForecastDay(
            date = day.date,
            minTemp = day.day.minTempC,
            maxTemp = day.day.maxTempC,
            conditionCode = day.day.condition.code,
            conditionText = day.day.condition.text
        )
    }

fun ForecastResponseDto.toDomainWeather() = WeatherDto(
    location = location,
    current = current
).toDomain()
