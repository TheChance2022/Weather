package com.moataz.weather.data.request

sealed class NetworkStatus<T>{
    class Success<T>(val transferredData: T) : NetworkStatus<T>()
    class Failure<T>(message: String) : NetworkStatus<T>()
    class Loading<T> : NetworkStatus<T>()
}
