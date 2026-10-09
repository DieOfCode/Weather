package com.example.weather.weather.data.remote

import com.example.weather.weather.data.remote.forecast.dto.CurrentWeatherDto
import com.example.weather.weather.data.remote.forecast.dto.DailyWeatherDto
import com.example.weather.weather.data.remote.forecast.dto.ForecastResponseDto
import com.example.weather.weather.data.remote.forecast.dto.HourlyWeatherDto
import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.model.CurrentWeather
import com.example.weather.weather.domain.model.DailyWeather
import com.example.weather.weather.domain.model.HourlyWeather
import com.example.weather.weather.domain.model.WeatherForecast
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

internal object RemoteWeatherMapper {

    fun forecastToDomain(
        response: ForecastResponseDto,
        city: City,
        updatedAt: Instant,
    ): WeatherForecast {
        val zoneId = parseZoneId(response.timezone.required("timezone"))

        return WeatherForecast(
            city = city,
            current = currentToDomain(
                current = response.current.required("current"),
                zoneId = zoneId,
            ),
            hourly = hourlyToDomain(
                hourly = response.hourly.required("hourly"),
                zoneId = zoneId,
            ),
            daily = dailyToDomain(response.daily.required("daily")),
            updatedAt = updatedAt,
        )
    }

    private fun currentToDomain(
        current: CurrentWeatherDto,
        zoneId: ZoneId,
    ): CurrentWeather = CurrentWeather(
        observedAt = parseInstant(
            value = current.time.required("current.time"),
            zoneId = zoneId,
            fieldName = "current.time",
        ),
        temperatureCelsius = current.temperatureCelsius.required("current.temperature_2m"),
        apparentTemperatureCelsius = current.apparentTemperatureCelsius.required(
            "current.apparent_temperature",
        ),
        relativeHumidityPercent = current.relativeHumidityPercent.required(
            "current.relative_humidity_2m",
        ),
        windSpeedKmh = current.windSpeedKmh.required("current.wind_speed_10m"),
        weatherCode = current.weatherCode.required("current.weather_code"),
    )

    private fun hourlyToDomain(
        hourly: HourlyWeatherDto,
        zoneId: ZoneId,
    ): List<HourlyWeather> {
        validateParallelLists(
            sectionName = "hourly",
            fields = mapOf(
                "time" to hourly.time,
                "temperature_2m" to hourly.temperatureCelsius,
                "apparent_temperature" to hourly.apparentTemperatureCelsius,
                "precipitation_probability" to hourly.precipitationProbabilityPercent,
                "relative_humidity_2m" to hourly.relativeHumidityPercent,
                "wind_speed_10m" to hourly.windSpeedKmh,
                "weather_code" to hourly.weatherCode,
            ),
        )

        return hourly.time.indices
            .map { index ->
                HourlyWeather(
                    forecastAt = parseInstant(
                        value = hourly.time[index].required("hourly.time[$index]"),
                        zoneId = zoneId,
                        fieldName = "hourly.time[$index]",
                    ),
                    temperatureCelsius = hourly.temperatureCelsius[index].required(
                        "hourly.temperature_2m[$index]",
                    ),
                    apparentTemperatureCelsius = hourly.apparentTemperatureCelsius[index].required(
                        "hourly.apparent_temperature[$index]",
                    ),
                    precipitationProbabilityPercent = hourly.precipitationProbabilityPercent[index]
                        .required("hourly.precipitation_probability[$index]"),
                    relativeHumidityPercent = hourly.relativeHumidityPercent[index].required(
                        "hourly.relative_humidity_2m[$index]",
                    ),
                    windSpeedKmh = hourly.windSpeedKmh[index].required(
                        "hourly.wind_speed_10m[$index]",
                    ),
                    weatherCode = hourly.weatherCode[index].required(
                        "hourly.weather_code[$index]",
                    ),
                )
            }
            .sortedBy(HourlyWeather::forecastAt)
    }

    private fun dailyToDomain(daily: DailyWeatherDto): List<DailyWeather> {
        validateParallelLists(
            sectionName = "daily",
            fields = mapOf(
                "time" to daily.time,
                "temperature_2m_min" to daily.minimumTemperatureCelsius,
                "temperature_2m_max" to daily.maximumTemperatureCelsius,
                "precipitation_probability_max" to daily.precipitationProbabilityPercent,
                "weather_code" to daily.weatherCode,
            ),
        )

        return daily.time.indices
            .map { index ->
                DailyWeather(
                    date = parseDate(
                        value = daily.time[index].required("daily.time[$index]"),
                        fieldName = "daily.time[$index]",
                    ),
                    minimumTemperatureCelsius = daily.minimumTemperatureCelsius[index].required(
                        "daily.temperature_2m_min[$index]",
                    ),
                    maximumTemperatureCelsius = daily.maximumTemperatureCelsius[index].required(
                        "daily.temperature_2m_max[$index]",
                    ),
                    precipitationProbabilityPercent = daily.precipitationProbabilityPercent[index]
                        .required("daily.precipitation_probability_max[$index]"),
                    weatherCode = daily.weatherCode[index].required(
                        "daily.weather_code[$index]",
                    ),
                )
            }
            .sortedBy(DailyWeather::date)
    }

    private fun validateParallelLists(
        sectionName: String,
        fields: Map<String, List<*>>,
    ) {
        val fieldSizes = fields.mapValues { (_, values) -> values.size }

        if (fieldSizes.values.distinct().size != 1) {
            val actualSizes = fieldSizes.entries.joinToString { (name, size) ->
                "$name=$size"
            }

            invalidResponse("$sectionName fields have different sizes: $actualSizes")
        }

        if (fieldSizes.values.first() == 0) {
            invalidResponse("$sectionName forecast is empty")
        }
    }

    private fun parseZoneId(value: String): ZoneId = try {
        ZoneId.of(value)
    } catch (exception: DateTimeException) {
        throw RemoteDataException.InvalidResponse(
            reason = "timezone has an invalid value: $value",
            cause = exception,
        )
    }

    private fun parseInstant(
        value: String,
        zoneId: ZoneId,
        fieldName: String,
    ): Instant = try {
        LocalDateTime.parse(value)
            .atZone(zoneId)
            .toInstant()
    } catch (exception: DateTimeException) {
        throw RemoteDataException.InvalidResponse(
            reason = "$fieldName has an invalid value: $value",
            cause = exception,
        )
    }

    private fun parseDate(
        value: String,
        fieldName: String,
    ): LocalDate = try {
        LocalDate.parse(value)
    } catch (exception: DateTimeException) {
        throw RemoteDataException.InvalidResponse(
            reason = "$fieldName has an invalid value: $value",
            cause = exception,
        )
    }

    private fun <T : Any> T?.required(fieldName: String): T =
        this ?: invalidResponse("Required field is missing: $fieldName")

    private fun invalidResponse(reason: String): Nothing =
        throw RemoteDataException.InvalidResponse(reason)
}
