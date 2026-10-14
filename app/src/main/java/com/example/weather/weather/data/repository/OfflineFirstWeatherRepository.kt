package com.example.weather.weather.data.repository

import com.example.weather.weather.data.local.WeatherLocalDataSource
import com.example.weather.weather.data.remote.CityRemoteDataSource
import com.example.weather.weather.data.remote.RemoteDataException
import com.example.weather.weather.data.remote.WeatherRemoteDataSource
import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.model.WeatherForecast
import com.example.weather.weather.domain.repository.WeatherRepository
import com.example.weather.weather.domain.repository.WeatherRepositoryException
import kotlinx.coroutines.flow.Flow

internal class OfflineFirstWeatherRepository(
    private val weatherLocalDataSource: WeatherLocalDataSource,
    private val weatherRemoteDataSource: WeatherRemoteDataSource,
    private val cityRemoteDataSource: CityRemoteDataSource,
) : WeatherRepository {

    override fun observeWeather(cityId: String): Flow<WeatherForecast?> =
        weatherLocalDataSource.observeWeather(cityId)

    override fun observeSavedCities(): Flow<List<City>> =
        weatherLocalDataSource.observeCities()

    override fun observeSelectedCity(): Flow<City?> =
        weatherLocalDataSource.observeSelectedCity()

    override suspend fun searchCities(query: String): List<City> = try {
        cityRemoteDataSource.searchCities(query)
    } catch (exception: RemoteDataException) {
        throw exception.toRepositoryException()
    }

    override suspend fun refreshWeather(city: City) {
        try {
            val forecast = weatherRemoteDataSource.fetchForecast(city)
            weatherLocalDataSource.saveForecast(forecast)
        } catch (exception: RemoteDataException) {
            throw exception.toRepositoryException()
        }
    }

    override suspend fun deleteCity(cityId: String) =
        weatherLocalDataSource.deleteCity(cityId)

    override suspend fun selectCity(cityId: String) =
        weatherLocalDataSource.selectCity(cityId)

    override suspend fun clearSelectedCity() =
        weatherLocalDataSource.clearSelectedCity()

    private fun RemoteDataException.toRepositoryException(): WeatherRepositoryException =
        when (this) {
            is RemoteDataException.Network -> WeatherRepositoryException.Network(
                cause = this,
            )

            is RemoteDataException.Http -> WeatherRepositoryException.Service(
                statusCode = statusCode,
                cause = this,
            )

            is RemoteDataException.Serialization,
            is RemoteDataException.InvalidResponse -> WeatherRepositoryException.InvalidData(
                cause = this,
            )
        }
}
