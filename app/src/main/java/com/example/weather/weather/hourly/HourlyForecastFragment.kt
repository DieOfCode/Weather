package com.example.weather.weather.hourly

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.databinding.FragmentHourlyForecastBinding
import com.example.weather.ui.insets.applyScreenInsets

class HourlyForecastFragment : Fragment(R.layout.fragment_hourly_forecast) {

    private var _binding: FragmentHourlyForecastBinding? = null
    private val binding: FragmentHourlyForecastBinding
        get() = checkNotNull(_binding)

    private lateinit var navigationArgs: HourlyForecastNavigationArgs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigationArgs = HourlyForecastNavigationArgs.from(requireArguments())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHourlyForecastBinding.bind(view)

        binding.root.applyScreenInsets(
            topBar = binding.toolbar,
            bottomContent = binding.hourlyRecyclerView,
        )

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.hourlyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
