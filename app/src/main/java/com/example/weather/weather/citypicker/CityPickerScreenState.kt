package com.example.weather.weather.citypicker

internal data class CityPickerScreenState(
    val query: String = "",
    val items: List<CityListItem> = emptyList(),
    val isOperationInProgress: Boolean = false,
)
