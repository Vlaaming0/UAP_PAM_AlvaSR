// PlantApiService.kt
package com.example.uap_pam_alvasr

import retrofit2.Call
import retrofit2.http.*

interface PlantApiService {
    @GET("plant/all")
    fun getAllPlants(): Call<PlantListResponse>

    @GET("plant/{name}")
    fun getPlant(@Path("name") name: String): Call<PlantResponse>

    @POST("plant/new")
    fun createPlant(@Body body: PlantCreateRequest): Call<PlantResponse>

    @PUT("plant/{name}")
    fun updatePlant(
        @Path("name") name: String,
        @Body body: PlantUpdateRequest
    ): Call<PlantResponse>

    @DELETE("plant/{name}")
    fun deletePlant(@Path("name") name: String): Call<GenericResponse>
}

data class PlantCreateRequest(
    val plant_name: String,
    val description: String,
    val price: String
)

data class PlantUpdateRequest(
    val plant_name: String,
    val description: String,
    val price: String
)
