package com.example.weather.weather.data.remote.forecast.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DailyWeatherDto(
    val time: List<String?> = emptyList(),
    @SerialName("temperature_2m_min")
    val minimumTemperatureCelsius: List<Double?> = emptyList(),
    @SerialName("temperature_2m_max")
    val maximumTemperatureCelsius: List<Double?> = emptyList(),
    @SerialName("precipitation_probability_max")
    val precipitationProbabilityPercent: List<Int?> = emptyList(),
    @SerialName("weather_code")
    val weatherCode: List<Int?> = emptyList(),
)
