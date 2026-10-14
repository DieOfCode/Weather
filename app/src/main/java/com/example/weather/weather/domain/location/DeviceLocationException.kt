package com.example.weather.weather.domain.location

sealed class DeviceLocationException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    class LocationUnavailable : DeviceLocationException(
        message = "Current location is unavailable",
    )

    class PermissionMissing(
        cause: Throwable,
    ) : DeviceLocationException(
        message = "Location permission is missing",
        cause = cause,
    )

    class ProviderUnavailable(
        cause: Throwable,
    ) : DeviceLocationException(
        message = "Device location is unavailable",
        cause = cause,
    )
}
