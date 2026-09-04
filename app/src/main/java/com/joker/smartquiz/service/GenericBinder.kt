package com.joker.smartquiz.service

import android.os.Binder

class GenericBinder<T>(private val service: T) : Binder() {
    var callback: ServiceCallback? = null
    fun getService(): T = service
}
