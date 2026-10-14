package com.example.weather.weather.domain.usecase

import com.example.weather.weather.domain.location.DeviceLocationException
import com.example.weather.weather.domain.location.DeviceLocationProvider
import com.example.weather.weather.domain.location.LocationCityResolver
import com.example.weather.weather.domain.repository.WeatherRepository

class LoadWeatherForCurrentLocationUseCase(
    private val deviceLocationProvider: DeviceLocationProvider,
    private val locationCityResolver: LocationCityResolver,
    private val weatherRepository: WeatherRepository,
) {

    suspend operator fun invoke() {
        val location = deviceLocationProvider.getCurrentLocation()
            ?: throw DeviceLocationException.LocationUnavailable()

        val city = locationCityResolver.resolve(location = location)
        weatherRepository.refreshWeather(city)
        weatherRepository.selectCity(city.id)
    }
}
