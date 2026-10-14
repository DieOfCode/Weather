package com.example.weather.weather

import androidx.annotation.StringRes
import com.example.weather.R
import java.time.LocalDate

data class WeatherUiState(
    val cityName: String,
    val timeZoneId: String,
    val temperature: Int,
    val condition: WeatherConditionUi,
    val feelsLike: Int,
    val humidityPercent: Int,
    val windSpeedKmh: Int,
    val dailyForecast: List<DailyWeatherUi>,
)

data class DailyWeatherUi(
    val id: String,
    val date: LocalDate,
    val condition: WeatherConditionUi,
    val minimumTemperature: Int,
    val maximumTemperature: Int,
)

enum class WeatherConditionUi(
    @StringRes val descriptionResId: Int,
    val emoji: String,
) {
    CLEAR(R.string.weather_clear, "☀️"),
    MAINLY_CLEAR(R.string.weather_mainly_clear, "🌤️"),
    PARTLY_CLOUDY(R.string.weather_partly_cloudy, "⛅"),
    OVERCAST(R.string.weather_overcast, "☁️"),
    FOG(R.string.weather_fog, "🌫️"),
    LIGHT_DRIZZLE(R.string.weather_light_drizzle, "🌦️"),
    DRIZZLE(R.string.weather_drizzle, "🌦️"),
    HEAVY_DRIZZLE(R.string.weather_heavy_drizzle, "🌧️"),
    FREEZING_DRIZZLE(R.string.weather_freezing_drizzle, "🌧️"),
    LIGHT_RAIN(R.string.weather_light_rain, "🌦️"),
    RAIN(R.string.weather_rain, "🌧️"),
    HEAVY_RAIN(R.string.weather_heavy_rain, "🌧️"),
    FREEZING_RAIN(R.string.weather_freezing_rain, "🌧️"),
    LIGHT_SNOW(R.string.weather_light_snow, "🌨️"),
    SNOW(R.string.weather_snow, "🌨️"),
    HEAVY_SNOW(R.string.weather_heavy_snow, "❄️"),
    SNOW_GRAINS(R.string.weather_snow_grains, "❄️"),
    LIGHT_RAIN_SHOWER(R.string.weather_light_rain_shower, "🌦️"),
    RAIN_SHOWER(R.string.weather_rain_shower, "🌧️"),
    HEAVY_RAIN_SHOWER(R.string.weather_heavy_rain_shower, "🌧️"),
    LIGHT_SNOW_SHOWER(R.string.weather_light_snow_shower, "🌨️"),
    HEAVY_SNOW_SHOWER(R.string.weather_heavy_snow_shower, "🌨️"),
    THUNDERSTORM(R.string.weather_thunderstorm, "⛈️"),
    THUNDERSTORM_WITH_HAIL(R.string.weather_thunderstorm_with_hail, "⛈️"),
    UNKNOWN(R.string.weather_unknown, "❔"),
}

private val sampleStartDate: LocalDate = LocalDate.now()

internal val sampleWeatherUiState = WeatherUiState(
    cityName = "Almaty",
    timeZoneId = "Asia/Almaty",
    temperature = 18,
    condition = WeatherConditionUi.PARTLY_CLOUDY,
    feelsLike = 17,
    humidityPercent = 54,
    windSpeedKmh = 11,
    dailyForecast = listOf(
        sampleDay(0, WeatherConditionUi.PARTLY_CLOUDY, 11, 19),
        sampleDay(1, WeatherConditionUi.MAINLY_CLEAR, 10, 21),
        sampleDay(2, WeatherConditionUi.LIGHT_RAIN, 9, 16),
        sampleDay(3, WeatherConditionUi.CLEAR, 8, 20),
        sampleDay(4, WeatherConditionUi.CLEAR, 10, 23),
        sampleDay(5, WeatherConditionUi.MAINLY_CLEAR, 12, 24),
        sampleDay(6, WeatherConditionUi.PARTLY_CLOUDY, 13, 22),
    ),
)

internal val sampleWeatherWinterUiState = WeatherUiState(
    cityName = "Astana",
    timeZoneId = "Asia/Almaty",
    temperature = -18,
    condition = WeatherConditionUi.SNOW,
    feelsLike = -25,
    humidityPercent = 24,
    windSpeedKmh = 24,
    dailyForecast = listOf(
        sampleDay(0, WeatherConditionUi.LIGHT_SNOW, -19, -11),
        sampleDay(1, WeatherConditionUi.SNOW, -21, -10),
        sampleDay(2, WeatherConditionUi.LIGHT_SNOW, -16, -9),
        sampleDay(3, WeatherConditionUi.OVERCAST, -20, -8),
        sampleDay(4, WeatherConditionUi.CLEAR, -23, -10),
        sampleDay(5, WeatherConditionUi.MAINLY_CLEAR, -24, -12),
        sampleDay(6, WeatherConditionUi.PARTLY_CLOUDY, -22, -13),
    ),
)

private fun sampleDay(
    daysFromToday: Long,
    condition: WeatherConditionUi,
    minimumTemperature: Int,
    maximumTemperature: Int,
): DailyWeatherUi {
    val date = sampleStartDate.plusDays(daysFromToday)

    return DailyWeatherUi(
        id = date.toString(),
        date = date,
        condition = condition,
        minimumTemperature = minimumTemperature,
        maximumTemperature = maximumTemperature,
    )
}
