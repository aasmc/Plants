package ru.aasmc.plants.data.network

sealed class NetworkResult<out T>

// by using Nothing as T, Loading is a subtype of all NetworkResult<T>
object Loading : NetworkResult<Nothing>()

// successful results are stored in data
data class OK<out T>(val data: T) : NetworkResult<T>()

data class NetworkError(val exception: Throwable): NetworkResult<Nothing>()