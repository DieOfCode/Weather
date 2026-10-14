package com.example.weather.weather.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val coroutineModule = module {
    single<CoroutineDispatcher>(ioDispatcherQualifier) {
        Dispatchers.IO
    }
}
