package com.example.weather.weather.data.remote.geocoding

import com.example.weather.weather.data.remote.geocoding.dto.GeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query


internal interface OpenMeteoGeocodingApi{

    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int,
        @Query("language") language: String,
        @Query(value = "format") format:String = "json"
    ): GeocodingResponseDto

}