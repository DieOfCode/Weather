package com.example.weather.weather.domain.location

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude in MIN_LATITUDE..MAX_LATITUDE) {
            "Latitude must be between $MIN_LATITUDE and $MAX_LATITUDE: $latitude"
        }
        require(longitude in MIN_LONGITUDE..MAX_LONGITUDE) {
            "Longitude must be between $MIN_LONGITUDE and $MAX_LONGITUDE: $longitude"
        }
    }

    private companion object {
        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0
        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0
    }
}
