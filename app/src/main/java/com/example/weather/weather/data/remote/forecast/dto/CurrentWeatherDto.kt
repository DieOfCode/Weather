package com.example.weather.weather.data.remote.forecast.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CurrentWeatherDto(
    val time: String? = null,
    @SerialName("temperature_2m")
    val temperatureCelsius: Double? = null,
    @SerialName("apparent_temperature")
    val apparentTemperatureCelsius: Double? = null,
    @SerialName("relative_humidity_2m")
    val relativeHumidityPercent: Int? = null,
    @SerialName("wind_speed_10m")
    val windSpeedKmh: Double? = null,
    @SerialName("weather_code")
    val weatherCode: Int? = null,
)
