package com.example.weather.weather.citypicker

internal sealed interface CityListPayload {

    data object SelectionChanged : CityListPayload
}
