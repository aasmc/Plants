package ru.aasmc.plants.di

import android.content.Context
import androidx.annotation.VisibleForTesting
import ru.aasmc.plants.data.PlantRepository
import ru.aasmc.plants.data.cache.AppDatabase
import ru.aasmc.plants.data.cache.PlantDao
import ru.aasmc.plants.data.network.NetworkService
import ru.aasmc.plants.ui.PlantListViewModelFactory

interface ViewModelFactoryProvider {
    fun providePlantListViewModelFactory(context: Context): PlantListViewModelFactory
}

val Injector: ViewModelFactoryProvider
    get() = currentInjector

private object DefaultViewModelProvider : ViewModelFactoryProvider {
    override fun providePlantListViewModelFactory(context: Context): PlantListViewModelFactory {
        return PlantListViewModelFactory(getPlantRepository(context))
    }

    private fun getPlantRepository(context: Context): PlantRepository {
        return PlantRepository.getInstance(
            plantDao(context),
            plantService()
        )
    }

    private fun plantService(): NetworkService = NetworkService()

    private fun plantDao(context: Context): PlantDao =
        AppDatabase.getInstance(context.applicationContext).plantDao()

}

private object Lock

@Volatile
private var currentInjector: ViewModelFactoryProvider =
    DefaultViewModelProvider

@VisibleForTesting
private fun setInjectorForTesting(injector: ViewModelFactoryProvider?) {
    synchronized(Lock) {
        currentInjector = injector ?: DefaultViewModelProvider
    }
}

@VisibleForTesting
private fun resetInjector() =
    setInjectorForTesting(null)



















