package com.example.weather.weather.data.remote

import java.io.IOException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

internal sealed class RemoteDataException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    class Network(
        cause: IOException,
    ) : RemoteDataException(
        message = "Network request failed",
        cause = cause,
    )

    class Http(
        val statusCode: Int,
        cause: HttpException,
    ) : RemoteDataException(
        message = "HTTP request failed with status code $statusCode",
        cause = cause,
    )

    class Serialization(
        cause: SerializationException,
    ) : RemoteDataException(
        message = "Failed to deserialize remote response",
        cause = cause,
    )

    class InvalidResponse(
        val reason: String,
        cause: Throwable? = null,
    ) : RemoteDataException(
        message = "Invalid remote response: $reason",
        cause = cause,
    )
}
