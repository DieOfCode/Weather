package com.example.weather.weather.data.local.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.weather.weather.data.local.room.entity.CityEntity
import com.example.weather.weather.data.local.room.entity.CurrentWeatherEntity
import com.example.weather.weather.data.local.room.entity.DailyWeatherEntity
import com.example.weather.weather.data.local.room.entity.HourlyWeatherEntity
import com.example.weather.weather.data.local.room.entity.SelectedCityEntity
import com.example.weather.weather.data.local.room.relation.WeatherWithForecasts
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Transaction
    @Query("SELECT * FROM cities WHERE id = :cityId LIMIT 1")
    fun observeWeather(cityId: String): Flow<WeatherWithForecasts?>

    @Query("SELECT * FROM cities ORDER BY name COLLATE NOCASE ASC")
    fun observeCities(): Flow<List<CityEntity>>

    @Query(
        """
        SELECT cities.*
        FROM selected_city
        INNER JOIN cities ON selected_city.city_id = cities.id
        WHERE selected_city.singleton_id = :singletonId
        LIMIT 1
        """,
    )
    fun observeSelectedCity(singletonId: Int): Flow<CityEntity?>

    @Upsert
    suspend fun upsertCity(city: CityEntity)

    @Upsert
    suspend fun upsertSelectedCity(selectedCity: SelectedCityEntity)

    @Upsert
    suspend fun upsertCurrentWeather(currentWeather: CurrentWeatherEntity)

    @Upsert
    suspend fun upsertHourlyWeather(hourlyWeather: List<HourlyWeatherEntity>)

    @Upsert
    suspend fun upsertDailyWeather(dailyWeather: List<DailyWeatherEntity>)

    @Query("DELETE FROM current_weather WHERE city_id = :cityId")
    suspend fun deleteCurrentWeather(cityId: String)

    @Query("DELETE FROM hourly_weather WHERE city_id = :cityId")
    suspend fun deleteHourlyWeather(cityId: String)

    @Query("DELETE FROM daily_weather WHERE city_id = :cityId")
    suspend fun deleteDailyWeather(cityId: String)

    @Query("DELETE FROM cities WHERE id = :cityId")
    suspend fun deleteCity(cityId: String)

    @Query("DELETE FROM cities")
    suspend fun deleteAllCities()

    @Query("DELETE FROM selected_city")
    suspend fun clearSelectedCity()
}
