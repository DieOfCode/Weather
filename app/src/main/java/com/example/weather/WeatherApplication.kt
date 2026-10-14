package com.example.weather

import android.app.Application
import com.example.weather.weather.di.coroutineModule
import com.example.weather.weather.di.locationModule
import com.example.weather.weather.di.networkModule
import com.example.weather.weather.di.roomLocalStorageModule
import com.example.weather.weather.di.weatherPresentationModule
import com.example.weather.weather.di.weatherRepositoryModule
import com.example.weather.weather.di.weatherUseCaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class WeatherApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@WeatherApplication)
            modules(
                coroutineModule,
                roomLocalStorageModule,
                networkModule,
                locationModule,
                weatherRepositoryModule,
                weatherPresentationModule,
                weatherUseCaseModule,
            )
        }
    }
}
