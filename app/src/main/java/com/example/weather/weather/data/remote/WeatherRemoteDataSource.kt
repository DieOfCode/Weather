package com.example.weather.weather.data.remote

import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.model.WeatherForecast

interface WeatherRemoteDataSource {

    suspend fun fetchForecast(city: City): WeatherForecast
}
