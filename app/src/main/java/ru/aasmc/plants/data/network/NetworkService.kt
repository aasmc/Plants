package ru.aasmc.plants.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import ru.aasmc.plants.data.model.GrowZone
import ru.aasmc.plants.data.model.Plant

private const val BASE_URL = "https://raw.githubusercontent.com/"

class NetworkService {

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val sunflowerService = retrofit.create(SunflowerService::class.java)

    suspend fun allPlants(): List<Plant> = withContext(Dispatchers.Default) {
        val result = sunflowerService.getAllPlants()
        result.shuffled()
    }

    suspend fun plantsByGrowZone(growZone: GrowZone) = withContext(Dispatchers.Default) {
        val result = sunflowerService.getAllPlants()
        result.filter { it.growZoneNumber == growZone.number }.shuffled()
    }

    suspend fun customPlantSortOrder(): List<String> = withContext(Dispatchers.Default) {
        val result = sunflowerService.getCustomPlantSortOrder()
        result.map { plant -> plant.plantId }
    }

}


interface SunflowerService {
    @GET("googlecodelabs/kotlin-coroutines/master/advanced-coroutines-codelab/sunflower/src/main/assets/plants.json")
    suspend fun getAllPlants(): List<Plant>

    @GET("googlecodelabs/kotlin-coroutines/master/advanced-coroutines-codelab/sunflower/src/main/assets/custom_plant_sort_order.json")
    suspend fun getCustomPlantSortOrder(): List<Plant>
}

























