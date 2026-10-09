package com.example.weather.weather.di

import com.example.weather.weather.data.remote.CityRemoteDataSource
import com.example.weather.weather.data.remote.OpenMeteoCityRemoteDataSource
import com.example.weather.weather.data.remote.OpenMeteoWeatherRemoteDataSource
import com.example.weather.weather.data.remote.WeatherRemoteDataSource
import com.example.weather.weather.data.remote.forecast.OpenMeteoWeatherApi
import com.example.weather.weather.data.remote.geocoding.OpenMeteoGeocodingApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val WEATHER_BASE_URL = "https://api.open-meteo.com/"
private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
private val JSON_MEDIA_TYPE = "application/json".toMediaType()

val networkModule = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
        }
    }

    single<OkHttpClient> {
        OkHttpClient.Builder().build()
    }

    single<Retrofit>(weatherRetrofitQualifier) {
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(get<Json>().asConverterFactory(JSON_MEDIA_TYPE))
            .build()
    }

    single<Retrofit>(geocodingRetrofitQualifier) {
        Retrofit.Builder()
            .baseUrl(GEOCODING_BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(get<Json>().asConverterFactory(JSON_MEDIA_TYPE))
            .build()
    }

    single<OpenMeteoWeatherApi> {
        get<Retrofit>(weatherRetrofitQualifier)
            .create(OpenMeteoWeatherApi::class.java)
    }

    single<OpenMeteoGeocodingApi> {
        get<Retrofit>(geocodingRetrofitQualifier)
            .create(OpenMeteoGeocodingApi::class.java)
    }

    single<WeatherRemoteDataSource> {
        OpenMeteoWeatherRemoteDataSource(
            weatherApi = get<OpenMeteoWeatherApi>(),
        )
    }

    single<CityRemoteDataSource> {
        OpenMeteoCityRemoteDataSource(
            geocodingApi = get<OpenMeteoGeocodingApi>(),
        )
    }
}
