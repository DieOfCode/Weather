package com.example.weather.weather.di

import com.example.weather.weather.WeatherViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val weatherPresentationModule = module {
    viewModelOf(::WeatherViewModel)
}
