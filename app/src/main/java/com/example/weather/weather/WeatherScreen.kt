package com.example.weather.weather

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherTheme
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun WeatherScreen(
    state: WeatherScreenState,
    onChooseCity: () -> Unit,
    onDayClick: (DailyWeatherUi) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is WeatherScreenState.Content -> WeatherContent(
            state = state,
            onChooseCity = onChooseCity,
            onDayClick = onDayClick,
            onRefresh = onRefresh,
            modifier = modifier,
        )

        is WeatherScreenState.Error -> WeatherError(
            messageResId = state.messageResId,
            onRetry = onRetry,
            modifier = modifier,
        )

        WeatherScreenState.Loading -> WeatherLoader(
            modifier = modifier,
        )

        WeatherScreenState.NoCitySelected -> WeatherNoCity(
            onChooseCity = onChooseCity,
            modifier = modifier,
        )
    }
}

@Composable
private fun WeatherNoCity(
    modifier: Modifier = Modifier,
    onChooseCity: () -> Unit,
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Text(
            text = stringResource(R.string.no_city_selected_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.no_city_selected_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onChooseCity) {
            Text(stringResource(R.string.choose_city))
        }
    }
}

@Composable
private fun WeatherLoader(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }

}

@Composable
private fun WeatherContent(
    state: WeatherScreenState.Content,
    onRefresh: () -> Unit,
    onChooseCity: () -> Unit,
    onDayClick: (DailyWeatherUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                CityHeader(
                    cityName = state.weatherState.cityName,
                    onChooseCity = onChooseCity,
                )
            }

            item {
                CurrentWeatherCard(state = state.weatherState)
            }

            state.refreshErrorResId?.let { messageResId ->
                item(key = "refresh_error") {
                    RefreshErrorBanner(
                        messageResId = messageResId,
                        onRetry = onRefresh,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.weekly_forecast),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            items(
                items = state.weatherState.dailyForecast,
                key = { day -> day.id },
            ) { day ->
                DailyWeatherRow(
                    day = day,
                    timeZoneId = state.weatherState.timeZoneId,
                    onClick = { onDayClick(day) },
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun RefreshErrorBanner(
    @StringRes messageResId: Int,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(messageResId),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )

            TextButton(onClick = onRetry) {
                Text(text = stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun WeatherError(
    @StringRes messageResId: Int,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 12.dp,
            alignment = Alignment.CenterVertically,
        )
    ) {
        Text(stringResource(messageResId))

        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun CityHeader(
    cityName: String,
    onChooseCity: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = cityName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        TextButton(onClick = onChooseCity) {
            Text(text = stringResource(R.string.change_city))
        }
    }
}

@Composable
private fun CurrentWeatherCard(state: WeatherUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.temperature_value, state.temperature),
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
            )
            Text(
                text = stringResource(state.condition.descriptionResId),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.feels_like, state.feelsLike),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = stringResource(R.string.humidity, state.humidityPercent))
                Text(text = stringResource(R.string.wind_speed, state.windSpeedKmh))
            }
        }
    }
}

@Composable
private fun DailyWeatherRow(
    day: DailyWeatherUi,
    timeZoneId: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = localizedDayLabel(
                    date = day.date,
                    timeZoneId = timeZoneId,
                ),
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = day.condition.emoji,
                fontSize = 24.sp,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Text(
                text = stringResource(
                    R.string.temperature_range,
                    day.minimumTemperature,
                    day.maximumTemperature,
                ),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun localizedDayLabel(
    date: LocalDate,
    timeZoneId: String,
): String {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val today = LocalDate.now(ZoneId.of(timeZoneId))

    return when (date) {
        today -> stringResource(R.string.today)
        today.plusDays(1) -> stringResource(R.string.tomorrow)
        else -> remember(date, locale) {
            date
                .format(DateTimeFormatter.ofPattern("EEEE", locale))
                .replaceFirstChar { firstCharacter ->
                    if (firstCharacter.isLowerCase()) {
                        firstCharacter.titlecase(locale)
                    } else {
                        firstCharacter.toString()
                    }
                }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WeatherScreenPreview() {
    WeatherTheme {
        Surface {
            WeatherScreen(
                state = WeatherScreenState.Content(sampleWeatherUiState),
                onChooseCity = {},
                onDayClick = {},
                onRetry = {},
                onRefresh = {},
            )
        }
    }
}

@Preview(
    name = "Loading",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun WeatherLoadingPreview() {
    WeatherTheme {
        Surface {
            WeatherScreen(
                state = WeatherScreenState.Loading,
                onChooseCity = {},
                onDayClick = {},
                onRetry = {},
                onRefresh = {},
            )
        }
    }
}

@Preview(
    name = "No city — English",
    locale = "en",
    showBackground = true,
    showSystemUi = true,
)
@Composable
private fun WeatherNoCityEnglishPreview() {
    WeatherTheme {
        Surface {
            WeatherScreen(
                state = WeatherScreenState.NoCitySelected,
                onChooseCity = {},
                onDayClick = {},
                onRetry = {},
                onRefresh = {},
            )
        }
    }
}

@Preview(
    name = "No city — Russian",
    locale = "ru",
    showBackground = true,
    showSystemUi = true,
)
@Composable
private fun WeatherNoCityRussianPreview() {
    WeatherTheme {
        Surface {
            WeatherScreen(
                state = WeatherScreenState.NoCitySelected,
                onChooseCity = {},
                onDayClick = {},
                onRetry = {},
                onRefresh = {},
            )
        }
    }
}

@Preview(
    name = "Initial error",
    showBackground = true,
    showSystemUi = true,
)
@Composable
private fun WeatherErrorPreview() {
    WeatherTheme {
        Surface {
            WeatherScreen(
                state = WeatherScreenState.Error(
                    messageResId = R.string.generic_error,
                ),
                onChooseCity = {},
                onDayClick = {},
                onRetry = {},
                onRefresh = {},
            )
        }
    }
}

@Preview(
    name = "Refreshing content",
    showBackground = true,
    showSystemUi = true,
)
@Composable
private fun WeatherRefreshingPreview() {
    WeatherTheme {
        Surface {
            WeatherScreen(
                state = WeatherScreenState.Content(
                    weatherState = sampleWeatherUiState,
                    isRefreshing = true,
                ),
                onChooseCity = {},
                onDayClick = {},
                onRetry = {},
                onRefresh = {},
            )
        }
    }
}

@Preview(
    name = "Refresh error",
    showBackground = true,
    showSystemUi = true,
)
@Composable
private fun WeatherRefreshErrorPreview() {
    WeatherTheme {
        Surface {
            WeatherScreen(
                state = WeatherScreenState.Content(
                    weatherState = sampleWeatherUiState,
                    refreshErrorResId = R.string.weather_update_failed,
                ),
                onChooseCity = {},
                onDayClick = {},
                onRetry = {},
                onRefresh = {},
            )
        }
    }
}
