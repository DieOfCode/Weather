package com.example.weather.weather.di

import com.example.weather.weather.data.repository.OfflineFirstWeatherRepository
import com.example.weather.weather.domain.repository.WeatherRepository
import org.koin.dsl.module

val weatherRepositoryModule = module {
    single<WeatherRepository> {
        OfflineFirstWeatherRepository(get(), get(), get())
    }
}