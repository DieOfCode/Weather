package com.example.weather.weather

import androidx.annotation.StringRes

internal data class RefreshState(
    val cityId: String? = null,
    val isRefreshing: Boolean = false,
    @StringRes val errorResId: Int? = null,
)
