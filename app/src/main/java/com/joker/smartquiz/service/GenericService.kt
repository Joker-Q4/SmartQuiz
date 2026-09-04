package com.joker.smartquiz.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * @author Joker
 * @since 2026/08/11
 */
abstract class GenericService<T> : Service() {

    lateinit var binder: GenericBinder<T>

    // 初始化通用Binder
    override fun onBind(intent: Intent?): IBinder? {
        binder = GenericBinder(createService())
        return binder
    }

    // 每个子类必须实现的方法，提供相应的服务实例
    protected abstract fun createService(): T
}

interface ServiceCallback {
    fun onServiceStopped()  // Service 关闭时调用
}