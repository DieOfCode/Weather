package com.example.weather.weather.domain.location

import com.example.weather.weather.domain.model.City

interface LocationCityResolver {
    suspend fun resolve(location: DeviceLocation): City
}
