package com.joker.smartquiz

import android.app.Application
import android.os.Handler
import cn.vove7.andro_accessibility_api.AccessibilityApi
import com.joanzapata.iconify.Iconify
import com.joanzapata.iconify.fonts.FontAwesomeModule
import com.joker.smartquiz.icon.FontModule
import com.joker.smartquiz.service.BaseAccessibilityService
import com.joker.smartquiz.service.GestureAccessibilityService
import com.joker.smartquiz.utils.Utils

/**
 * @author Joker
 * @since 2026/08/11
 */
class JokerApp : Application() {
    companion object {
        lateinit var INS: Application
        lateinit var HANDLER: Handler
    }

    override fun onCreate() {
        INS = this
        HANDLER = Handler(this.mainLooper)
        super.onCreate()
        Iconify
            .with(FontAwesomeModule())
            .with(FontModule())

        AccessibilityApi.apply {
            BASE_SERVICE_CLS = BaseAccessibilityService::class.java
            GESTURE_SERVICE_CLS = GestureAccessibilityService::class.java
        }
        Utils.init(this)
    }
}