package ru.aasmc.plants.data.cache

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.aasmc.plants.data.model.Plant

@Dao
interface PlantDao {

    @Query("SELECT * FROM plants ORDER BY name")
    fun getPlants(): LiveData<List<Plant>>

    @Query("SELECT * FROM plants WHERE growZoneNumber = :growZoneNumber ORDER BY name")
    fun getPlantsWithGrowZoneNumber(growZoneNumber: Int): LiveData<List<Plant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plants: List<Plant>)

    /**
     * By specifying a Flow return type, Room executes the query with the following characteristics:
     * - Main-safety. Queries with a Flow return type always run on the Room executors, so they are
     * always main-safe. We don't need to do anything in code to make them run off the main thread.
     *
     * - Observes changes. Room automatically observes changes and emits new values to the flow.
     *
     * - Async sequence. Flow emits the entire query result on each change, and it won't
     * introduce any buffers. If we return Flow<List<T>>, the flow emits List<T> that contains
     * all rows from the query result. It will execute just like a sequence - emitting one
     * query result at a time and suspending until it is asked for the next one.
     *
     * - Cancellable. When the scope that's collecting these flows is cancelled, Room cancels
     * observing this query.
     */
    @Query("SELECT * FROM plants ORDER BY name")
    fun getPlantsFlow(): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE growZoneNumber = :growZoneNumber ORDER BY name")
    fun getPlantsWithGrowZoneNumberFlow(growZoneNumber: Int): Flow<List<Plant>>
}