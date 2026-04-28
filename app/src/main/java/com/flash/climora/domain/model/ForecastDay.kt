package com.flash.climora.domain.model

data class ForecastDay(
    val date: String,
    val minTemp: Double,
    val maxTemp: Double,
    val conditionCode: Int,
    val conditionText: String
)
