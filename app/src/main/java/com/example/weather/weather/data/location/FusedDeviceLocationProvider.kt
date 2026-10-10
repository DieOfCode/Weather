package com.example.weather.weather.data.location

import com.example.weather.weather.domain.location.DeviceLocation
import com.example.weather.weather.domain.location.DeviceLocationException
import com.example.weather.weather.domain.location.DeviceLocationProvider
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class FusedDeviceLocationProvider(
    private val fusedLocationClient: FusedLocationProviderClient,
) : DeviceLocationProvider {

    override suspend fun getCurrentLocation(): DeviceLocation? =
        suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }

            try {
                fusedLocationClient
                    .getCurrentLocation(
                        currentLocationRequest,
                        cancellationTokenSource.token,
                    )
                    .addOnSuccessListener { location ->
                        if (!continuation.isActive) return@addOnSuccessListener

                        try {
                            continuation.resume(
                                location?.let { value ->
                                    DeviceLocation(
                                        latitude = value.latitude,
                                        longitude = value.longitude,
                                    )
                                },
                            )
                        } catch (exception: Exception) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(
                                    exception.toDeviceLocationException(),
                                )
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) {
                            continuation.resumeWithException(
                                exception.toDeviceLocationException(),
                            )
                        }
                    }
                    .addOnCanceledListener {
                        continuation.cancel()
                    }
            } catch (exception: SecurityException) {
                if (continuation.isActive) {
                    continuation.resumeWithException(
                        DeviceLocationException.PermissionMissing(exception),
                    )
                }
            } catch (exception: Exception) {
                if (continuation.isActive) {
                    continuation.resumeWithException(
                        exception.toDeviceLocationException(),
                    )
                }
            }
        }

    private fun Throwable.toDeviceLocationException(): DeviceLocationException =
        when (this) {
            is SecurityException -> DeviceLocationException.PermissionMissing(this)
            else -> DeviceLocationException.ProviderUnavailable(this)
        }

    private companion object {
        const val REQUEST_DURATION_MILLIS = 15_000L
        const val MAX_UPDATE_AGE_MILLIS = 60_000L

        val currentLocationRequest: CurrentLocationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setGranularity(Granularity.GRANULARITY_COARSE)
            .setDurationMillis(REQUEST_DURATION_MILLIS)
            .setMaxUpdateAgeMillis(MAX_UPDATE_AGE_MILLIS)
            .build()
    }
}
