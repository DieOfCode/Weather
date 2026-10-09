package com.example.weather.weather.data.local.room.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.weather.weather.data.local.room.entity.CityEntity
import com.example.weather.weather.data.local.room.entity.CurrentWeatherEntity
import com.example.weather.weather.data.local.room.entity.DailyWeatherEntity
import com.example.weather.weather.data.local.room.entity.HourlyWeatherEntity


data class WeatherWithForecasts(
    @Embedded
    val city: CityEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "city_id",
    )
    val current: CurrentWeatherEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "city_id",
    )
    val hourly: List<HourlyWeatherEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "city_id",
    )
    val daily: List<DailyWeatherEntity>,
)