package com.example.weather.weather

import androidx.annotation.StringRes

internal sealed interface LocationLoadState {

    data object Idle : LocationLoadState

    data object Loading : LocationLoadState

    data class Failed(
        @StringRes val messageResId: Int,
    ) : LocationLoadState
}
