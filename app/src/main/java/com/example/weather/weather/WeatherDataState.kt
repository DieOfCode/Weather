package com.example.weather.weather

import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.model.WeatherForecast

internal sealed interface WeatherDataState {

    data object Loading : WeatherDataState

    data object NoCity : WeatherDataState

    data class CityWeather(
        val city: City,
        val forecast: WeatherForecast?,
    ) : WeatherDataState

    data class Failed(
        val cause: Throwable,
    ) : WeatherDataState
}
