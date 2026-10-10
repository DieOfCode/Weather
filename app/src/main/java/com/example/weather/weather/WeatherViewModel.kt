package com.example.weather.weather

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.R
import com.example.weather.weather.domain.location.DeviceLocationException
import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.model.WeatherForecast
import com.example.weather.weather.domain.repository.WeatherRepository
import com.example.weather.weather.domain.repository.WeatherRepositoryException
import com.example.weather.weather.domain.usecase.LoadWeatherForCurrentLocationUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val loadWeatherForCurrentLocationUseCase: LoadWeatherForCurrentLocationUseCase,
) : ViewModel() {

    private val refreshState = MutableStateFlow(RefreshState())
    private val locationLoadState = MutableStateFlow<LocationLoadState>(LocationLoadState.Idle)
    private var refreshJob: Job? = null
    private var locationJob: Job? = null

    private val selectedCityState: StateFlow<SelectedCityState> =
        weatherRepository.observeSelectedCity().map<City?, SelectedCityState> { city ->
            SelectedCityState.Loaded(city)
        }.catch { cause ->
            emit(SelectedCityState.Failed(cause))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SelectedCityState.Loading,
        )

    private val weatherDataState: StateFlow<WeatherDataState> =
        selectedCityState.flatMapLatest { selectedCity ->
            when (selectedCity) {
                SelectedCityState.Loading -> flowOf(WeatherDataState.Loading)

                is SelectedCityState.Failed -> flowOf(
                    WeatherDataState.Failed(selectedCity.cause),
                )

                is SelectedCityState.Loaded -> {
                    val city = selectedCity.city

                    if (city == null) {
                        flowOf(WeatherDataState.NoCity)
                    } else {
                        weatherRepository.observeWeather(city.id)
                            .map<WeatherForecast?, WeatherDataState> { forecast ->
                                WeatherDataState.CityWeather(
                                    city = city,
                                    forecast = forecast,
                                )
                            }.catch { cause ->
                                emit(WeatherDataState.Failed(cause))
                            }
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = WeatherDataState.Loading,
        )


    val state: StateFlow<WeatherScreenState> = combine(
        weatherDataState,
        refreshState,
        locationLoadState,
        ::mapToScreenState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeatherScreenState.Loading,
    )

    private fun mapToScreenState(
        weatherDataState: WeatherDataState,
        refreshState: RefreshState,
        locationLoadState: LocationLoadState,
    ): WeatherScreenState = when (weatherDataState) {
        is WeatherDataState.CityWeather -> {
            val forecast = weatherDataState.forecast
            val isCurrentCityRefresh = refreshState.cityId == weatherDataState.city.id

            if (forecast == null) {
                val refreshErrorResId = refreshState.errorResId

                if (isCurrentCityRefresh && refreshErrorResId != null) {
                    WeatherScreenState.Error(refreshErrorResId)
                } else {
                    WeatherScreenState.Loading
                }
            } else {
                WeatherScreenState.Content(
                    weatherState = WeatherUiMapper.forecastToUiState(forecast),
                    isRefreshing = isCurrentCityRefresh && refreshState.isRefreshing,
                    refreshErrorResId = refreshState.errorResId.takeIf { isCurrentCityRefresh },
                )
            }
        }

        is WeatherDataState.Failed -> WeatherScreenState.Error(R.string.generic_error)
        WeatherDataState.Loading -> WeatherScreenState.Loading
        WeatherDataState.NoCity -> when (locationLoadState) {
            is LocationLoadState.Failed -> WeatherScreenState.Error(
                messageResId = locationLoadState.messageResId,
            )

            LocationLoadState.Idle -> WeatherScreenState.NoCitySelected
            LocationLoadState.Loading -> WeatherScreenState.Loading
        }
    }

    init {
        viewModelScope.launch {
            selectedCityState.map { selectedCity ->
                (selectedCity as? SelectedCityState.Loaded)?.city
            }.distinctUntilChangedBy { city -> city?.id }.filterNotNull().collect { city ->
                refreshWeather(city)
            }
        }
    }

    private fun refreshWeather(city: City) {
        val isCurrentCityRefreshing =
            refreshState.value.cityId == city.id && refreshJob?.isActive == true

        if (isCurrentCityRefreshing) return

        refreshJob?.cancel()
        refreshState.value = RefreshState(
            cityId = city.id,
            isRefreshing = true,
        )

        refreshJob = viewModelScope.launch {
            try {
                weatherRepository.refreshWeather(city)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: WeatherRepositoryException) {
                setRefreshError(
                    cityId = city.id,
                    errorResId = exception.toMessageResId(),
                )
            } catch (exception: Exception) {
                setRefreshError(
                    cityId = city.id,
                    errorResId = R.string.generic_error,
                )
            } finally {
                refreshState.update { currentState ->
                    if (currentState.cityId == city.id) {
                        currentState.copy(isRefreshing = false)
                    } else {
                        currentState
                    }
                }
            }
        }
    }

    private fun setRefreshError(
        cityId: String,
        @StringRes errorResId: Int,
    ) {
        refreshState.update { currentState ->
            if (currentState.cityId == cityId) {
                currentState.copy(errorResId = errorResId)
            } else {
                currentState
            }
        }
    }

    @StringRes
    private fun WeatherRepositoryException.toMessageResId(): Int = when (this) {
        is WeatherRepositoryException.Network -> R.string.weather_network_error
        is WeatherRepositoryException.Service -> R.string.weather_service_error
        is WeatherRepositoryException.InvalidData -> R.string.weather_invalid_data_error
    }

    fun refreshWeather() {
        val city = (selectedCityState.value as? SelectedCityState.Loaded)?.city ?: return
        refreshWeather(city)
    }

    fun retry() {
        val selectedCity = (selectedCityState.value as? SelectedCityState.Loaded)?.city

        if (selectedCity == null) {
            loadWeatherForCurrentLocation()
        } else {
            refreshWeather(selectedCity)
        }
    }

    fun loadWeatherForCurrentLocation() {
        if (locationJob?.isActive == true) return

        locationLoadState.value = LocationLoadState.Loading
        locationJob = viewModelScope.launch {
            try {
                loadWeatherForCurrentLocationUseCase()
                locationLoadState.value = LocationLoadState.Idle
            } catch (exception: CancellationException) {
                locationLoadState.value = LocationLoadState.Idle
                throw exception
            } catch (exception: DeviceLocationException) {
                locationLoadState.value = LocationLoadState.Failed(
                    messageResId = exception.toMessageResId(),
                )
            } catch (exception: WeatherRepositoryException) {
                locationLoadState.value = LocationLoadState.Failed(
                    messageResId = exception.toMessageResId(),
                )
            } catch (exception: Exception) {
                locationLoadState.value = LocationLoadState.Failed(
                    messageResId = R.string.generic_error,
                )
            }
        }
    }

    @StringRes
    private fun DeviceLocationException.toMessageResId(): Int = when (this) {
        is DeviceLocationException.LocationUnavailable,
        is DeviceLocationException.ProviderUnavailable,
        -> R.string.location_unavailable

        is DeviceLocationException.PermissionMissing -> R.string.location_permission_missing
    }
}
