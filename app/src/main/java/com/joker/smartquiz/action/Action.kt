package com.joker.smartquiz.action

import android.content.ServiceConnection

import android.app.Activity
import android.content.ComponentName
import android.os.IBinder
import android.util.Log
import com.joker.smartquiz.service.GenericBinder
import com.joker.smartquiz.service.ServiceCallback

/**
 * actions
 * @author Joker
 * @since 2022/10/31
 */
@Suppress("UNCHECKED_CAST")
abstract class Action<T>(internal var serviceCallback: ServiceCallback?) {
    abstract val name: String
    var service: T? = null
    var bound: Boolean = false
    abstract suspend fun run(act: Activity)
    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val binder = binder as GenericBinder<T>
            Log.e("bound", "onServiceConnected")
            Log.e("bound", "onServiceConnected")
            Log.e("bound", "onServiceConnected")
            service = binder.getService()
            binder.callback = serviceCallback
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e("bound", "onServiceDisconnected")
            Log.e("bound", "onServiceDisconnected")
            Log.e("bound", "onServiceDisconnected")
            service = null
            bound = false
        }
    }
    override fun toString() = name

}