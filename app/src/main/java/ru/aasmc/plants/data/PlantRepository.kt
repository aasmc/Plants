package ru.aasmc.plants.data

import androidx.annotation.AnyThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import ru.aasmc.plants.data.cache.PlantDao
import ru.aasmc.plants.data.model.GrowZone
import ru.aasmc.plants.data.model.Plant
import ru.aasmc.plants.data.network.NetworkService
import ru.aasmc.plants.util.CacheOnSuccess
import ru.aasmc.plants.util.ComparablePair

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
    val plants: LiveData<List<Plant>> = liveData {
        val plantsLiveData = plantDao.getPlants()
        val customSortOrder = plantsListSortOrderCache.getOrAwait()
        emitSource(plantsLiveData.map { plantList ->
            plantList.applySort(customSortOrder)
        })
    }

    val plantsFlow: Flow<List<Plant>>
        get() = plantDao.getPlantsFlow()
            // when the result of customSortFlow is available
            // this will combine it with the latest value form
            // the flow above. thus, as long as both plants
            // sortOrder have an initial value (their flow
            // has emitted at leas one value), any change
            // to either plants or sortOrder will call
            // plants.applySort(sortOrder).
            .combine(customSortFlow) { plants, sortOrder ->
                plants.applySort(sortOrder)
            }
            // The operator flowOn launches a new coroutine to collect the
            // flow above it and introduces a buffer to write the results.
            // You can control the buffer with more operators, such as
            // conflate which says to store only the last value produced in the buffer.
            .flowOn(defaultDispatcher)
            // Conflates flow emissions via conflated channel and runs collector
            // in a separate coroutine. The effect of this is that emitter is never
            // suspended due to a slow collector, but collector always gets the most
            // recent value emitted.
            .conflate()

    /**
     * Defines a Flow that, when collected, will call getOrAwait and emit the
     * sort order.
     */
    private val customSortFlow = flow {
        emit(plantsListSortOrderCache.getOrAwait())
    }

    /**
     * Fetches the custom sort order from the network and then caches it in memory.
     * Falls back to empty list if there's a network error.
     */
    private var plantsListSortOrderCache =
        CacheOnSuccess(onErrorFallback = { listOf<String>() }) {
            plantService.customPlantSortOrder()
        }

    /**
     * Rearranges the list, placing Plants that are in the customSortOrder at the front
     * of the list.
     */
    private fun List<Plant>.applySort(customSortOrder: List<String>): List<Plant> {
        return sortedBy { plant ->
            val positionForItem = customSortOrder.indexOf(plant.plantId).let { order ->
                if (order > -1) order else Int.MAX_VALUE
            }
            ComparablePair(positionForItem, plant.name)
        }
    }

    @AnyThread
    suspend fun List<Plant>.applyMainSafeSort(customSortOrder: List<String>) =
        withContext(defaultDispatcher) {
            this@applyMainSafeSort.applySort(customSortOrder)
        }

    /**
     * Fetch a list of [Plant]s from the db, that matches a given [GrowZone].
     * Returns a LiveData-wrapped List of Plants.
     */
    fun getPlantsWithGrowZone(growZone: GrowZone): LiveData<List<Plant>> =
        plantDao.getPlantsWithGrowZoneNumber(growZone.number)
            .switchMap { plantList ->
                liveData {
                    val customSortOrder = plantsListSortOrderCache.getOrAwait()
                    emit(plantList.applyMainSafeSort(customSortOrder))
                }
            }

    fun getPlantsWithGrowZoneFlow(growZone: GrowZone): Flow<List<Plant>> {
        return plantDao.getPlantsWithGrowZoneNumberFlow(growZone.number)
            .map { plantList ->
                val sortOrderFromNetwork = plantsListSortOrderCache.getOrAwait()
                val nextValue = plantList.applyMainSafeSort(sortOrderFromNetwork)
                nextValue
            }
    }

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

























