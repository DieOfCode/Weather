package com.example.weather.weather.data.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.weather.R
import com.example.weather.weather.domain.location.DeviceLocation
import com.example.weather.weather.domain.location.LocationCityResolver
import com.example.weather.weather.domain.model.City
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

internal class AndroidLocationCityResolver(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher,
) : LocationCityResolver {

    private val applicationContext = context.applicationContext

    override suspend fun resolve(location: DeviceLocation): City {
        val address = try {
            resolveAddress(location)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            null
        }

        return City(
            id = CURRENT_LOCATION_CITY_ID,
            name = address.cityNameOrNull()
                ?: applicationContext.getString(R.string.current_location),
            countryCode = address.countryCodeOrEmpty(),
            latitude = location.latitude,
            longitude = location.longitude,
            timeZoneId = AUTO_TIME_ZONE_ID,
        )
    }

    private suspend fun resolveAddress(location: DeviceLocation): Address? {
        if (!Geocoder.isPresent()) return null

        val locale = applicationContext.resources.configuration.locales[0]
        val geocoder = Geocoder(applicationContext, locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            resolveAddressAsync(geocoder, location)
        } else {
            resolveAddressBlocking(geocoder, location)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun resolveAddressAsync(
        geocoder: Geocoder,
        location: DeviceLocation,
    ): Address? = suspendCancellableCoroutine { continuation ->
        try {
            geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                MAX_RESULTS,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (continuation.isActive) {
                            continuation.resume(addresses.firstOrNull())
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                },
            )
        } catch (exception: Exception) {
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun resolveAddressBlocking(
        geocoder: Geocoder,
        location: DeviceLocation,
    ): Address? = withContext(ioDispatcher) {
        geocoder.getFromLocation(
            location.latitude,
            location.longitude,
            MAX_RESULTS,
        ).orEmpty().firstOrNull()
    }

    private fun Address?.cityNameOrNull(): String? = sequenceOf(
        this?.locality,
        this?.subAdminArea,
        this?.adminArea,
        this?.featureName,
    )
        .mapNotNull { value -> value?.trim()?.takeIf(String::isNotEmpty) }
        .firstOrNull()

    private fun Address?.countryCodeOrEmpty(): String = this
        ?.countryCode
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?.uppercase(Locale.ROOT)
        .orEmpty()

    private companion object {
        const val MAX_RESULTS = 1
        const val CURRENT_LOCATION_CITY_ID = "current-location"
        const val AUTO_TIME_ZONE_ID = "auto"
    }
}
