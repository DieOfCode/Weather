package com.example.weather.weather.data.remote

import com.example.weather.weather.data.remote.geocoding.dto.GeocodingCityDto
import com.example.weather.weather.data.remote.geocoding.dto.GeocodingResponseDto
import com.example.weather.weather.domain.model.City
import java.time.DateTimeException
import java.time.ZoneId

internal object RemoteCityMapper {

    fun citiesToDomain(response: GeocodingResponseDto): List<City> =
        response.results.mapIndexed(::cityToDomain)

    private fun cityToDomain(
        index: Int,
        city: GeocodingCityDto,
    ): City {
        val fieldPrefix = "results[$index]"
        val id = city.id.required("$fieldPrefix.id")
        val latitude = city.latitude.required("$fieldPrefix.latitude")
        val longitude = city.longitude.required("$fieldPrefix.longitude")

        validateLatitude(latitude, "$fieldPrefix.latitude")
        validateLongitude(longitude, "$fieldPrefix.longitude")

        return City(
            id = id.toString(),
            name = city.name.requiredText("$fieldPrefix.name"),
            countryCode = city.countryCode.requiredText("$fieldPrefix.country_code"),
            latitude = latitude,
            longitude = longitude,
            timeZoneId = parseZoneId(
                value = city.timezone.requiredText("$fieldPrefix.timezone"),
                fieldName = "$fieldPrefix.timezone",
            ).id,
        )
    }

    private fun validateLatitude(
        value: Double,
        fieldName: String,
    ) {
        if (!value.isFinite() || value !in LATITUDE_RANGE) {
            invalidResponse("$fieldName has an invalid value: $value")
        }
    }

    private fun validateLongitude(
        value: Double,
        fieldName: String,
    ) {
        if (!value.isFinite() || value !in LONGITUDE_RANGE) {
            invalidResponse("$fieldName has an invalid value: $value")
        }
    }

    private fun parseZoneId(
        value: String,
        fieldName: String,
    ): ZoneId = try {
        ZoneId.of(value)
    } catch (exception: DateTimeException) {
        throw RemoteDataException.InvalidResponse(
            reason = "$fieldName has an invalid value: $value",
            cause = exception,
        )
    }

    private fun String?.requiredText(fieldName: String): String =
        required(fieldName).takeIf(String::isNotBlank)
            ?: invalidResponse("Required field is blank: $fieldName")

    private fun <T : Any> T?.required(fieldName: String): T =
        this ?: invalidResponse("Required field is missing: $fieldName")

    private fun invalidResponse(reason: String): Nothing =
        throw RemoteDataException.InvalidResponse(reason)

    private val LATITUDE_RANGE = -90.0..90.0
    private val LONGITUDE_RANGE = -180.0..180.0
}
