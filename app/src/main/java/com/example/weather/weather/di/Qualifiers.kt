package com.example.weather.weather.di

import org.koin.core.qualifier.named

internal val ioDispatcherQualifier = named("ioDispatcher")

internal val weatherRetrofitQualifier = named("weatherRetrofit")
internal val geocodingRetrofitQualifier = named("geocodingRetrofit")
