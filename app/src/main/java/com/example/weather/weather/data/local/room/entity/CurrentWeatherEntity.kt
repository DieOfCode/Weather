package com.example.weather.weather.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "current_weather",
    foreignKeys = [
        ForeignKey(
            entity = CityEntity::class,
            parentColumns = ["id"],
            childColumns = ["city_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CurrentWeatherEntity(
    @PrimaryKey
    @ColumnInfo(name = "city_id")
    val cityId: String,
    @ColumnInfo(name = "observed_at_epoch_millis")
    val observedAtEpochMillis: Long,
    @ColumnInfo(name = "temperature_celsius")
    val temperatureCelsius: Double,
    @ColumnInfo(name = "apparent_temperature_celsius")
    val apparentTemperatureCelsius: Double,
    @ColumnInfo(name = "relative_humidity_percent")
    val relativeHumidityPercent: Int,
    @ColumnInfo(name = "wind_speed_kmh")
    val windSpeedKmh: Double,
    @ColumnInfo(name = "weather_code")
    val weatherCode: Int,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)
