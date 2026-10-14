package com.example.weather.weather.data.remote.geocoding.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class GeocodingResponseDto(
    val results: List<GeocodingCityDto> = emptyList(),
)
