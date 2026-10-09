package com.example.weather.weather.data.remote

import com.example.weather.weather.data.remote.geocoding.OpenMeteoGeocodingApi
import com.example.weather.weather.domain.model.City

internal class OpenMeteoCityRemoteDataSource(
    private val geocodingApi: OpenMeteoGeocodingApi,
) : CityRemoteDataSource {

    override suspend fun searchCities(query: String): List<City> {
        val normalizedQuery = query.trim()

        if (normalizedQuery.isEmpty()) {
            return emptyList()
        }

        return executeRemoteRequest {
            val response = geocodingApi.search(
                name = normalizedQuery,
                count = DEFAULT_RESULT_LIMIT,
                language = DEFAULT_LANGUAGE,
            )

            RemoteCityMapper.citiesToDomain(response)
        }
    }

    private companion object {
        const val DEFAULT_RESULT_LIMIT = 10
        const val DEFAULT_LANGUAGE = "ru"
    }
}
