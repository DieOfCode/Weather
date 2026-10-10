package com.example.weather.weather

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weather.R
import com.example.weather.databinding.FragmentWeatherBinding
import com.example.weather.ui.theme.WeatherTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class WeatherFragment : Fragment(R.layout.fragment_weather) {

    private var _binding: FragmentWeatherBinding? = null
    private val binding: FragmentWeatherBinding
        get() = checkNotNull(_binding)

    private val weatherViewModel: WeatherViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWeatherBinding.bind(view)

        binding.composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
        binding.composeView.setContent {
            val weatherViewState: WeatherScreenState by weatherViewModel.state.collectAsStateWithLifecycle()

            WeatherTheme {
                WeatherScreen(
                    state = weatherViewState,
                    onChooseCity = {},
                    onDayClick = {},
                    onRetry = { weatherViewModel.retry() },
                    onRefresh = { weatherViewModel.refreshWeather() },
                )
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
