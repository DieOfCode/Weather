package com.example.weather.weather

data class WeatherUiState(
    val cityName: String,
    val temperature: Int,
    val description: String,
    val feelsLike: Int,
    val humidityPercent: Int,
    val windSpeedKmh: Int,
    val dailyForecast: List<DailyWeatherUi>,
)

data class DailyWeatherUi(
    val id: String,
    val day: String,
    val conditionEmoji: String,
    val description: String,
    val minimumTemperature: Int,
    val maximumTemperature: Int,
)

internal val sampleWeatherUiState = WeatherUiState(
    cityName = "Алматы",
    temperature = 18,
    description = "Переменная облачность",
    feelsLike = 17,
    humidityPercent = 54,
    windSpeedKmh = 11,
    dailyForecast = listOf(
        DailyWeatherUi("mon", "Сегодня", "⛅", "Облачно", 11, 19),
        DailyWeatherUi("tue", "Вторник", "🌤️", "Малооблачно", 10, 21),
        DailyWeatherUi("wed", "Среда", "🌧️", "Небольшой дождь", 9, 16),
        DailyWeatherUi("thu", "Четверг", "☀️", "Ясно", 8, 20),
        DailyWeatherUi("fri", "Пятница", "☀️", "Ясно", 10, 23),
        DailyWeatherUi("sat", "Суббота", "🌤️", "Малооблачно", 12, 24),
        DailyWeatherUi("sun", "Воскресенье", "⛅", "Облачно", 13, 22),
    ),
)


internal val sampleWeatherWinterUiState = WeatherUiState(
    cityName = "Астана",
    temperature = -18,
    description = "Снег",
    feelsLike = -25,
    humidityPercent = 24,
    windSpeedKmh = 24,
    dailyForecast = listOf(
        DailyWeatherUi("mon", "Сегодня", "🌨️", "Снег", -19, -11),
        DailyWeatherUi("tue", "Вторник", "❄️", "Снег", -21, -10),
        DailyWeatherUi("wed", "Среда", "🌨️", "Снег", -16, -9),
        DailyWeatherUi("thu", "Четверг", "☁️", "Облачно", -20, -8),
        DailyWeatherUi("fri", "Пятница", "☀️", "Ясно", -23, -10),
        DailyWeatherUi("sat", "Суббота", "🌤️", "Малооблачно", -24, -12),
        DailyWeatherUi("sun", "Воскресенье", "⛅", "Облачно", -22, -13),
    ),
)
