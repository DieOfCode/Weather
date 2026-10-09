package com.example.weather.weather.data.remote.forecast.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class HourlyWeatherDto(
    val time: List<String?> = emptyList(),
    @SerialName("temperature_2m")
    val temperatureCelsius: List<Double?> = emptyList(),
    @SerialName("apparent_temperature")
    val apparentTemperatureCelsius: List<Double?> = emptyList(),
    @SerialName("precipitation_probability")
    val precipitationProbabilityPercent: List<Int?> = emptyList(),
    @SerialName("relative_humidity_2m")
    val relativeHumidityPercent: List<Int?> = emptyList(),
    @SerialName("wind_speed_10m")
    val windSpeedKmh: List<Double?> = emptyList(),
    @SerialName("weather_code")
    val weatherCode: List<Int?> = emptyList(),
)
