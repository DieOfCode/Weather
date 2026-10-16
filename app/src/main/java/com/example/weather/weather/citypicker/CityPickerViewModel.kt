package com.example.weather.weather.citypicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.R
import com.example.weather.weather.domain.model.City
import com.example.weather.weather.domain.repository.WeatherRepository
import com.example.weather.weather.domain.repository.WeatherRepositoryException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class CityPickerViewModel(
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

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

    val state: StateFlow<CityPickerScreenState> = combine(
        _query,
        weatherRepository.observeSelectedCity(),
        weatherRepository.observeSavedCities(),
        searchState,
    ) { query, selectedCity, savedCities, search ->
        CityPickerScreenState(
            query = query,
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

    init {
        observeSearchQuery()
    }

    fun onQueryChange(query: String) {
        _query.value = query
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            _query
                .map(String::trim)
                .distinctUntilChanged()
                .collectLatest { normalizedQuery ->
                    if (normalizedQuery.length < MIN_QUERY_LENGTH) {
                        _searchState.value = SearchState.Idle
                        return@collectLatest
                    }

                    _searchState.value = SearchState.Idle

                    delay(SEARCH_DELAY_MILLIS)

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
                    city.toListItem(
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
                            city.toListItem(
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

    private fun City.toListItem(
        section: CityListSection,
        selectedCityId: String?,
        canDelete: Boolean,
    ): CityListItem.City = CityListItem.City(
        section = section,
        cityId = id,
        name = name,
        countryCode = countryCode,
        isSelected = id == selectedCityId,
        canDelete = canDelete,
    )

    private companion object {
        const val MIN_QUERY_LENGTH = 2
        const val SEARCH_DELAY_MILLIS = 400L
    }
}
