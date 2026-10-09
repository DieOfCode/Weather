package com.example.weather.weather.domain.repository

import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.model.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun observeWeather(cityId: String): Flow<WeatherForecast?>

    fun observeSavedCities(): Flow<List<City>>

    fun observeSelectedCity(): Flow<City?>

    suspend fun searchCities(query: String): List<City>

    suspend fun refreshWeather(city: City)

    suspend fun deleteCity(cityId: String)

    suspend fun selectCity(cityId: String)

    suspend fun clearSelectedCity()
}


