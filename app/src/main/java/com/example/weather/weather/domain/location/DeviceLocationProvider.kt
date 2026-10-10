package com.example.weather.weather.domain.location

interface DeviceLocationProvider {
    suspend fun getCurrentLocation(): DeviceLocation?
}
