package com.example.weather.weather.domain.repository

import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.model.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface WeatherRepository{
    fun observeWeather(city: City): Flow<WeatherForecast?>

    suspend fun refreshWeather(city: City)
}


