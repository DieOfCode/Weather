package com.example.weather.weather.citypicker

import androidx.annotation.StringRes

internal sealed interface CityListItem {

    val itemKey: String

    data object CurrentLocation : CityListItem {
        override val itemKey: String = "action:current-location"
    }

    data class Section(
        val section: CityListSection,
        @StringRes val titleResId: Int,
    ) : CityListItem {
        override val itemKey: String = "section:${section.key}"
    }

    data class City(
        val section: CityListSection,
        val cityId: String,
        val name: String,
        val countryCode: String,
        val isSelected: Boolean,
        val canDelete: Boolean,
    ) : CityListItem {
        override val itemKey: String = "city:${section.key}:$cityId"
    }

    sealed interface Status : CityListItem {

        val section: CityListSection

        data class Loading(
            override val section: CityListSection,
        ) : Status {
            override val itemKey: String = "status:${section.key}:loading"
        }

        data class Empty(
            override val section: CityListSection,
            @StringRes val messageResId: Int,
        ) : Status {
            override val itemKey: String = "status:${section.key}:empty"
        }

        data class Error(
            override val section: CityListSection,
            @StringRes val messageResId: Int,
        ) : Status {
            override val itemKey: String = "status:${section.key}:error"
        }
    }
}

internal enum class CityListSection(
    val key: String,
) {
    Saved(key = "saved"),
    SearchResults(key = "search"),
}
