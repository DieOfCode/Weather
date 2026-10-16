package com.example.weather.weather.citypicker

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.databinding.FragmentCityPickerBinding
import com.example.weather.ui.insets.applyScreenInsets
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CityPickerFragment : Fragment(R.layout.fragment_city_picker) {

    private var _binding: FragmentCityPickerBinding? = null
    private val binding: FragmentCityPickerBinding
        get() = checkNotNull(_binding)

    private val cityPickerViewModel: CityPickerViewModel by viewModel()

    private lateinit var cityAdapter: CityAdapter

    private var isLocationPermissionRequestInProgress = false

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        isLocationPermissionRequestInProgress = false

        if (isGranted) {
            cityPickerViewModel.onLocationPermissionGranted()
        } else {
            cityPickerViewModel.onLocationPermissionDenied()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCityPickerBinding.bind(view)

        binding.root.applyScreenInsets(
            topBar = binding.toolbar,
            bottomContent = binding.cityRecyclerView,
            includeIme = true,
        )

        setupToolbar()
        setupCityList()
        setupSearch()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { navigateBack() }
    }

    private fun setupCityList() {
        cityAdapter = CityAdapter(
            listener = object : CityAdapterListener {
                override fun onCurrentLocationClick() {
                    requestCurrentLocationWeather()
                }

                override fun onCityClick(cityId: String) {
                    cityPickerViewModel.selectCity(cityId)
                }

                override fun onDeleteCityClick(cityId: String) {
                    cityPickerViewModel.deleteCity(cityId)
                }

                override fun onRetryClick(section: CityListSection) {
                    if (section == CityListSection.SearchResults) {
                        cityPickerViewModel.retrySearch()
                    }
                }
            },
        )

        binding.cityRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cityRecyclerView.adapter = cityAdapter
    }

    private fun setupSearch() {
        binding.searchEditText.doAfterTextChanged { editable ->
            cityPickerViewModel.onQueryChange(
                query = editable?.toString().orEmpty(),
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    cityPickerViewModel.state.collect { state ->
                        render(state)
                    }
                }

                launch {
                    cityPickerViewModel.events.collect { event ->
                        handleEvent(event)
                    }
                }
            }
        }
    }

    private fun render(state: CityPickerScreenState) {
        cityAdapter.submitList(state.items)

        val currentQuery = binding.searchEditText.text?.toString().orEmpty()
        if (currentQuery != state.query) {
            binding.searchEditText.setText(state.query)
            binding.searchEditText.setSelection(state.query.length)
        }

        binding.operationProgressIndicator.isVisible = state.isOperationInProgress
        binding.searchInputLayout.isEnabled = !state.isOperationInProgress
        binding.cityRecyclerView.isEnabled = !state.isOperationInProgress
        binding.cityRecyclerView.alpha = if (state.isOperationInProgress) {
            DISABLED_CONTENT_ALPHA
        } else {
            ENABLED_CONTENT_ALPHA
        }
    }

    private fun handleEvent(event: CityPickerEvent) {
        when (event) {
            CityPickerEvent.NavigateBack -> navigateBack()

            is CityPickerEvent.ShowMessage -> Snackbar.make(
                binding.root,
                event.messageResId,
                Snackbar.LENGTH_LONG,
            ).show()
        }
    }

    private fun requestCurrentLocationWeather() {
        if (
            isLocationPermissionRequestInProgress ||
            cityPickerViewModel.state.value.isOperationInProgress
        ) {
            return
        }

        val isPermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (isPermissionGranted) {
            cityPickerViewModel.onLocationPermissionGranted()
        } else {
            isLocationPermissionRequestInProgress = true
            locationPermissionLauncher.launch(ACCESS_COARSE_LOCATION)
        }
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        binding.cityRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val ENABLED_CONTENT_ALPHA = 1f
        const val DISABLED_CONTENT_ALPHA = 0.5f
    }
}
