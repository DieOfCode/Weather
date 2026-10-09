package com.example.weather.weather.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "hourly_weather",
    primaryKeys = ["city_id", "forecast_at_epoch_millis"],
    foreignKeys = [
        ForeignKey(
            entity = CityEntity::class,
            parentColumns = ["id"],
            childColumns = ["city_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class HourlyWeatherEntity(
    @ColumnInfo(name = "city_id")
    val cityId: String,
    @ColumnInfo(name = "forecast_at_epoch_millis")
    val forecastAtEpochMillis: Long,
    @ColumnInfo(name = "temperature_celsius")
    val temperatureCelsius: Double,
    @ColumnInfo(name = "apparent_temperature_celsius")
    val apparentTemperatureCelsius: Double,
    @ColumnInfo(name = "precipitation_probability_percent")
    val precipitationProbabilityPercent: Int,
    @ColumnInfo(name = "relative_humidity_percent")
    val relativeHumidityPercent: Int,
    @ColumnInfo(name = "wind_speed_kmh")
    val windSpeedKmh: Double,
    @ColumnInfo(name = "weather_code")
    val weatherCode: Int,
)
