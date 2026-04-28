package com.flash.climora.data.remote

import com.flash.climora.data.remote.dto.ForecastResponseDto
import com.flash.climora.data.remote.dto.WeatherDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("current.json")
    suspend fun getWeather(
        @Query("key") key: String,
        @Query("q") query: String
    ): WeatherDto

    @GET("forecast.json")
    suspend fun getForecast(
        @Query("key") key: String,
        @Query("q") query: String,
        @Query("days") days: Int
    ): ForecastResponseDto
}
