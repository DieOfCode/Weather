package com.example.weather.weather.data.remote

import com.example.weather.weather.data.remote.forecast.OpenMeteoWeatherApi
import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.model.WeatherForecast
import java.time.Clock

internal class OpenMeteoWeatherRemoteDataSource(
    private val weatherApi: OpenMeteoWeatherApi,
    private val clock: Clock = Clock.systemUTC(),
) : WeatherRemoteDataSource {

    override suspend fun fetchForecast(city: City): WeatherForecast = executeRemoteRequest {
        val response = weatherApi.getForecast(
            latitude = city.latitude,
            longitude = city.longitude,
            timezone = city.timeZoneId,
        )

        RemoteWeatherMapper.forecastToDomain(
            response = response,
            city = city,
            updatedAt = clock.instant(),
        )
    }
}
