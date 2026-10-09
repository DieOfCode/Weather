package com.example.weather.weather

import com.example.weather.weather.domain.model.DailyWeather
import com.example.weather.weather.domain.model.WeatherForecast
import kotlin.math.roundToInt

internal object WeatherUiMapper {

    private const val DAYS_IN_WEEK = 7

    fun forecastToUiState(forecast: WeatherForecast): WeatherUiState {
        val currentCondition = weatherCondition(forecast.current.weatherCode)

        return WeatherUiState(
            cityName = forecast.city.name,
            timeZoneId = forecast.city.timeZoneId,
            temperature = forecast.current.temperatureCelsius.roundToInt(),
            condition = currentCondition,
            feelsLike = forecast.current.apparentTemperatureCelsius.roundToInt(),
            humidityPercent = forecast.current.relativeHumidityPercent,
            windSpeedKmh = forecast.current.windSpeedKmh.roundToInt(),
            dailyForecast = forecast.daily
                .sortedBy(DailyWeather::date)
                .take(DAYS_IN_WEEK)
                .map { weather ->
                    weather.toUiState()
                },
        )
    }

    private fun DailyWeather.toUiState(): DailyWeatherUi {
        val condition = weatherCondition(weatherCode)

        return DailyWeatherUi(
            id = date.toString(),
            date = date,
            condition = condition,
            minimumTemperature = minimumTemperatureCelsius.roundToInt(),
            maximumTemperature = maximumTemperatureCelsius.roundToInt(),
        )
    }

    private fun weatherCondition(weatherCode: Int): WeatherConditionUi = when (weatherCode) {
        0 -> WeatherConditionUi.CLEAR
        1 -> WeatherConditionUi.MAINLY_CLEAR
        2 -> WeatherConditionUi.PARTLY_CLOUDY
        3 -> WeatherConditionUi.OVERCAST
        45, 48 -> WeatherConditionUi.FOG
        51 -> WeatherConditionUi.LIGHT_DRIZZLE
        53 -> WeatherConditionUi.DRIZZLE
        55 -> WeatherConditionUi.HEAVY_DRIZZLE
        56, 57 -> WeatherConditionUi.FREEZING_DRIZZLE
        61 -> WeatherConditionUi.LIGHT_RAIN
        63 -> WeatherConditionUi.RAIN
        65 -> WeatherConditionUi.HEAVY_RAIN
        66, 67 -> WeatherConditionUi.FREEZING_RAIN
        71 -> WeatherConditionUi.LIGHT_SNOW
        73 -> WeatherConditionUi.SNOW
        75 -> WeatherConditionUi.HEAVY_SNOW
        77 -> WeatherConditionUi.SNOW_GRAINS
        80 -> WeatherConditionUi.LIGHT_RAIN_SHOWER
        81 -> WeatherConditionUi.RAIN_SHOWER
        82 -> WeatherConditionUi.HEAVY_RAIN_SHOWER
        85 -> WeatherConditionUi.LIGHT_SNOW_SHOWER
        86 -> WeatherConditionUi.HEAVY_SNOW_SHOWER
        95, 97 -> WeatherConditionUi.THUNDERSTORM
        96, 99 -> WeatherConditionUi.THUNDERSTORM_WITH_HAIL
        else -> WeatherConditionUi.UNKNOWN
    }
}
