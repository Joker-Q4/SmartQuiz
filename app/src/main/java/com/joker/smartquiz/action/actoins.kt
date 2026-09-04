package com.joker.smartquiz.action

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.joker.smartquiz.service.FloatingWindowService
import com.joker.smartquiz.service.NoneService
import com.joker.smartquiz.utils.toast
import androidx.core.net.toUri
import cn.vove7.andro_accessibility_api.requireBaseAccessibility
import cn.vove7.andro_accessibility_api.waitBaseAccessibility
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.joker.smartquiz.database.AppDatabase
import com.joker.smartquiz.database.entity.InputTitle
import com.joker.smartquiz.service.BaseAccessibilityService
import com.joker.smartquiz.service.ServiceCallback
import kotlin.coroutines.resume
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * actions
 * @author Joker
 * @since 2022/10/31
 */

class RequireAction : Action<NoneService>(null) {
    override val name: String get() = "获取权限"

    override suspend fun run(act: Activity) {
        requireBaseAccessibility(true)
        waitBaseAccessibility()
        toast("获取权限完毕")
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun scanScreen(action: AnswerAction, act: Activity, floatingWindowServiceIntent: Intent) {
        if (!Settings.canDrawOverlays(act)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:com.joker.smartquiz".toUri()
            )
            act.startActivityForResult(intent, 100)
        }else {
            act.startForegroundService(floatingWindowServiceIntent)
        }

        act.bindService(floatingWindowServiceIntent, action.serviceConnection, BIND_AUTO_CREATE)
}

/**
 * 弹出题库多选对话框，返回被选中题库的 id 列表；取消返回 null。默认全部选中。
 */
suspend fun selectBanks(act: Activity, banks: List<InputTitle>): List<String>? =
    suspendCancellableCoroutine { cont ->
        act.runOnUiThread {
            val names = banks.map { it.fileName }.toTypedArray()
            val checked = BooleanArray(banks.size) { true }
            MaterialAlertDialogBuilder(act)
                .setTitle("选择参与匹配的题库")
                .setMultiChoiceItems(names, checked) { _, which, isChecked -> checked[which] = isChecked }
                .setPositiveButton("确定") { _, _ ->
                    cont.resume(banks.filterIndexed { i, _ -> checked[i] }.map { it.id })
                }
                .setNegativeButton("取消") { _, _ -> cont.resume(null) }
                .setOnCancelListener { if (cont.isActive) cont.resume(null) }
                .show()
        }
    }

open class AnswerAction : Action<FloatingWindowService>(null), ServiceCallback {

    override val name: String get() = "自动扫描"

    lateinit var act: Activity
    var floatingWindowServiceIntent: Intent? = null

    override suspend fun run(act: Activity) {
        if(hasPer(act, BaseAccessibilityService::class.java.name)) {
            val banks = AppDatabase.getInstance().inputTitleDao().getAll()
            if(banks == null || banks.isEmpty()) {
                toast("尚未导入题库，请检查")
                Log.d("data", "尚未导入题库，请检查")
                return
            }
            val selected = selectBanks(act, banks)
            if(selected.isNullOrEmpty()) {
                toast("未选择题库，已取消")
                return
            }
            val all = AppDatabase.getInstance().inputDataDao().getSentence(selected)
            if(all == null || all.isEmpty()) {
                toast("所选题库无题目数据")
                return
            }
            this.serviceCallback = this
            this.act = act
            if(floatingWindowServiceIntent != null) {
                toast("当前已有正在执行的任务")
                return
            }
            floatingWindowServiceIntent = Intent(act, FloatingWindowService::class.java)
                .putStringArrayListExtra("titleIds", ArrayList(selected))
            scanScreen(this, act, floatingWindowServiceIntent!!)
        }else {
            toast("请先授予无障碍权限")
        }
    }

    override fun onServiceStopped() {
        act.unbindService(this.serviceConnection)
        act.stopService(floatingWindowServiceIntent)
        floatingWindowServiceIntent = null
    }

}

@Suppress("DEPRECATION")
fun hasPer(act: Activity, name: String): Boolean {
    val systemService: ActivityManager = act.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningServices = systemService.getRunningServices(100)
    if(runningServices == null || runningServices.isEmpty()) {
        return false
    }
    for (info in runningServices) {
        val service = info.service
        if(service.className.contains(name)) {
            return true
        }
    }
    return false
}
