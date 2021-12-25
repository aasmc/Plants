package ru.aasmc.plants.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import ru.aasmc.plants.data.cache.PlantDao
import ru.aasmc.plants.data.model.GrowZone
import ru.aasmc.plants.data.network.NetworkService

/**
 * Repository that handles all data operations.
 *
 * It exposes two UI-observable database queries [plants] and
 * [getPlantsWithGrowZone].
 *
 * To update the plants cache, call [tryUpdateRecentPlantsForGrowZoneCache] or
 * [tryUpdateRecentPlantsCache].
 */
class PlantRepository private constructor(
    private val plantDao: PlantDao,
    private val plantService: NetworkService,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    /**
     * Fetch a list of [Plant]s from the database.
     * Returns a LiveData-wrapped List of Plants.
     */
    val plants = plantDao.getPlants()

    /**
     * Fetch a list of [Plant]s from the db, that matches a goven [GrowZone].
     * Returns a LiveData-wrapped List of Plants.
     */
    fun getPlantsWithGrowZone(growZone: GrowZone) =
        plantDao.getPlantsWithGrowZoneNumber(growZone.number)

    /**
     * Returns true, if we should make a network request.
     */
    private fun shouldUpdatePlantsCache(): Boolean {
        // here you can check, e.g. the status of the db
        return true
    }

    /**
     * Update the plants cache.
     *
     * This function may decide to avoid making a network request on every call
     * based on a cache-invalidation policy.
     */
    suspend fun tryUpdateRecentPlantsCache() {
        if (shouldUpdatePlantsCache()) fetchRecentPlants()
    }

    /**
     * Update the plants cache for a specific grow zone.
     *
     * This function may decide to avoid making a network request on every call
     * based on a cache-invalidation policy.
     */
    suspend fun tryUpdateRecentPlantsForGrowZoneCache(growZoneNumber: GrowZone) {
        if (shouldUpdatePlantsCache()) fetchPlantsForGrowZone(growZoneNumber)
    }

    /**
     * Fetch a new list of plants from the network, and append them to [plantDao]
     */
    private suspend fun fetchRecentPlants() {
        val plants = plantService.allPlants()
        plantDao.insertAll(plants)
    }

    /**
     * Fetch a list of plants for a grow zone number from the network,
     * and append them to the [plantDao].
     */
    private suspend fun fetchPlantsForGrowZone(growZoneNumber: GrowZone) {
        val plants = plantService.plantsByGrowZone(growZoneNumber)
        plantDao.insertAll(plants)
    }

    companion object {
        @Volatile
        private var INSTANCE: PlantRepository? = null
        fun getInstance(plantDao: PlantDao, plantService: NetworkService) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlantRepository(plantDao, plantService).also { INSTANCE = it }
            }
    }

}

























