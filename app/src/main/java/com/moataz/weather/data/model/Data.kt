package com.moataz.weather.data.model

data class Data(
    val sunset: String,
    val weather: Weather,
    val temp: Double,
    val rh: Double,
    val wind_spd: Double

)