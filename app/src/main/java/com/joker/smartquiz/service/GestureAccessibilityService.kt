package com.joker.smartquiz.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.view.accessibility.AccessibilityEvent
import cn.vove7.andro_accessibility_api.AccessibilityApi

/**
 * # GestureAccessibilityService
 * @author Joker
 * @since 2026/08/11
 */
@SuppressLint("AccessibilityPolicy")
class GestureAccessibilityService : AccessibilityService() {
    override fun onCreate() {
        super.onCreate()
        //must
        AccessibilityApi.gestureService = this
    }

    override fun onDestroy() {
        super.onDestroy()
        //must
        AccessibilityApi.gestureService = null
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
}