package com.example.weather

import android.app.Application
import com.example.weather.weather.di.roomLocalStorageModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class WeatherApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@WeatherApplication)
            modules(roomLocalStorageModule)
        }
    }
}
