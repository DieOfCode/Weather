package com.example.weather.weather.data.local.room

import androidx.room.withTransaction
import com.example.weather.weather.data.local.WeatherLocalDataSource
import com.example.weather.weather.data.local.room.entity.SelectedCityEntity
import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.model.WeatherForecast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class RoomWeatherLocalDataSource(
    private val database: WeatherRoomDatabase,
) : WeatherLocalDataSource {

    private val weatherDao: WeatherDao = database.weatherDao()

    override fun observeWeather(cityId: String): Flow<WeatherForecast?> {
        require(cityId.isNotBlank()) {
            "cityId must not be blank"
        }

        return weatherDao.observeWeather(cityId)
            .map { storedForecast ->
                storedForecast?.let(RoomWeatherMapper::forecastToDomain)
            }
            .distinctUntilChanged()
    }

    override fun observeCities(): Flow<List<City>> = weatherDao
        .observeCities()
        .map { cityEntities ->
            cityEntities.map(RoomWeatherMapper::cityToDomain)
        }
        .distinctUntilChanged()

    override fun observeSelectedCity(): Flow<City?> = weatherDao
        .observeSelectedCity(SelectedCityEntity.SINGLETON_ID)
        .map { cityEntity ->
            cityEntity?.let(RoomWeatherMapper::cityToDomain)
        }
        .distinctUntilChanged()

    override suspend fun saveForecast(forecast: WeatherForecast) {
        val cityId = forecast.city.id
        require(cityId.isNotBlank()) {
            "forecast.city.id must not be blank"
        }

        val entities = RoomWeatherMapper.forecastToEntities(forecast)

        database.withTransaction {
            weatherDao.upsertCity(entities.city)
            weatherDao.upsertCurrentWeather(entities.current)

            weatherDao.deleteHourlyWeather(cityId)
            if (entities.hourly.isNotEmpty()) {
                weatherDao.upsertHourlyWeather(entities.hourly)
            }

            weatherDao.deleteDailyWeather(cityId)
            if (entities.daily.isNotEmpty()) {
                weatherDao.upsertDailyWeather(entities.daily)
            }
        }
    }

    override suspend fun deleteForecast(cityId: String) {
        require(cityId.isNotBlank()) {
            "cityId must not be blank"
        }

        database.withTransaction {
            weatherDao.deleteCurrentWeather(cityId)
            weatherDao.deleteHourlyWeather(cityId)
            weatherDao.deleteDailyWeather(cityId)
        }
    }

    override suspend fun deleteCity(cityId: String) {
        require(cityId.isNotBlank()) {
            "cityId must not be blank"
        }

        weatherDao.deleteCity(cityId)
    }

    override suspend fun clear() {
        weatherDao.deleteAllCities()
    }

    override suspend fun clearSelectedCity() {
        weatherDao.clearSelectedCity()
    }

    override suspend fun selectCity(cityId: String) {
        require(cityId.isNotBlank()) {
            "cityId must not be blank"
        }

        weatherDao.upsertSelectedCity(
            SelectedCityEntity(cityId = cityId),
        )
    }
}
