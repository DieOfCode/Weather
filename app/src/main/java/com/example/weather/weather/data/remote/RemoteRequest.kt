package com.example.weather.weather.data.remote

import java.io.IOException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

internal suspend fun <T> executeRemoteRequest(
    block: suspend () -> T,
): T = try {
    block()
} catch (exception: HttpException) {
    throw RemoteDataException.Http(
        statusCode = exception.code(),
        cause = exception,
    )
} catch (exception: IOException) {
    throw RemoteDataException.Network(exception)
} catch (exception: SerializationException) {
    throw RemoteDataException.Serialization(exception)
}
