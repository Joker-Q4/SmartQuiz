package com.joker.smartquiz.service

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import cn.vove7.andro_accessibility_api.AccessibilityApi
import cn.vove7.auto.core.AppScope

/**
 * # MyAccessibilityService
 * @author Joker
 * @since 2026/08/11
 */
@SuppressLint("AccessibilityServicePolicy")
class BaseAccessibilityService : AccessibilityApi() {

    //启用 页面更新 回调
    override val enableListenPageUpdate: Boolean = true

    override fun onCreate() {
        //must set
        baseService = this
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        val command = intent?.getStringExtra("command")
        if(command != null && baseService != null) {
            Log.d(TAG, "command: $command")
            disableSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        //must set
        baseService = null
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    //页面更新回调
    override fun onPageUpdate(currentScope: AppScope) {
        Log.d(TAG, "onPageUpdate: $currentScope")
    }

    companion object {
        private const val TAG = "MyAccessibilityService"
    }
}