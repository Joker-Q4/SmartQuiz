package com.joker.smartquiz.service

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

/**
 * @author Joker
 * @since 2026/08/11
 */
class FloatingTouchListener(
    private val params: WindowManager.LayoutParams,
    private val windowManager: WindowManager
) : View.OnTouchListener {

    private var lastX = 0
    private var lastY = 0
    private var startX = 0
    private var startY = 0
    private var isClick = true // 标记是否为点击（而非拖动）
    private val clickThreshold = 10 // 点击阈值（像素）

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录按下时的坐标
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
                startX = params.x
                startY = params.y
                isClick = true // 初始假设为点击
            }
            MotionEvent.ACTION_MOVE -> {
                // 计算当前移动的距离
                val dx = event.rawX.toInt() - lastX
                val dy = event.rawY.toInt() - lastY

                // 如果移动距离超过阈值，则认为是拖动而非点击
                if (kotlin.math.abs(dx) > clickThreshold || kotlin.math.abs(dy) > clickThreshold) {
                    isClick = false
                }

                // 更新悬浮窗的位置
                params.x = startX + dx
                params.y = startY + dy

                // 更新视图布局，实时刷新位置
                windowManager.updateViewLayout(view, params)
            }
            MotionEvent.ACTION_UP -> {
                // 如果是点击（移动距离很小），调用 performClick 以支持无障碍
                if (isClick) {
                    view.performClick()
                }
            }
        }
        return true // 返回 true，表示已处理触摸事件，防止事件进一步传递
    }
}