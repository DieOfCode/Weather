package com.example.weather.weather.domain.model

import java.time.Instant
import java.time.LocalDate

data class WeatherForecast(
    val city: City,
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>,
    val updatedAt: Instant,
)

data class City(
    val id: String,
    val name: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
)

data class CurrentWeather(
    val observedAt: Instant,
    val temperatureCelsius: Double,
    val apparentTemperatureCelsius: Double,
    val relativeHumidityPercent: Int,
    val windSpeedKmh: Double,
    val weatherCode: Int,
)

data class HourlyWeather(
    val forecastAt: Instant,
    val temperatureCelsius: Double,
    val apparentTemperatureCelsius: Double,
    val precipitationProbabilityPercent: Int,
    val relativeHumidityPercent: Int,
    val windSpeedKmh: Double,
    val weatherCode: Int,
)

data class DailyWeather(
    val date: LocalDate,
    val minimumTemperatureCelsius: Double,
    val maximumTemperatureCelsius: Double,
    val precipitationProbabilityPercent: Int,
    val weatherCode: Int,
)
