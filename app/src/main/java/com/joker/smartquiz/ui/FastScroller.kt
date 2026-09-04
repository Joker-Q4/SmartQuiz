package com.joker.smartquiz.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * 可拖动的快速滚动条 - 使用 XML 布局中的滚动条视图
 */
class FastScroller(
    private val recyclerView: RecyclerView,
    private val scrollbarThumb: View
) {

    private var isDragging = false
    private var lastTouchY = 0f
    private var topOffset = 0f // 顶部偏移量（ActionBar 高度）
    private var hideAnimator: ObjectAnimator? = null
    private val fadeDuration = 300L // 渐隐动画时长

    init {
        // 初始隐藏滚动条
        scrollbarThumb.alpha = 0f

        // 设置触摸监听
        setupTouchListeners()

        // 监听滚动事件，更新滚动条位置
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isDragging) {
                    updateScrollbarPosition()
                    showScrollbar()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // 滚动停止，渐隐滚动条
                    hideScrollbar()
                }
            }
        })
    }

    private fun showScrollbar() {
        // 取消正在进行的渐隐动画
        hideAnimator?.cancel()
        // 立即显示滚动条（无动画）
        scrollbarThumb.alpha = 1f
    }

    private fun hideScrollbar() {
        // 渐隐滚动条
        hideAnimator = ObjectAnimator.ofFloat(scrollbarThumb, "alpha", 1f, 0f).apply {
            duration = fadeDuration
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    scrollbarThumb.alpha = 0f
                }
            })
            start()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListeners() {
        scrollbarThumb.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = true
                    lastTouchY = event.rawY
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    // 拖动时也显示滚动条
                    showScrollbar()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDragging) {
                        val deltaY = event.rawY - lastTouchY
                        lastTouchY = event.rawY

                        // 计算新的滚动条位置
                        val parent = v.parent as? View
                        if (parent != null) {
                            val parentHeight = parent.height.toFloat()
                            val thumbHeight = v.height.toFloat()
                            val marginPx = 8 * recyclerView.context.resources.displayMetrics.density
                            val maxThumbY = parentHeight - thumbHeight - marginPx * 2
                            val minThumbY = topOffset + marginPx

                            val newThumbY = (v.y + deltaY).coerceIn(minThumbY, maxThumbY)
                            v.y = newThumbY

                            // 计算对应的 RecyclerView 滚动位置
                            val scrollRange = maxThumbY - minThumbY
                            if (scrollRange > 0) {
                                val scrollProgress = (newThumbY - minThumbY) / scrollRange
                                val totalScrollRange = recyclerView.computeVerticalScrollRange()
                                val visibleRange = recyclerView.computeVerticalScrollExtent()
                                val maxScrollOffset = totalScrollRange - visibleRange

                                if (maxScrollOffset > 0) {
                                    val targetScrollOffset = (scrollProgress * maxScrollOffset).roundToInt()
                                    val currentScrollOffset = recyclerView.computeVerticalScrollOffset()
                                    recyclerView.scrollBy(0, targetScrollOffset - currentScrollOffset)
                                }
                            }
                        }
                        true
                    } else {
                        false
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                    v.parent?.requestDisallowInterceptTouchEvent(false)
                    // 拖动结束后渐隐
                    hideScrollbar()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateScrollbarPosition() {
        val scrollOffset = recyclerView.computeVerticalScrollOffset()
        val scrollRange = recyclerView.computeVerticalScrollRange()
        val visibleRange = recyclerView.computeVerticalScrollExtent()

        val maxOffset = scrollRange - visibleRange
        if (maxOffset <= 0) {
            scrollbarThumb.visibility = View.GONE
            return
        }

        scrollbarThumb.visibility = View.VISIBLE

        val parent = scrollbarThumb.parent as? View
        if (parent != null) {
            val parentHeight = parent.height.toFloat()
            val thumbHeight = scrollbarThumb.height.toFloat()
            val marginPx = 8 * recyclerView.context.resources.displayMetrics.density
            val maxThumbY = parentHeight - thumbHeight - marginPx * 2
            val minThumbY = topOffset + marginPx

            val scrollProgress = scrollOffset.toFloat() / maxOffset
            val thumbY = (minThumbY + scrollProgress * (maxThumbY - minThumbY)).roundToInt()

            scrollbarThumb.y = thumbY.toFloat()
        }
    }
}

