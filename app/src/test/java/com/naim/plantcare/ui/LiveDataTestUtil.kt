package com.naim.plantcare.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.awaitValue(): T {
    var result: T? = null
    var received = false
    val observer = Observer<T> {
        result = it
        received = true
    }
    observeForever(observer)
    removeObserver(observer)
    check(received) { "LiveData has no value" }
    @Suppress("UNCHECKED_CAST")
    return result as T
}
