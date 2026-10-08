package com.example.weather.weather.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weather.weather.data.local.room.entity.CityEntity
import com.example.weather.weather.data.local.room.entity.CurrentWeatherEntity
import com.example.weather.weather.data.local.room.entity.DailyWeatherEntity
import com.example.weather.weather.data.local.room.entity.HourlyWeatherEntity

@Database(
    entities = [CityEntity::class, CurrentWeatherEntity::class, DailyWeatherEntity::class, HourlyWeatherEntity::class],
    version = 1,
    exportSchema = true
)
abstract class WeatherRoomDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "weather_room.db"
    }

    abstract fun weatherDao(): WeatherDao
}