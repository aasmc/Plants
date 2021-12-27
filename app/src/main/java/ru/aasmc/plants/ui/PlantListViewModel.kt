package ru.aasmc.plants.ui

import androidx.lifecycle.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.aasmc.plants.data.PlantRepository
import ru.aasmc.plants.data.model.GrowZone
import ru.aasmc.plants.data.model.NoGrowZone
import ru.aasmc.plants.data.model.Plant

class PlantListViewModel internal constructor(
    private val plantRepository: PlantRepository
) : ViewModel() {

    /**
     * Request a snackbar to display a string.
     */
    private val _snackbar = MutableLiveData<String>()
    val snackbar: LiveData<String>
        get() = _snackbar

    /**
     * Show a spinner if true.
     */
    private val _spinner = MutableLiveData<Boolean>(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    /**
     * The current selection of growZone.
     */
    private val growZone = MutableLiveData<GrowZone>(NoGrowZone)

    /**
     * A list of plants that updates based on the current filter.
     */
    val plants: LiveData<List<Plant>> = growZone.switchMap { growZone ->
        if (growZone == NoGrowZone) {
            plantRepository.plants
        } else {
            plantRepository.getPlantsWithGrowZone(growZone)
        }
    }

    /**
     * A thread-safe flow holder that keeps only the last value it was given.
     */
    private val growZoneFlow = MutableStateFlow<GrowZone>(NoGrowZone)

    /**
     * FlatMapLatest - > whenever the growZone changes its value, this lambda will be
     * applied and it must return a Flow.
     */
    @ExperimentalCoroutinesApi
    val plantsUsingFlow: LiveData<List<Plant>> = growZoneFlow.flatMapLatest { growZone ->
        if (growZone == NoGrowZone) {
            plantRepository.plantsFlow
        } else {
            plantRepository.getPlantsWithGrowZoneFlow(growZone)
        }
    }.asLiveData()


    init {
        // when creating a new ViewModel, clear the grow zone and perform any related updates.
        clearGrowZoneNumber()

        growZoneFlow.mapLatest { growZone ->
            _spinner.value = true
            if (growZone == NoGrowZone) {
                plantRepository.tryUpdateRecentPlantsCache()
            } else {
                plantRepository.getPlantsWithGrowZone(growZone)
            }
        }
            .onEach { _spinner.value = false }
            .catch { throwable -> _snackbar.value = throwable.message }
            .launchIn(viewModelScope)

        // fetch the full plant list
//        launchDataLoad { plantRepository.tryUpdateRecentPlantsCache() }
    }

    /**
     * Filter the list of plants to this grow zone.
     */
    fun setGrowZoneNumber(num: Int) {
        growZone.value = GrowZone(num)
        growZoneFlow.value = GrowZone(num)
//        launchDataLoad { plantRepository.tryUpdateRecentPlantsForGrowZoneCache(GrowZone(num)) }
    }

    /**
     * Clear the current filter of this plants list.
     */
    fun clearGrowZoneNumber() {
        growZone.value = NoGrowZone
        growZoneFlow.value = NoGrowZone
//        launchDataLoad { plantRepository.tryUpdateRecentPlantsCache() }
    }

    /**
     * Return true if the current list is filtered.
     */
    fun isFiltered() = growZone.value != NoGrowZone

    /**
     * Called immediately after the UI shows the snackbar.
     */
    fun onSnackBarSwhon() {
        _snackbar.value = null
    }

    /**
     * Helper function to call a data load function with a loading spinner;
     * errors. will trigger a snackbar.
     *
     * @param block lambda to actually load data. It is called in the viewModelScope.
     *              Before calling the lambda, the loading spinner will display. After
     *              completion or error, the loading spinner will stop.
     */
    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            try {
                _spinner.value = true
                block()
            } catch (error: Throwable) {
                _snackbar.value = error.message
            } finally {
                _spinner.value = false
            }
        }
    }
}

class PlantListViewModelFactory(
    private val repository: PlantRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PlantListViewModel(repository) as T
    }
}

























