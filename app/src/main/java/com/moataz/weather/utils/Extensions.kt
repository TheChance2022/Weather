package com.moataz.weather.utils

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable


fun Disposable.add(compositeDisposable: CompositeDisposable?) {
    compositeDisposable?.add(this)
}