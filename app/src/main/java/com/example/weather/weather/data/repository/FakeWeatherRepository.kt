package com.example.weather.weather.data.repository

import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.model.WeatherForecast
import com.example.weather.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow

class FakeWeatherRepository: WeatherRepository {

    override fun observeWeather(city: City): Flow<WeatherForecast?> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshWeather(city: City) {
        TODO("Not yet implemented")
    }
}