package com.joker.smartquiz.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import cn.vove7.andro_accessibility_api.requireBaseAccessibility
import cn.vove7.auto.core.api.back
import com.joker.smartquiz.activity.MainActivity
import com.joker.smartquiz.R
import com.joker.smartquiz.utils.launchWithExpHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * # ForegroundService
 * @author Joker
 * @since 2026/08/11
 */
class ForegroundService : GenericService<ForegroundService>() {

    override fun createService(): ForegroundService {
        return this
    }

    private val channelId by lazy {
        val id = "ForegroundService"
        val c = NotificationChannel(
            id,
            getString(R.string.fore_service),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setShowBadge(false)
            enableVibration(false)
            enableLights(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(c)
        id
    }

    private fun getNotification() = NotificationCompat.Builder(this, channelId).apply {
        setContentTitle(getString(R.string.fore_service))
        setContentText("")
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setSmallIcon(R.mipmap.ic_launcher_round)
        setOngoing(true)

        val intent = Intent(this@ForegroundService, MainActivity::class.java) //代表fragment所绑定的activity，这个需要写全路径


        val printIntent = PendingIntent.getActivity(
            this@ForegroundService, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        setContentIntent(printIntent)
    }.build()

    override fun onCreate() {
        super.onCreate()
        startForeground(1999, getNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.action?.also {
            parseAction(it)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun parseAction(action: String) {
        when (action) {
            ACTION_PRINT_LAYOUT -> {
                launchWithExpHandler {
                    requireBaseAccessibility()
                    back()
                    delay(1.seconds)
//                    printLayoutInfo()
                }
            }
        }

    }

    companion object {
        const val ACTION_PRINT_LAYOUT = "print_layout"
    }
}