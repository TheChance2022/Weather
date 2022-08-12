package com.moataz.weather.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.moataz.weather.R
import com.moataz.weather.data.model.WeatherResponse
import com.moataz.weather.data.request.ApiClient
import com.moataz.weather.data.request.NetworkStatus
import com.moataz.weather.databinding.ActivityMainBinding
import com.moataz.weather.utils.add
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val apiClient by lazy { ApiClient() }
    private var compositeDisposable: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getNetworkResult()
    }

    private fun getNetworkResult() {
        val netWorkResult = Observable.create { status ->
            status.onNext(NetworkStatus.Loading())
            callWeatherAPI(status)
        }
        netWorkResult.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe { status ->
                when (status) {
                    is NetworkStatus.Failure -> {
                        displayNormalVisibilityState()
                        displayFailureState()
                    }
                    is NetworkStatus.Loading -> {
                        displayLoadingVisibilityState()
                    }
                    is NetworkStatus.Success -> {
                        displayNormalVisibilityState()
                        displayWeatherData(status)
                    }
                }
            }.add(compositeDisposable)
    }

    private fun callWeatherAPI(status: ObservableEmitter<NetworkStatus<WeatherResponse>>) {
        apiClient.makeApiRequest().enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                status.onNext(NetworkStatus.Failure(e.message.toString()))
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string().let { jsonString ->
                    val transferredData =
                        Gson().fromJson(jsonString, WeatherResponse::class.java)
                    status.onNext(NetworkStatus.Success(transferredData))
                }
            }
        })
    }

    private fun displayLoadingVisibilityState(){
        binding.apply {
            group.visibility = View.GONE
            animationLoding.visibility = View.VISIBLE
        }
    }

    private fun displayNormalVisibilityState(){
        binding.apply {
            group.visibility = View.VISIBLE
            animationLoding.visibility = View.GONE
        }
    }

    private fun displayFailureState() {
        binding.apply {
            animationLoding.setAnimation(R.raw.icon_error)
            animationLoding.playAnimation()
        }
    }

    private fun displayWeatherData(status: NetworkStatus.Success<WeatherResponse>) {
        binding.apply {
            descriptionTextview.text = status.transferredData.data.first().weather.description
            temperature.text = resources.getString(
                R.string.temp, status.transferredData.data.first().temp.toString()
            )
            sunsetTextview.text = status.transferredData.data.first().sunset
            windSpeedTextview.text = status.transferredData.data.first().wind_spd.toInt().toString()
            humidityTextview.text = status.transferredData.data.first().rh.toInt().toString()
        }
    }

    override fun onDestroy() {
        compositeDisposable?.dispose()
        super.onDestroy()
    }
}


