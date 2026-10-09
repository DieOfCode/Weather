package com.example.weather.weather.data.local

import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.model.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface WeatherLocalDataSource {

    fun observeWeather(cityId: String): Flow<WeatherForecast?>

    fun observeCities(): Flow<List<City>>

    fun observeSelectedCity(): Flow<City?>

    suspend fun saveForecast(forecast: WeatherForecast)

    suspend fun deleteForecast(cityId: String)

    suspend fun deleteCity(cityId: String)

    suspend fun clear()

    suspend fun clearSelectedCity()

    suspend fun selectCity(cityId: String)
}
