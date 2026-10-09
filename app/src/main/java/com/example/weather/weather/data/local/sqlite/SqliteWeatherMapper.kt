package com.example.weather.weather.data.local.sqlite

import android.content.ContentValues
import android.database.Cursor
import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.model.CurrentWeather
import com.example.weather.weather.domain.model.DailyWeather
import com.example.weather.weather.domain.model.HourlyWeather
import java.time.Instant
import java.time.LocalDate

internal object SqliteWeatherMapper {

    fun cityToContentValues(city: City): ContentValues = ContentValues(6).apply {
        put(WeatherSqlContract.Cities.COLUMN_ID, city.id)
        put(WeatherSqlContract.Cities.COLUMN_NAME, city.name)
        put(WeatherSqlContract.Cities.COLUMN_COUNTRY_CODE, city.countryCode)
        put(WeatherSqlContract.Cities.COLUMN_LATITUDE, city.latitude)
        put(WeatherSqlContract.Cities.COLUMN_LONGITUDE, city.longitude)
        put(WeatherSqlContract.Cities.COLUMN_TIME_ZONE_ID, city.timeZoneId)
    }

    fun currentWeatherToContentValues(
        cityId: String,
        weather: CurrentWeather,
        updatedAt: Instant,
    ): ContentValues = ContentValues(8).apply {
        put(WeatherSqlContract.CurrentWeather.COLUMN_CITY_ID, cityId)
        put(
            WeatherSqlContract.CurrentWeather.COLUMN_OBSERVED_AT_EPOCH_MILLIS,
            weather.observedAt.toEpochMilli(),
        )
        put(
            WeatherSqlContract.CurrentWeather.COLUMN_TEMPERATURE_CELSIUS,
            weather.temperatureCelsius,
        )
        put(
            WeatherSqlContract.CurrentWeather.COLUMN_APPARENT_TEMPERATURE_CELSIUS,
            weather.apparentTemperatureCelsius,
        )
        put(
            WeatherSqlContract.CurrentWeather.COLUMN_RELATIVE_HUMIDITY_PERCENT,
            weather.relativeHumidityPercent,
        )
        put(
            WeatherSqlContract.CurrentWeather.COLUMN_WIND_SPEED_KMH,
            weather.windSpeedKmh,
        )
        put(
            WeatherSqlContract.CurrentWeather.COLUMN_WEATHER_CODE,
            weather.weatherCode,
        )
        put(
            WeatherSqlContract.CurrentWeather.COLUMN_UPDATED_AT_EPOCH_MILLIS,
            updatedAt.toEpochMilli(),
        )
    }

    fun hourlyWeatherToContentValues(
        cityId: String,
        weather: HourlyWeather,
    ): ContentValues = ContentValues(8).apply {
        put(WeatherSqlContract.HourlyWeather.COLUMN_CITY_ID, cityId)
        put(
            WeatherSqlContract.HourlyWeather.COLUMN_FORECAST_AT_EPOCH_MILLIS,
            weather.forecastAt.toEpochMilli(),
        )
        put(
            WeatherSqlContract.HourlyWeather.COLUMN_TEMPERATURE_CELSIUS,
            weather.temperatureCelsius,
        )
        put(
            WeatherSqlContract.HourlyWeather.COLUMN_APPARENT_TEMPERATURE_CELSIUS,
            weather.apparentTemperatureCelsius,
        )
        put(
            WeatherSqlContract.HourlyWeather.COLUMN_PRECIPITATION_PROBABILITY_PERCENT,
            weather.precipitationProbabilityPercent,
        )
        put(
            WeatherSqlContract.HourlyWeather.COLUMN_RELATIVE_HUMIDITY_PERCENT,
            weather.relativeHumidityPercent,
        )
        put(
            WeatherSqlContract.HourlyWeather.COLUMN_WIND_SPEED_KMH,
            weather.windSpeedKmh,
        )
        put(
            WeatherSqlContract.HourlyWeather.COLUMN_WEATHER_CODE,
            weather.weatherCode,
        )
    }

    fun dailyWeatherToContentValues(
        cityId: String,
        weather: DailyWeather,
    ): ContentValues = ContentValues(6).apply {
        put(WeatherSqlContract.DailyWeather.COLUMN_CITY_ID, cityId)
        put(
            WeatherSqlContract.DailyWeather.COLUMN_FORECAST_DATE,
            weather.date.toString(),
        )
        put(
            WeatherSqlContract.DailyWeather.COLUMN_MINIMUM_TEMPERATURE_CELSIUS,
            weather.minimumTemperatureCelsius,
        )
        put(
            WeatherSqlContract.DailyWeather.COLUMN_MAXIMUM_TEMPERATURE_CELSIUS,
            weather.maximumTemperatureCelsius,
        )
        put(
            WeatherSqlContract.DailyWeather.COLUMN_PRECIPITATION_PROBABILITY_PERCENT,
            weather.precipitationProbabilityPercent,
        )
        put(
            WeatherSqlContract.DailyWeather.COLUMN_WEATHER_CODE,
            weather.weatherCode,
        )
    }
}

internal data class CurrentWeatherRecord(
    val weather: CurrentWeather,
    val updatedAt: Instant,
)

internal class CityCursorReader(
    private val cursor: Cursor,
) {
    private val idIndex =
        cursor.getColumnIndexOrThrow(WeatherSqlContract.Cities.COLUMN_ID)
    private val nameIndex =
        cursor.getColumnIndexOrThrow(WeatherSqlContract.Cities.COLUMN_NAME)
    private val countryCodeIndex =
        cursor.getColumnIndexOrThrow(WeatherSqlContract.Cities.COLUMN_COUNTRY_CODE)
    private val latitudeIndex =
        cursor.getColumnIndexOrThrow(WeatherSqlContract.Cities.COLUMN_LATITUDE)
    private val longitudeIndex =
        cursor.getColumnIndexOrThrow(WeatherSqlContract.Cities.COLUMN_LONGITUDE)
    private val timeZoneIdIndex =
        cursor.getColumnIndexOrThrow(WeatherSqlContract.Cities.COLUMN_TIME_ZONE_ID)

    fun readCurrentRow(): City = City(
        id = cursor.getString(idIndex),
        name = cursor.getString(nameIndex),
        countryCode = cursor.getString(countryCodeIndex),
        latitude = cursor.getDouble(latitudeIndex),
        longitude = cursor.getDouble(longitudeIndex),
        timeZoneId = cursor.getString(timeZoneIdIndex),
    )
}

internal class CurrentWeatherCursorReader(
    private val cursor: Cursor,
) {
    private val observedAtIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.CurrentWeather.COLUMN_OBSERVED_AT_EPOCH_MILLIS,
    )
    private val temperatureIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.CurrentWeather.COLUMN_TEMPERATURE_CELSIUS,
    )
    private val apparentTemperatureIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.CurrentWeather.COLUMN_APPARENT_TEMPERATURE_CELSIUS,
    )
    private val humidityIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.CurrentWeather.COLUMN_RELATIVE_HUMIDITY_PERCENT,
    )
    private val windSpeedIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.CurrentWeather.COLUMN_WIND_SPEED_KMH,
    )
    private val weatherCodeIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.CurrentWeather.COLUMN_WEATHER_CODE,
    )
    private val updatedAtIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.CurrentWeather.COLUMN_UPDATED_AT_EPOCH_MILLIS,
    )

    fun readCurrentRow(): CurrentWeatherRecord = CurrentWeatherRecord(
        weather = CurrentWeather(
            observedAt = Instant.ofEpochMilli(cursor.getLong(observedAtIndex)),
            temperatureCelsius = cursor.getDouble(temperatureIndex),
            apparentTemperatureCelsius = cursor.getDouble(apparentTemperatureIndex),
            relativeHumidityPercent = cursor.getInt(humidityIndex),
            windSpeedKmh = cursor.getDouble(windSpeedIndex),
            weatherCode = cursor.getInt(weatherCodeIndex),
        ),
        updatedAt = Instant.ofEpochMilli(cursor.getLong(updatedAtIndex)),
    )
}

internal class HourlyWeatherCursorReader(
    private val cursor: Cursor,
) {
    private val forecastAtIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.HourlyWeather.COLUMN_FORECAST_AT_EPOCH_MILLIS,
    )
    private val temperatureIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.HourlyWeather.COLUMN_TEMPERATURE_CELSIUS,
    )
    private val apparentTemperatureIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.HourlyWeather.COLUMN_APPARENT_TEMPERATURE_CELSIUS,
    )
    private val precipitationIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.HourlyWeather.COLUMN_PRECIPITATION_PROBABILITY_PERCENT,
    )
    private val humidityIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.HourlyWeather.COLUMN_RELATIVE_HUMIDITY_PERCENT,
    )
    private val windSpeedIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.HourlyWeather.COLUMN_WIND_SPEED_KMH,
    )
    private val weatherCodeIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.HourlyWeather.COLUMN_WEATHER_CODE,
    )

    fun readCurrentRow(): HourlyWeather = HourlyWeather(
        forecastAt = Instant.ofEpochMilli(cursor.getLong(forecastAtIndex)),
        temperatureCelsius = cursor.getDouble(temperatureIndex),
        apparentTemperatureCelsius = cursor.getDouble(apparentTemperatureIndex),
        precipitationProbabilityPercent = cursor.getInt(precipitationIndex),
        relativeHumidityPercent = cursor.getInt(humidityIndex),
        windSpeedKmh = cursor.getDouble(windSpeedIndex),
        weatherCode = cursor.getInt(weatherCodeIndex),
    )
}

internal class DailyWeatherCursorReader(
    private val cursor: Cursor,
) {
    private val dateIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.DailyWeather.COLUMN_FORECAST_DATE,
    )
    private val minimumTemperatureIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.DailyWeather.COLUMN_MINIMUM_TEMPERATURE_CELSIUS,
    )
    private val maximumTemperatureIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.DailyWeather.COLUMN_MAXIMUM_TEMPERATURE_CELSIUS,
    )
    private val precipitationIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.DailyWeather.COLUMN_PRECIPITATION_PROBABILITY_PERCENT,
    )
    private val weatherCodeIndex = cursor.getColumnIndexOrThrow(
        WeatherSqlContract.DailyWeather.COLUMN_WEATHER_CODE,
    )

    fun readCurrentRow(): DailyWeather = DailyWeather(
        date = LocalDate.parse(cursor.getString(dateIndex)),
        minimumTemperatureCelsius = cursor.getDouble(minimumTemperatureIndex),
        maximumTemperatureCelsius = cursor.getDouble(maximumTemperatureIndex),
        precipitationProbabilityPercent = cursor.getInt(precipitationIndex),
        weatherCode = cursor.getInt(weatherCodeIndex),
    )
}
