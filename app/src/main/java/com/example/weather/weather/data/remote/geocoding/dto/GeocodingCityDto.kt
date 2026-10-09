package com.example.weather.weather.data.remote.geocoding.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GeocodingCityDto(
    val id: Long? = null,
    val name: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("country_code")
    val countryCode: String? = null,
    val timezone: String? = null,
    val country: String? = null,
    val admin1: String? = null,
)
