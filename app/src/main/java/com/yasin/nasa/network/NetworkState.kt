package com.yasin.nasa.network

/**
 * Created by Yasin on 7/6/20.
 */
sealed class NetworkState<out T> {
    object Loading : NetworkState<Nothing>()
    object NetworkError : NetworkState<Nothing>()
    data class Error<T>(val message: String) : NetworkState<T>()
    data class Success<T>(val data: T?) : NetworkState<T>()
}