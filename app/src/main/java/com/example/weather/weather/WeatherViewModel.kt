package com.example.weather.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel(
    weatherRepository: WeatherRepository,
) : ViewModel() {

    private val selectedCityState: StateFlow<SelectedCityState> = weatherRepository
        .observeSelectedCity()
        .map<City?, SelectedCityState> { city ->
            SelectedCityState.Loaded(city)
        }
        .catch { cause ->
            emit(SelectedCityState.Failed(cause))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SelectedCityState.Loading,
        )

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
