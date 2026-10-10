package com.example.weather.weather.di

import androidx.room.Room
import com.example.weather.weather.data.local.WeatherLocalDataSource
import com.example.weather.weather.data.local.room.RoomWeatherLocalDataSource
import com.example.weather.weather.data.local.room.WeatherRoomDatabase
import com.example.weather.weather.data.local.sqlite.DatabaseHelper
import com.example.weather.weather.data.local.sqlite.SqliteWeatherLocalDataSource
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val roomLocalStorageModule = module {
    single<WeatherRoomDatabase> {
        Room.databaseBuilder(
            androidContext(),
            WeatherRoomDatabase::class.java,
            WeatherRoomDatabase.DATABASE_NAME
        )
            .addMigrations(WeatherRoomDatabase.MIGRATION_1_2)
            .build()
    }
    single<WeatherLocalDataSource> { RoomWeatherLocalDataSource(get<WeatherRoomDatabase>()) }
}

val sqliteLocalStorageModule = module {
    single<DatabaseHelper> {
        DatabaseHelper(androidContext())
    }

    single<WeatherLocalDataSource> {
        SqliteWeatherLocalDataSource(
            databaseHelper = get<DatabaseHelper>(),
            ioDispatcher = get<CoroutineDispatcher>(ioDispatcherQualifier),
        )
    }
}
