package com.example.weather.weather.data.remote.forecast

import com.example.weather.weather.data.remote.forecast.dto.ForecastResponseDto
import com.example.weather.weather.domain.model.WeatherForecast
import retrofit2.http.GET
import retrofit2.http.Query

internal interface OpenMeteoWeatherApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("timezone") timezone: String,
        @Query("current") currentFields: String = CURRENT_FIELDS,
        @Query("hourly") hourlyFields: String = HOURLY_FIELDS,
        @Query("daily") dailyFields: String = DAILY_FIELDS,
        @Query("forecast_days") forecastDays: Int = DEFAULT_FORECAST_DAYS,
        @Query("temperature_unit") temperatureUnit: String = TEMPERATURE_UNIT,
        @Query("wind_speed_unit") windSpeedUnit: String = WIND_SPEED_UNIT,
    ): ForecastResponseDto

    private companion object {
        const val CURRENT_FIELDS =
            "temperature_2m,apparent_temperature,relative_humidity_2m," +
                    "weather_code,wind_speed_10m"

        const val HOURLY_FIELDS =
            "temperature_2m,apparent_temperature,precipitation_probability," +
                    "relative_humidity_2m,weather_code,wind_speed_10m"

        const val DAILY_FIELDS =
            "temperature_2m_min,temperature_2m_max," +
                    "precipitation_probability_max,weather_code"

        const val DEFAULT_FORECAST_DAYS = 7
        const val TEMPERATURE_UNIT = "celsius"
        const val WIND_SPEED_UNIT = "kmh"
    }
}
