package com.example.weather.weather.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "daily_weather",
    primaryKeys = ["city_id", "forecast_date"],
    foreignKeys = [
        ForeignKey(
            entity = CityEntity::class,
            parentColumns = ["id"],
            childColumns = ["city_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class DailyWeatherEntity(
    @ColumnInfo(name = "city_id")
    val cityId: String,
    @ColumnInfo(name = "forecast_date")
    val forecastDate: String,
    @ColumnInfo(name = "minimum_temperature_celsius")
    val minimumTemperatureCelsius: Double,
    @ColumnInfo(name = "maximum_temperature_celsius")
    val maximumTemperatureCelsius: Double,
    @ColumnInfo(name = "precipitation_probability_percent")
    val precipitationProbabilityPercent: Int,
    @ColumnInfo(name = "weather_code")
    val weatherCode: Int,
)
