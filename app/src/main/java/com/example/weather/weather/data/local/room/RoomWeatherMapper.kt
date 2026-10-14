package com.example.weather.weather.data.local.room

import com.example.weather.weather.data.local.room.entity.CityEntity
import com.example.weather.weather.data.local.room.entity.CurrentWeatherEntity
import com.example.weather.weather.data.local.room.entity.DailyWeatherEntity
import com.example.weather.weather.data.local.room.entity.HourlyWeatherEntity
import com.example.weather.weather.data.local.room.relation.WeatherWithForecasts
import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.model.CurrentWeather
import com.example.weather.weather.domain.model.DailyWeather
import com.example.weather.weather.domain.model.HourlyWeather
import com.example.weather.weather.domain.model.WeatherForecast
import java.time.Instant
import java.time.LocalDate

internal data class RoomForecastEntities(
    val city: CityEntity,
    val current: CurrentWeatherEntity,
    val hourly: List<HourlyWeatherEntity>,
    val daily: List<DailyWeatherEntity>,
)

internal object RoomWeatherMapper {

    fun forecastToEntities(forecast: WeatherForecast): RoomForecastEntities {
        val cityId = forecast.city.id

        return RoomForecastEntities(
            city = cityToEntity(forecast.city),
            current = currentWeatherToEntity(
                cityId = cityId,
                weather = forecast.current,
                updatedAt = forecast.updatedAt,
            ),
            hourly = forecast.hourly.map { weather ->
                hourlyWeatherToEntity(cityId, weather)
            },
            daily = forecast.daily.map { weather ->
                dailyWeatherToEntity(cityId, weather)
            },
        )
    }

    fun cityToEntity(city: City): CityEntity = CityEntity(
        id = city.id,
        name = city.name,
        countryCode = city.countryCode,
        latitude = city.latitude,
        longitude = city.longitude,
        timeZoneId = city.timeZoneId,
    )

    fun currentWeatherToEntity(
        cityId: String,
        weather: CurrentWeather,
        updatedAt: Instant,
    ): CurrentWeatherEntity = CurrentWeatherEntity(
        cityId = cityId,
        observedAtEpochMillis = weather.observedAt.toEpochMilli(),
        temperatureCelsius = weather.temperatureCelsius,
        apparentTemperatureCelsius = weather.apparentTemperatureCelsius,
        relativeHumidityPercent = weather.relativeHumidityPercent,
        windSpeedKmh = weather.windSpeedKmh,
        weatherCode = weather.weatherCode,
        updatedAtEpochMillis = updatedAt.toEpochMilli(),
    )

    fun hourlyWeatherToEntity(
        cityId: String,
        weather: HourlyWeather,
    ): HourlyWeatherEntity = HourlyWeatherEntity(
        cityId = cityId,
        forecastAtEpochMillis = weather.forecastAt.toEpochMilli(),
        temperatureCelsius = weather.temperatureCelsius,
        apparentTemperatureCelsius = weather.apparentTemperatureCelsius,
        precipitationProbabilityPercent = weather.precipitationProbabilityPercent,
        relativeHumidityPercent = weather.relativeHumidityPercent,
        windSpeedKmh = weather.windSpeedKmh,
        weatherCode = weather.weatherCode,
    )

    fun dailyWeatherToEntity(
        cityId: String,
        weather: DailyWeather,
    ): DailyWeatherEntity = DailyWeatherEntity(
        cityId = cityId,
        forecastDate = weather.date.toString(),
        minimumTemperatureCelsius = weather.minimumTemperatureCelsius,
        maximumTemperatureCelsius = weather.maximumTemperatureCelsius,
        precipitationProbabilityPercent = weather.precipitationProbabilityPercent,
        weatherCode = weather.weatherCode,
    )

    fun cityToDomain(entity: CityEntity): City = City(
        id = entity.id,
        name = entity.name,
        countryCode = entity.countryCode,
        latitude = entity.latitude,
        longitude = entity.longitude,
        timeZoneId = entity.timeZoneId,
    )

    fun currentWeatherToDomain(entity: CurrentWeatherEntity): CurrentWeather = CurrentWeather(
        observedAt = Instant.ofEpochMilli(entity.observedAtEpochMillis),
        temperatureCelsius = entity.temperatureCelsius,
        apparentTemperatureCelsius = entity.apparentTemperatureCelsius,
        relativeHumidityPercent = entity.relativeHumidityPercent,
        windSpeedKmh = entity.windSpeedKmh,
        weatherCode = entity.weatherCode,
    )

    fun hourlyWeatherToDomain(entity: HourlyWeatherEntity): HourlyWeather = HourlyWeather(
        forecastAt = Instant.ofEpochMilli(entity.forecastAtEpochMillis),
        temperatureCelsius = entity.temperatureCelsius,
        apparentTemperatureCelsius = entity.apparentTemperatureCelsius,
        precipitationProbabilityPercent = entity.precipitationProbabilityPercent,
        relativeHumidityPercent = entity.relativeHumidityPercent,
        windSpeedKmh = entity.windSpeedKmh,
        weatherCode = entity.weatherCode,
    )

    fun dailyWeatherToDomain(entity: DailyWeatherEntity): DailyWeather = DailyWeather(
        date = LocalDate.parse(entity.forecastDate),
        minimumTemperatureCelsius = entity.minimumTemperatureCelsius,
        maximumTemperatureCelsius = entity.maximumTemperatureCelsius,
        precipitationProbabilityPercent = entity.precipitationProbabilityPercent,
        weatherCode = entity.weatherCode,
    )

    fun forecastToDomain(forecast: WeatherWithForecasts): WeatherForecast? {
        val current = forecast.current ?: return null

        return WeatherForecast(
            city = cityToDomain(forecast.city),
            current = currentWeatherToDomain(current),
            hourly = forecast.hourly
                .map(::hourlyWeatherToDomain)
                .sortedBy { weather -> weather.forecastAt },
            daily = forecast.daily
                .map(::dailyWeatherToDomain)
                .sortedBy { weather -> weather.date },
            updatedAt = Instant.ofEpochMilli(current.updatedAtEpochMillis),
        )
    }
}
