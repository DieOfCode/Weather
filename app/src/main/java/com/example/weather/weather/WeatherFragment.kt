package com.example.weather.weather

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.weather.R
import com.example.weather.databinding.FragmentWeatherBinding
import com.example.weather.ui.insets.applyScreenInsets
import com.example.weather.weather.hourly.HourlyForecastNavigationArgs
import com.example.weather.ui.theme.WeatherTheme
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class WeatherFragment : Fragment(R.layout.fragment_weather) {

    private var _binding: FragmentWeatherBinding? = null
    private val binding: FragmentWeatherBinding
        get() = checkNotNull(_binding)

    private val weatherViewModel: WeatherViewModel by viewModel()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            weatherViewModel.loadWeatherForCurrentLocation()
        } else {
            weatherViewModel.onLocationPermissionDenied()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWeatherBinding.bind(view)

        binding.root.applyScreenInsets(
            bottomContent = binding.composeView,
        )

        binding.composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
        binding.composeView.setContent {
            val weatherViewState: WeatherScreenState by weatherViewModel.state.collectAsStateWithLifecycle()

            WeatherTheme {
                WeatherScreen(
                    state = weatherViewState,
                    onChooseCity = {
                        findNavController().navigate(
                            R.id.action_weatherFragment_to_cityPickerFragment,
                        )
                    },
                    onDayClick = { day ->
                        (weatherViewState as? WeatherScreenState.Content)
                            ?.weatherState
                            ?.let { weatherState ->
                                findNavController().navigate(
                                    R.id.action_weatherFragment_to_hourlyForecastFragment,
                                    HourlyForecastNavigationArgs(
                                        cityId = weatherState.cityId,
                                        date = day.date,
                                    ).toBundle(),
                                )
                            }
                    },
                    onRetry = {
                        when ((weatherViewState as? WeatherScreenState.Error)?.retryAction) {
                            WeatherRetryAction.RequestLocationPermission -> {
                                requestCurrentLocationWeather()
                            }

                            WeatherRetryAction.RefreshWeather,
                            null,
                            -> weatherViewModel.retry()
                        }
                    },
                    onRefresh = { weatherViewModel.refreshWeather() },
                )
            }
        }

        observeInitialLocationRequest()
    }

    private fun observeInitialLocationRequest() {
        viewLifecycleOwner.lifecycleScope.launch {
            weatherViewModel.state
                .flowWithLifecycle(
                    lifecycle = viewLifecycleOwner.lifecycle,
                    minActiveState = Lifecycle.State.STARTED,
                )
                .filter { state ->
                    state == WeatherScreenState.NoCitySelected
                }
                .take(1)
                .collect {
                    requestCurrentLocationWeather()
                }
        }
    }

    private fun requestCurrentLocationWeather() {
        val isPermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (isPermissionGranted) {
            weatherViewModel.loadWeatherForCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                ACCESS_COARSE_LOCATION,
            )
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
