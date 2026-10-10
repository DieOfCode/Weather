package com.example.weather.weather.di

import com.example.weather.weather.data.location.AndroidLocationCityResolver
import com.example.weather.weather.data.location.FusedDeviceLocationProvider
import com.example.weather.weather.domain.location.DeviceLocationProvider
import com.example.weather.weather.domain.location.LocationCityResolver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val locationModule = module {
    single<FusedLocationProviderClient> {
        LocationServices.getFusedLocationProviderClient(androidContext())
    }

    single<DeviceLocationProvider> {
        FusedDeviceLocationProvider(
            fusedLocationClient = get<FusedLocationProviderClient>(),
        )
    }

    single<LocationCityResolver> {
        AndroidLocationCityResolver(
            context = androidContext(),
            ioDispatcher = get<CoroutineDispatcher>(ioDispatcherQualifier),
        )
    }
}
