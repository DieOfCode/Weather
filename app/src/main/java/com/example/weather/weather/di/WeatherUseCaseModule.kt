package com.example.weather.weather.di

import com.example.weather.weather.domain.usecase.LoadWeatherForCurrentLocationUseCase
import org.koin.dsl.module

val weatherUseCaseModule = module {
    factory {
        LoadWeatherForCurrentLocationUseCase(get(), get(), get())
    }
}
