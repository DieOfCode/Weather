package com.example.weather.weather.domain.repository

sealed class WeatherRepositoryException(
    message: String,
    cause: Throwable,
) : Exception(message, cause) {

    class Network(
        cause: Throwable,
    ) : WeatherRepositoryException(
        message = "Network request failed",
        cause = cause,
    )

    class Service(
        val statusCode: Int,
        cause: Throwable,
    ) : WeatherRepositoryException(
        message = "Remote service request failed with HTTP status $statusCode",
        cause = cause,
    )

    class InvalidData(
        cause: Throwable,
    ) : WeatherRepositoryException(
        message = "Remote service returned invalid data",
        cause = cause,
    )
}
