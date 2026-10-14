package com.example.weather.weather.data.remote

import com.example.weather.weather.domain.model.City

interface CityRemoteDataSource {

    suspend fun searchCities(query: String): List<City>
}
