package com.example.weather.weather

sealed interface WeatherScreenState {

    data object Loading : WeatherScreenState

    data class Content(
        val weatherState: WeatherUiState,
        val isRefreshing: Boolean = false,
        val refreshError: String? = null,
    ) : WeatherScreenState

    data class Error(val message: String) : WeatherScreenState
}