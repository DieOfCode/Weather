package com.example.weather.weather.citypicker

import androidx.annotation.StringRes

internal sealed interface CityPickerEvent {

    data object NavigateBack : CityPickerEvent

    data class ShowMessage(
        @StringRes val messageResId: Int,
    ) : CityPickerEvent
}
