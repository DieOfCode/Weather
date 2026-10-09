package com.example.weather.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val _state = MutableStateFlow<WeatherScreenState>(WeatherScreenState.Loading)
    val state: StateFlow<WeatherScreenState> = _state.asStateFlow()

    private var refreshJob: Job? = null

    init {
        loadWeather()
    }

    fun loadWeather() {
        viewModelScope.launch {
            _state.value = WeatherScreenState.Loading
            delay(1_000)
            _state.value = WeatherScreenState.Content(sampleWeatherUiState)
        }
    }

    fun refreshWeather() {
        val currentState = _state.value as? WeatherScreenState.Content ?: return
        if (currentState.isRefreshing || refreshJob?.isActive == true) return

        _state.value = currentState.copy(
            isRefreshing = true,
            refreshErrorResId = null,
        )

        refreshJob = viewModelScope.launch {
            delay(1_000)

            _state.update { latestState ->
                if (latestState is WeatherScreenState.Content) {
                    latestState.copy(isRefreshing = false)
                } else {
                    latestState
                }
            }
        }
    }

    fun retry() {
        loadWeather()
    }
}
