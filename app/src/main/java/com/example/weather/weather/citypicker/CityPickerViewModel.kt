package com.example.weather.weather.citypicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.R
import com.example.weather.weather.domain.location.DeviceLocationException
import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.repository.WeatherRepository
import com.example.weather.weather.domain.repository.WeatherRepositoryException
import com.example.weather.weather.domain.usecase.LoadWeatherForCurrentLocationUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

internal class CityPickerViewModel(
    private val weatherRepository: WeatherRepository,
    private val loadWeatherForCurrentLocationUseCase: LoadWeatherForCurrentLocationUseCase,
) : ViewModel() {

    private var searchJob: Job? = null
    private var operationJob: Job? = null

    private sealed interface SearchState {

        data object Idle : SearchState
        data object Loading : SearchState
        data class Content(
            val cities: List<City>,
        ) : SearchState

        data object Error : SearchState
    }

    private val _query = MutableStateFlow("")

    private val _searchState =
        MutableStateFlow<SearchState>(SearchState.Idle)

    private val searchState: StateFlow<SearchState> =
        _searchState.asStateFlow()

    private val _isOperationInProgress = MutableStateFlow(false)

    private val _events = Channel<CityPickerEvent>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val selectedCity = weatherRepository.observeSelectedCity().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    private val savedCities = weatherRepository.observeSavedCities().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList(),
    )

    val state: StateFlow<CityPickerScreenState> = combine(
        _query,
        selectedCity,
        savedCities,
        searchState,
        _isOperationInProgress,
    ) { query, selectedCity, savedCities, search, isOperationInProgress ->
        CityPickerScreenState(
            query = query,
            isOperationInProgress = isOperationInProgress,
            items = buildItems(
                query = query,
                selectedCity = selectedCity,
                savedCities = savedCities,
                search = search,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CityPickerScreenState(
            items = listOf(CityListItem.CurrentLocation),
        ),
    )

    fun onQueryChange(query: String) {
        if (_query.value == query) {
            return
        }

        _query.value = query
        startSearch(
            rawQuery = query,
            useDebounce = true,
        )
    }

    fun retrySearch() {
        if (_searchState.value != SearchState.Error) {
            return
        }

        if (searchJob?.isActive == true) {
            return
        }

        startSearch(
            rawQuery = _query.value,
            useDebounce = false,
        )
    }

    fun selectCity(cityId: String) {
        if (cityId.isBlank()) {
            _events.trySend(
                CityPickerEvent.ShowMessage(R.string.generic_error),
            )
            return
        }

        val isSavedCity = savedCities.value.any { city ->
            city.id == cityId
        }
        val searchCity = (_searchState.value as? SearchState.Content)
            ?.cities
            ?.firstOrNull { city -> city.id == cityId }

        if (!isSavedCity && searchCity == null) {
            _events.trySend(
                CityPickerEvent.ShowMessage(R.string.generic_error),
            )
            return
        }

        launchOperation {
            if (!isSavedCity) {
                weatherRepository.refreshWeather(checkNotNull(searchCity))
            }

            weatherRepository.selectCity(cityId)
            _events.send(CityPickerEvent.NavigateBack)
        }
    }

    fun deleteCity(cityId: String) {
        if (cityId.isBlank()) {
            return
        }

        launchOperation {
            weatherRepository.deleteCity(cityId)
        }
    }

    fun onLocationPermissionGranted() {
        launchOperation {
            loadWeatherForCurrentLocationUseCase()
            _events.send(CityPickerEvent.NavigateBack)
        }
    }

    fun onLocationPermissionDenied() {
        _events.trySend(
            CityPickerEvent.ShowMessage(R.string.location_permission_missing),
        )
    }

    private fun launchOperation(
        operation: suspend () -> Unit,
    ) {
        if (operationJob?.isActive == true) {
            return
        }

        operationJob = viewModelScope.launch {
            _isOperationInProgress.value = true

            try {
                operation()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: DeviceLocationException) {
                _events.send(
                    CityPickerEvent.ShowMessage(exception.toMessageResId()),
                )
            } catch (exception: WeatherRepositoryException) {
                _events.send(
                    CityPickerEvent.ShowMessage(exception.toMessageResId()),
                )
            } catch (_: Exception) {
                _events.send(
                    CityPickerEvent.ShowMessage(R.string.generic_error),
                )
            } finally {
                _isOperationInProgress.value = false
            }
        }
    }

    private fun DeviceLocationException.toMessageResId(): Int = when (this) {
        is DeviceLocationException.LocationUnavailable,
        is DeviceLocationException.ProviderUnavailable,
            -> R.string.location_unavailable

        is DeviceLocationException.PermissionMissing -> R.string.location_permission_missing
    }

    private fun WeatherRepositoryException.toMessageResId(): Int = when (this) {
        is WeatherRepositoryException.Network -> R.string.weather_network_error
        is WeatherRepositoryException.Service -> R.string.weather_service_error
        is WeatherRepositoryException.InvalidData -> R.string.weather_invalid_data_error
    }

    private fun startSearch(
        rawQuery: String,
        useDebounce: Boolean,
    ) {
        searchJob?.cancel()

        val normalizedQuery = rawQuery.trim()
        if (normalizedQuery.length < MIN_QUERY_LENGTH) {
            searchJob = null
            _searchState.value = SearchState.Idle
            return
        }

        _searchState.value = SearchState.Idle

        searchJob = viewModelScope.launch {
            if (useDebounce) {
                delay(SEARCH_DELAY_MILLIS)
            }

            _searchState.value = SearchState.Loading

            _searchState.value = try {
                val cities = weatherRepository.searchCities(
                    query = normalizedQuery,
                )

                SearchState.Content(cities)
            } catch (_: WeatherRepositoryException) {
                SearchState.Error
            }
        }
    }

    private fun buildItems(
        query: String,
        selectedCity: City?,
        savedCities: List<City>,
        search: SearchState,
    ): List<CityListItem> = buildList {
        add(CityListItem.CurrentLocation)

        if (savedCities.isNotEmpty()) {
            add(
                CityListItem.Section(
                    section = CityListSection.Saved,
                    titleResId = R.string.saved_cities,
                ),
            )

            savedCities.forEach { city ->
                add(
                    city.toCityListItem(
                        section = CityListSection.Saved,
                        selectedCityId = selectedCity?.id,
                        canDelete = true,
                    ),
                )
            }
        }

        if (query.trim().length < MIN_QUERY_LENGTH) {
            return@buildList
        }

        add(
            CityListItem.Section(
                section = CityListSection.SearchResults,
                titleResId = R.string.search_results,
            ),
        )

        when (search) {
            SearchState.Idle -> Unit

            SearchState.Loading -> add(
                CityListItem.Status.Loading(
                    section = CityListSection.SearchResults,
                ),
            )

            SearchState.Error -> add(
                CityListItem.Status.Error(
                    section = CityListSection.SearchResults,
                    messageResId = R.string.city_search_error,
                ),
            )

            is SearchState.Content -> {
                val cities = search.cities.distinctBy(City::id)

                if (cities.isEmpty()) {
                    add(
                        CityListItem.Status.Empty(
                            section = CityListSection.SearchResults,
                            messageResId = R.string.city_search_empty,
                        ),
                    )
                } else {
                    cities.forEach { city ->
                        add(
                            city.toCityListItem(
                                section = CityListSection.SearchResults,
                                selectedCityId = selectedCity?.id,
                                canDelete = false,
                            ),
                        )
                    }
                }
            }
        }
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 2
        const val SEARCH_DELAY_MILLIS = 400L
    }
}
