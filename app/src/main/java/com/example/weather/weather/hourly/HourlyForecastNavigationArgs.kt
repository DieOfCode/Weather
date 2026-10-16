package com.example.weather.weather.hourly

import android.os.Bundle
import androidx.core.os.bundleOf
import java.time.LocalDate

internal data class HourlyForecastNavigationArgs(
    val cityId: String,
    val date: LocalDate,
) {

    fun toBundle(): Bundle = bundleOf(
        CITY_ID_KEY to cityId,
        DATE_EPOCH_DAY_KEY to date.toEpochDay(),
    )

    companion object {
        const val CITY_ID_KEY = "cityId"
        const val DATE_EPOCH_DAY_KEY = "dateEpochDay"

        fun from(bundle: Bundle): HourlyForecastNavigationArgs {
            val cityId = requireNotNull(bundle.getString(CITY_ID_KEY)) {
                "Navigation argument '$CITY_ID_KEY' is missing"
            }
            require(cityId.isNotBlank()) {
                "Navigation argument '$CITY_ID_KEY' must not be blank"
            }
            require(bundle.containsKey(DATE_EPOCH_DAY_KEY)) {
                "Navigation argument '$DATE_EPOCH_DAY_KEY' is missing"
            }

            return HourlyForecastNavigationArgs(
                cityId = cityId,
                date = LocalDate.ofEpochDay(bundle.getLong(DATE_EPOCH_DAY_KEY)),
            )
        }
    }
}
