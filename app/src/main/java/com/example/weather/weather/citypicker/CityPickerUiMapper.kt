package com.example.weather.weather.citypicker

import com.example.weather.weather.domain.model.City

internal fun City.toCityListItem(
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
