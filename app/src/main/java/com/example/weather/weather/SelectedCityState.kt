package com.example.weather.weather

import com.example.weather.weather.domain.model.City

internal sealed interface SelectedCityState {

    data object Loading : SelectedCityState

    data class Loaded(
        val city: City?,
    ) : SelectedCityState

    data class Failed(
        val cause: Throwable,
    ) : SelectedCityState
}
