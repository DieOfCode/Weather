package com.example.weather.weather

import androidx.annotation.StringRes

sealed interface WeatherScreenState {

    data object Loading : WeatherScreenState

    data object NoCitySelected : WeatherScreenState

    data class Content(
        val weatherState: WeatherUiState,
        val isRefreshing: Boolean = false,
        @StringRes val refreshErrorResId: Int? = null,
    ) : WeatherScreenState

    data class Error(
        @StringRes val messageResId: Int,
    ) : WeatherScreenState
}
