package com.example.weather.weather.data.local.sqlite

import android.database.sqlite.SQLiteDatabase
import androidx.core.database.sqlite.transaction
import com.example.weather.weather.data.local.WeatherLocalDataSource
import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.model.DailyWeather
import com.example.weather.weather.domain.model.HourlyWeather
import com.example.weather.weather.domain.model.WeatherForecast
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext


private data class InvalidationState(
    val globalVersion: Long = 0,
    val cityVersions: Map<String, Long> = emptyMap(),
    val citiesVersion: Long = 0,
)

class SqliteWeatherLocalDataSource(
    private val databaseHelper: DatabaseHelper, private val ioDispatcher: CoroutineDispatcher
) : WeatherLocalDataSource {

    private val invalidationState = MutableStateFlow(InvalidationState())
    private val databaseMutex = Mutex()

    companion object {
        private val CITY_PROJECTION = arrayOf(
            WeatherSqlContract.Cities.COLUMN_ID,
            WeatherSqlContract.Cities.COLUMN_NAME,
            WeatherSqlContract.Cities.COLUMN_COUNTRY_CODE,
            WeatherSqlContract.Cities.COLUMN_LATITUDE,
            WeatherSqlContract.Cities.COLUMN_LONGITUDE,
            WeatherSqlContract.Cities.COLUMN_TIME_ZONE_ID,
        )

        private val CURRENT_WEATHER_PROJECTION = arrayOf(
            WeatherSqlContract.CurrentWeather.COLUMN_OBSERVED_AT_EPOCH_MILLIS,
            WeatherSqlContract.CurrentWeather.COLUMN_TEMPERATURE_CELSIUS,
            WeatherSqlContract.CurrentWeather.COLUMN_APPARENT_TEMPERATURE_CELSIUS,
            WeatherSqlContract.CurrentWeather.COLUMN_RELATIVE_HUMIDITY_PERCENT,
            WeatherSqlContract.CurrentWeather.COLUMN_WIND_SPEED_KMH,
            WeatherSqlContract.CurrentWeather.COLUMN_WEATHER_CODE,
            WeatherSqlContract.CurrentWeather.COLUMN_UPDATED_AT_EPOCH_MILLIS,
        )

        private val HOURLY_WEATHER_PROJECTION = arrayOf(
            WeatherSqlContract.HourlyWeather.COLUMN_FORECAST_AT_EPOCH_MILLIS,
            WeatherSqlContract.HourlyWeather.COLUMN_TEMPERATURE_CELSIUS,
            WeatherSqlContract.HourlyWeather.COLUMN_APPARENT_TEMPERATURE_CELSIUS,
            WeatherSqlContract.HourlyWeather.COLUMN_PRECIPITATION_PROBABILITY_PERCENT,
            WeatherSqlContract.HourlyWeather.COLUMN_RELATIVE_HUMIDITY_PERCENT,
            WeatherSqlContract.HourlyWeather.COLUMN_WIND_SPEED_KMH,
            WeatherSqlContract.HourlyWeather.COLUMN_WEATHER_CODE,
        )

        private val DAILY_WEATHER_PROJECTION = arrayOf(
            WeatherSqlContract.DailyWeather.COLUMN_FORECAST_DATE,
            WeatherSqlContract.DailyWeather.COLUMN_MINIMUM_TEMPERATURE_CELSIUS,
            WeatherSqlContract.DailyWeather.COLUMN_MAXIMUM_TEMPERATURE_CELSIUS,
            WeatherSqlContract.DailyWeather.COLUMN_PRECIPITATION_PROBABILITY_PERCENT,
            WeatherSqlContract.DailyWeather.COLUMN_WEATHER_CODE,
        )
    }

    override fun observeWeather(cityId: String): Flow<WeatherForecast?> {
        require(cityId.isNotBlank()) {
            "cityId must not be blank"
        }
        return invalidationState.map { state ->
            state.globalVersion to (state.cityVersions[cityId] ?: 0L)
        }.distinctUntilChanged().mapLatest {
            withContext(ioDispatcher) {
                databaseMutex.withLock {
                    val database = databaseHelper.readableDatabase
                    readForecast(database, cityId)
                }
            }
        }.distinctUntilChanged()


    }


    override fun observeCities(): Flow<List<City>> = invalidationState
        .map { state ->
            state.globalVersion to state.citiesVersion
        }
        .distinctUntilChanged()
        .mapLatest {
            withContext(ioDispatcher) {
                databaseMutex.withLock {
                    readCities(databaseHelper.readableDatabase)
                }
            }
        }
        .distinctUntilChanged()

    override suspend fun saveForecast(forecast: WeatherForecast) {
        val cityId = forecast.city.id
        require(cityId.isNotBlank()) {
            "forecast.city.id must not be blank"
        }

        withContext(ioDispatcher) {
            databaseMutex.withLock {
                val database = databaseHelper.writableDatabase

                database.transaction {
                    upsertCity(this, forecast.city)
                    upsertCurrentWeather(this, forecast)
                    replaceHourlyWeather(this, cityId, forecast.hourly)
                    replaceDailyWeather(this, cityId, forecast.daily)
                }

                invalidateCity(cityId, citiesChanged = true)
            }
        }
    }

    override suspend fun deleteForecast(cityId: String) {
        require(cityId.isNotBlank()) {
            "cityId must not be blank"
        }
        withContext(
            ioDispatcher
        ) {
            databaseMutex.withLock {
                val database = databaseHelper.writableDatabase
                database.transaction {
                    database.delete(
                        WeatherSqlContract.CurrentWeather.TABLE_NAME,
                        "${WeatherSqlContract.CurrentWeather.COLUMN_CITY_ID} = ?",
                        arrayOf(cityId),
                    )
                    database.delete(
                        WeatherSqlContract.HourlyWeather.TABLE_NAME,
                        "${WeatherSqlContract.HourlyWeather.COLUMN_CITY_ID} = ?",
                        arrayOf(cityId),
                    )
                    database.delete(
                        WeatherSqlContract.DailyWeather.TABLE_NAME,
                        "${WeatherSqlContract.DailyWeather.COLUMN_CITY_ID} = ?",
                        arrayOf(cityId),
                    )
                }
                invalidateCity(cityId, citiesChanged = false)
            }
        }
    }

    override suspend fun deleteCity(cityId: String) {
        require(cityId.isNotBlank()) {
            "cityId must not be blank"
        }
        withContext(
            ioDispatcher
        ) {
            databaseMutex.withLock {
                val database = databaseHelper.writableDatabase
                database.transaction {
                    database.delete(
                        WeatherSqlContract.Cities.TABLE_NAME,
                        "${WeatherSqlContract.Cities.COLUMN_ID} = ?",
                        arrayOf(cityId),
                    )
                }
                invalidateCity(cityId, citiesChanged = true)
            }
        }
    }

    override suspend fun clear() {
        withContext(ioDispatcher) {
            databaseMutex.withLock {
                val database = databaseHelper.writableDatabase
                database.delete(
                    WeatherSqlContract.Cities.TABLE_NAME,
                    null,
                    null,
                )
                invalidateAll()
            }
        }
    }

    private fun readCity(database: SQLiteDatabase, cityId: String): City? = database.query(
        WeatherSqlContract.Cities.TABLE_NAME,
        CITY_PROJECTION,
        "${WeatherSqlContract.Cities.COLUMN_ID} = ?",
        arrayOf(cityId),
        null,
        null,
        null,
        "1",
    ).use { cursor ->
        if (!cursor.moveToFirst()) {
            return@use null
        }

        CityCursorReader(cursor).readCurrentRow()
    }

    private fun readCities(database: SQLiteDatabase): List<City> {
        val cities = mutableListOf<City>()
        database.query(
            WeatherSqlContract.Cities.TABLE_NAME,
            CITY_PROJECTION,
            null,
            null,
            null,
            null,
            "${WeatherSqlContract.Cities.COLUMN_NAME} COLLATE NOCASE ASC",
            null,
        ).use { cursor ->
            val reader = CityCursorReader(cursor)
            while (cursor.moveToNext()) {
                cities.add(reader.readCurrentRow())
            }
        }
        return cities
    }

    private fun readCurrentWeather(
        database: SQLiteDatabase,
        cityId: String,
    ): CurrentWeatherRecord? = database.query(
        WeatherSqlContract.CurrentWeather.TABLE_NAME,
        CURRENT_WEATHER_PROJECTION,
        "${WeatherSqlContract.CurrentWeather.COLUMN_CITY_ID} = ?",
        arrayOf(cityId),
        null,
        null,
        null,
        "1",
    ).use { cursor ->
        if (!cursor.moveToFirst()) {
            return@use null
        }

        CurrentWeatherCursorReader(cursor).readCurrentRow()
    }

    private fun readHourlyWeather(
        database: SQLiteDatabase,
        cityId: String,
    ): List<HourlyWeather> {
        val weatherList = mutableListOf<HourlyWeather>()
        database.query(
            WeatherSqlContract.HourlyWeather.TABLE_NAME,
            HOURLY_WEATHER_PROJECTION,
            "${WeatherSqlContract.HourlyWeather.COLUMN_CITY_ID} = ?",
            arrayOf(cityId),
            null,
            null,
            "${WeatherSqlContract.HourlyWeather.COLUMN_FORECAST_AT_EPOCH_MILLIS} ASC",
            null,
        ).use { cursor ->
            val reader = HourlyWeatherCursorReader(cursor)
            while (cursor.moveToNext()) {
                weatherList.add(reader.readCurrentRow())
            }
        }
        return weatherList
    }

    private fun readDailyWeather(
        database: SQLiteDatabase,
        cityId: String,
    ): List<DailyWeather> {
        val weatherList = mutableListOf<DailyWeather>()
        database.query(
            WeatherSqlContract.DailyWeather.TABLE_NAME,
            DAILY_WEATHER_PROJECTION,
            "${WeatherSqlContract.DailyWeather.COLUMN_CITY_ID} = ?",
            arrayOf(cityId),
            null,
            null,
            "${WeatherSqlContract.DailyWeather.COLUMN_FORECAST_DATE} ASC",
            null,
        ).use { cursor ->
            val reader = DailyWeatherCursorReader(cursor)
            while (cursor.moveToNext()) {
                weatherList.add(reader.readCurrentRow())
            }
        }
        return weatherList
    }

    private fun readForecast(
        database: SQLiteDatabase,
        cityId: String,
    ): WeatherForecast? {
        val city = readCity(database, cityId) ?: return null
        val currentWeatherRecord = readCurrentWeather(database, cityId) ?: return null

        return WeatherForecast(
            city = city,
            current = currentWeatherRecord.weather,
            hourly = readHourlyWeather(database, cityId),
            daily = readDailyWeather(database, cityId),
            updatedAt = currentWeatherRecord.updatedAt,
        )
    }

    private fun upsertCity(
        database: SQLiteDatabase,
        city: City,
    ) {
        val values = SqliteWeatherMapper.cityToContentValues(city)
        val updatedRows = database.update(
            WeatherSqlContract.Cities.TABLE_NAME,
            values,
            "${WeatherSqlContract.Cities.COLUMN_ID} = ?",
            arrayOf(city.id),
        )

        if (updatedRows == 0) {
            database.insertOrThrow(
                WeatherSqlContract.Cities.TABLE_NAME,
                null,
                values,
            )
        }
    }

    private fun upsertCurrentWeather(
        database: SQLiteDatabase,
        forecast: WeatherForecast,
    ) {
        val cityId = forecast.city.id
        val values = SqliteWeatherMapper.currentWeatherToContentValues(
            cityId = cityId,
            weather = forecast.current,
            updatedAt = forecast.updatedAt,
        )
        val updatedRows = database.update(
            WeatherSqlContract.CurrentWeather.TABLE_NAME,
            values,
            "${WeatherSqlContract.CurrentWeather.COLUMN_CITY_ID} = ?",
            arrayOf(cityId),
        )

        if (updatedRows == 0) {
            database.insertOrThrow(
                WeatherSqlContract.CurrentWeather.TABLE_NAME,
                null,
                values,
            )
        }
    }

    private fun replaceHourlyWeather(
        database: SQLiteDatabase,
        cityId: String,
        hourlyWeather: List<HourlyWeather>,
    ) {
        database.delete(
            WeatherSqlContract.HourlyWeather.TABLE_NAME,
            "${WeatherSqlContract.HourlyWeather.COLUMN_CITY_ID} = ?",
            arrayOf(cityId),
        )

        hourlyWeather.forEach { weather ->
            database.insertOrThrow(
                WeatherSqlContract.HourlyWeather.TABLE_NAME,
                null,
                SqliteWeatherMapper.hourlyWeatherToContentValues(cityId, weather),
            )
        }
    }

    private fun replaceDailyWeather(
        database: SQLiteDatabase,
        cityId: String,
        dailyWeather: List<DailyWeather>,
    ) {
        database.delete(
            WeatherSqlContract.DailyWeather.TABLE_NAME,
            "${WeatherSqlContract.DailyWeather.COLUMN_CITY_ID} = ?",
            arrayOf(cityId),
        )

        dailyWeather.forEach { weather ->
            database.insertOrThrow(
                WeatherSqlContract.DailyWeather.TABLE_NAME,
                null,
                SqliteWeatherMapper.dailyWeatherToContentValues(cityId, weather),
            )
        }
    }

    private fun invalidateCity(
        cityId: String,
        citiesChanged: Boolean,
    ) {
        invalidationState.update { state ->
            val nextVersion = (state.cityVersions[cityId] ?: 0L) + 1L
            state.copy(
                cityVersions = state.cityVersions + (cityId to nextVersion),
                citiesVersion = if (citiesChanged) {
                    state.citiesVersion + 1L
                } else {
                    state.citiesVersion
                },
            )
        }
    }

    private fun invalidateAll() {
        invalidationState.update { state ->
            state.copy(
                globalVersion = state.globalVersion + 1L,
                cityVersions = emptyMap(),
                citiesVersion = state.citiesVersion + 1L,
            )
        }
    }
}
