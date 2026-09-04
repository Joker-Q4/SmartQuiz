package com.joker.smartquiz.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import com.joker.smartquiz.activity.MainActivity
import com.joker.smartquiz.R
import androidx.core.view.isVisible
import cn.hutool.extra.tokenizer.engine.ikanalyzer.IKAnalyzerEngine
import com.joanzapata.iconify.widget.IconTextView
import com.joker.smartquiz.database.AppDatabase
import com.joker.smartquiz.similarity.CosineSimilarity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import androidx.core.graphics.toColorInt
import cn.vove7.auto.core.viewfinder.ScreenTextFinder
import kotlin.math.abs

/**
 * @author Joker
 * @since 2026/08/11
 */
class FloatingWindowService : GenericService<FloatingWindowService>() {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private lateinit var toggleButton: IconTextView
    private lateinit var dragHandle: IconTextView
    private lateinit var look: IconTextView
    private lateinit var close: IconTextView
    private lateinit var title: TextView
    private lateinit var answer: TextView

    private var isStop: Boolean = true

    // 本次扫描参与匹配的题库 id 列表（由 AnswerAction 通过 Intent 传入）
    private var selectedTitleIds: List<String> = emptyList()


    private var layoutParams: WindowManager.LayoutParams? = null
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var lastWidth = 0
    private var lastHeight = 0


    private val channelId by lazy {
        val id = "FloatingWindowService"
        val c = NotificationChannel(
            id,
            "保活",
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
        setContentTitle("保活")
        setContentText("这是一条保活通知，勿动！")
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setSmallIcon(R.mipmap.ic_launcher_round)
        setOngoing(true)

        val intent = Intent(this@FloatingWindowService, MainActivity::class.java) //代表fragment所绑定的activity，这个需要写全路径


        val printIntent = PendingIntent.getActivity(
            this@FloatingWindowService, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        setContentIntent(printIntent)

    }.build()

    override fun createService(): FloatingWindowService {
        return this
    }

    override fun onCreate() {
        super.onCreate()
        // 在 Android 12 及以上版本，必须声明前台服务类型
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
////            setForegroundServiceType(FOREGROUND_SERVICE_TYPE_OTHER) // 声明为其他类型
////            foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
//            startForeground(1999, getNotification())
//        }
        isStop = false
        startForeground(1999, getNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        return super.onStartCommand(intent, flags, startId)
        selectedTitleIds = intent?.getStringArrayListExtra("titleIds") ?: emptyList()
        createFloatingWindow()
        setupToggleButton(
            toggleButton,
            floatingView!!,
            windowManager!!,
            layoutParams!!
        )
        setupAnswerButton(
            look,
            title,
            floatingView!!,
            windowManager!!,
            layoutParams!!
        )
        return START_NOT_STICKY
    }

    private fun createFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 创建悬浮窗视图
        @SuppressWarnings("inflateParams")
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window, null)

        toggleButton = floatingView!!.findViewById(R.id.toggle_visibility)
        dragHandle = floatingView!!.findViewById(R.id.drag_handle)
        look = floatingView!!.findViewById(R.id.look)
        close = floatingView!!.findViewById(R.id.close)
        title = floatingView!!.findViewById(R.id.title)
        answer = floatingView!!.findViewById(R.id.answer)


        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val maxWidth = screenWidth  // 最大宽度 = 屏幕宽度
        val maxHeight = screenHeight

        val minWidth = 200
        val minHeight = 200


        layoutParams = WindowManager.LayoutParams(
            (screenWidth * 0.3).toInt(),  // 宽度为屏幕宽度的 50%
            (screenHeight * 0.2).toInt(), // 高度为屏幕高度的 30%
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // 不阻塞其他操作
            PixelFormat.TRANSLUCENT
        )

        // 初始位置设置
        layoutParams!!.gravity = Gravity.TOP or Gravity.START
//        layoutParams!!.x = 200
//        layoutParams!!.y = 500

        // 将悬浮窗添加到窗口
        windowManager?.addView(floatingView, layoutParams)

        // 设置拖动监听器
        floatingView?.setOnTouchListener(FloatingTouchListener(layoutParams!!, windowManager!!))

        toggleButton.setOnClickListener {
            if (floatingView!!.isVisible) {
                floatingView!!.visibility = View.GONE
//                toggleButton.text = "显示"
            } else {
                floatingView!!.visibility = View.VISIBLE
//                toggleButton.text = "隐藏"
            }
        }

        close.setOnClickListener {
            isStop = true
            removeFloatingWindow()
            binder.callback?.onServiceStopped()
            stopSelf()
        }

        // 通过拖拽 `drag_handle` 调整窗口大小
        val dragTouchListener = object : View.OnTouchListener {
            private var isClick = true
            private val clickThreshold = 10

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 记录初始大小和触摸点
                        lastWidth = layoutParams!!.width
                        lastHeight = layoutParams!!.height
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = (event.rawX - initialTouchX).toInt()
                        val deltaY = (event.rawY - initialTouchY).toInt()

                        // 如果移动距离超过阈值，则认为是拖拽而非点击
                        if (abs(deltaX) > clickThreshold || abs(deltaY) > clickThreshold) {
                            isClick = false
                        }

                        // 计算新的宽高，限制范围
                        val newWidth = (lastWidth + deltaX).coerceIn(minWidth, maxWidth)
                        val newHeight = (lastHeight + deltaY).coerceIn(minHeight, maxHeight)

                        if (layoutParams!!.width != newWidth || layoutParams!!.height != newHeight) {
                            layoutParams!!.width = newWidth
                            layoutParams!!.height = newHeight
                            windowManager!!.updateViewLayout(floatingView, layoutParams)
                        }
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        // 如果是点击（移动距离很小），调用 performClick 以支持无障碍
                        if (isClick) {
                            v.performClick()
                        }
                        return true
                    }
                }
                return false
            }
        }
        @SuppressLint("ClickableViewAccessibility")
        dragHandle.setOnTouchListener(dragTouchListener)



        CoroutineScope(Dispatchers.IO).launch {
            val engine = IKAnalyzerEngine()
            while(!isStop) {
                if(selectedTitleIds.isEmpty()) {
                    break
                }
                val all = AppDatabase.getInstance().inputDataDao().getSentence(selectedTitleIds)
                if(all == null || all.isEmpty()) {
                    break
                }
                Log.d("data", "获取屏幕数据：" + ScreenTextFinder().find().joinToString("；"))
                val screen = ScreenTextFinder().find()
                    //                    .filter {
                    //                        (it.nodeWrapper != null && it.nodeWrapper.viewIdResourceName != null
                    //                                && !it.id.contains("com.android.systemui")
                    //                                && !it.id.contains("com.miui.securitycenter")
                    //                                && !it.id.contains("com.joker.smartquiz"))
                    //                                || it.nodeWrapper == null || it.nodeWrapper.viewIdResourceName == null
                    //                    }
                    .asSequence()
                    .filter {
                        // 用带完整包名的 viewIdResourceName 过滤，避免抓到自己的悬浮窗/系统灵动岛
                        val rid = it.node.viewIdResourceName
                        rid == null || (
                                !rid.contains("com.android.systemui")
                                        && !rid.contains("com.miui.securitycenter")
                                        && !rid.contains("com.joker.smartquiz"))
                    }
                    .map {
                            obj ->
//                        Log.d("文字", obj.id + "    " + obj.text?.toString())
                        // text 为空时回退到 contentDescription（图标/图片按钮常用描述而非 text）
                        val t = obj.text ?: obj.node.contentDescription
                        Log.d("文字", t?.toString().toString())
                        t
                    }.filter {
                        StringUtils.isNotBlank(it)
                    }.map {
                        it!!.trim()
                    }.toList().joinToString(" ")
                Log.d("data", "获取屏幕数据：$screen")
                val split = engine.parse(screen)
                val curSentence = split.filterNotNull().mapNotNull { it.text }.toList()
                if(curSentence.isEmpty() || StringUtils.isBlank(curSentence.joinToString(""))) {
                    continue
                }
                var maxScore: Double = -1.0
                var id: String? = null
                for (data in all) {
                    val apply = CosineSimilarity.calculateCosineSimilarity(data.key.split("||||"), curSentence)
                    if(apply >= maxScore) {
                        maxScore = apply
                        id = data.id
                    }
                }
                val one = AppDatabase.getInstance().inputDataDao().getOne(id!!)
//                    val show = "答案：\t" + one.answer + "\n" + one.question + "\n" +
//                            (if(one.col_a==null) "" else one.col_a + "\n") +
//                            (if(one.col_b==null) "" else one.col_b + "\n") +
//                            (if(one.col_c==null) "" else one.col_c + "\n") +
//                            (if(one.col_d==null) "" else one.col_d + "\n") +
//                            (if(one.col_e==null) "" else one.col_e + "\n") +
//                            (if(one.col_f==null) "" else one.col_f + "\n")
                val show = one.question + "\n" +
                        (if(one.col_a==null) "" else one.col_a + "\n") +
                        (if(one.col_b==null) "" else one.col_b + "\n") +
                        (if(one.col_c==null) "" else one.col_c + "\n") +
                        (if(one.col_d==null) "" else one.col_d + "\n") +
                        (if(one.col_e==null) "" else one.col_e + "\n") +
                        (if(one.col_f==null) "" else one.col_f + "\n")
                Log.d("answer", show)
//                CoroutineScope(Dispatchers.Main).launch {
//                    setText(one.answer, "当前页面最高得分$maxScore\n$show")
//                }
                CoroutineScope(Dispatchers.Main).launch {
                    // 标题显示答案，正文显示匹配到的题目与选项
                    setText("${one.answer}", show)
                }
            }
            delay(300.milliseconds)
//        }
        }
    }

    // 关闭悬浮窗
    fun removeFloatingWindow() {
        floatingView?.let {
            windowManager?.removeView(it)
            floatingView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isStop = true
        floatingView?.let { windowManager?.removeView(it) }
    }

    // 提供一个方法供客户端调用
    fun setText(top: String, desc: String) {
        title.text = top
        answer.text = desc
    }

    private fun setupToggleButton(
        button: View,
        floatingView: View,
        windowManager: WindowManager,
        layoutParams: WindowManager.LayoutParams
    ) {
        var isMinimized = false
        var originalWidth = layoutParams.width
        var originalHeight = layoutParams.height
        var originalX = layoutParams.x
        var originalY = layoutParams.y

        button.setOnClickListener {
            if (isMinimized) {
                // 还原悬浮窗
                layoutParams.width = originalWidth
                layoutParams.height = originalHeight
                layoutParams.x = originalX
                layoutParams.y = originalY
//                floatingView.setBackgroundColor("#80000000".toColorInt()) // 恢复背景
                floatingView.background = AppCompatResources.getDrawable(this, R.drawable.radius) // 变为半透明
                showOther()
            } else {
                // 记录原始大小和位置
                originalWidth = layoutParams.width
                originalHeight = layoutParams.height
                originalX = layoutParams.x
                originalY = layoutParams.y

                // 缩小到左上角
                layoutParams.width = 200
                layoutParams.height = 200
                layoutParams.x = 0
                layoutParams.y = 0
//                floatingView.background = null
                floatingView.setBackgroundColor(Color.TRANSPARENT)
                hideOther()
            }
            isMinimized = !isMinimized
            windowManager.updateViewLayout(floatingView, layoutParams)
        }
    }

    private fun setupAnswerButton(
        button: View,
        ans: View,
        floatingView: View,
        windowManager: WindowManager,
        layoutParams: WindowManager.LayoutParams
    ) {
        var isMinimized = false
        var originalWidth = layoutParams.width
        var originalHeight = layoutParams.height
        var originalX = layoutParams.x
        var originalY = layoutParams.y

        button.setOnClickListener {
            if (isMinimized) {
                // 还原悬浮窗
                layoutParams.width = originalWidth
                layoutParams.height = originalHeight
                layoutParams.x = originalX
                layoutParams.y = originalY
//                floatingView.setBackgroundColor("#80000000".toColorInt()) // 恢复背景
                floatingView.background = AppCompatResources.getDrawable(this, R.drawable.radius) // 变为半透明
                showAnswerOther()
            } else {
                // 记录原始大小和位置
                originalWidth = layoutParams.width
                originalHeight = layoutParams.height
                originalX = layoutParams.x
                originalY = layoutParams.y

                // 缩小到左上角
                layoutParams.width = 200
                layoutParams.height = 200
                layoutParams.x = 0
                layoutParams.y = 0
//                floatingView.background = null
                floatingView.setBackgroundColor(Color.TRANSPARENT)
                hideAnswerOther()
            }
            isMinimized = !isMinimized
            windowManager.updateViewLayout(floatingView, layoutParams)
        }
        ans.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private val clickThreshold = 10 // 触摸误差阈值（防止误触）

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 记录初始位置
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // 计算新的位置
                        layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, layoutParams)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        // 计算手指移动距离
                        val deltaX = abs(event.rawX - initialTouchX)
                        val deltaY = abs(event.rawY - initialTouchY)

                        // 如果手指基本没动，触发点击事件
                        if (deltaX < clickThreshold && deltaY < clickThreshold) {
                            // 调用 performClick 以支持无障碍功能
                            v.performClick()
                            if (isMinimized) {
                                // 还原悬浮窗
                                layoutParams.width = originalWidth
                                layoutParams.height = originalHeight
                                layoutParams.x = originalX
                                layoutParams.y = originalY
//                floatingView.setBackgroundColor("#80000000".toColorInt()) // 恢复背景
                                floatingView.background = AppCompatResources.getDrawable(this@FloatingWindowService, R.drawable.radius) // 变为半透明
                                showAnswerOther()
                            }
                            isMinimized = !isMinimized
                            windowManager.updateViewLayout(floatingView, layoutParams)
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    fun showOther() {
        toggleButton.text = getString(R.string.yin_cang)
        val dpToPx = dpToPx(this, 25)
        toggleButton.layoutParams.height = dpToPx
        toggleButton.layoutParams.width = dpToPx
//        windowManager!!.updateViewLayout(toggleButton, toggleButton.layoutParams)
//        toggleButton.visibility = View.VISIBLE
        look.visibility = View.VISIBLE
        dragHandle.visibility = View.VISIBLE
        close.visibility = View.VISIBLE
        title.visibility = View.VISIBLE
        answer.visibility = View.VISIBLE
    }
    fun hideOther() {
        toggleButton.text = ""
        toggleButton.layoutParams.height = 200  //88
        toggleButton.layoutParams.width = 200
//        windowManager!!.updateViewLayout(toggleButton, toggleButton.layoutParams)
//        toggleButton.visibility = View.INVISIBLE
        look.visibility = View.GONE
        dragHandle.visibility = View.GONE
        close.visibility = View.GONE
        title.visibility = View.GONE
        answer.visibility = View.GONE
    }

    fun showAnswerOther() {
        val dpToPx = dpToPx(this, 25)
        title.layoutParams.height = dpToPx
        title.setTextColor(Color.WHITE)
//        look.text = getString(R.string.da_an)
//        val dpToPx = dpToPx(this, 25)
//        look.layoutParams.height = dpToPx
//        look.layoutParams.width = dpToPx
//        windowManager!!.updateViewLayout(toggleButton, toggleButton.layoutParams)
//        toggleButton.visibility = View.VISIBLE
        look.visibility = View.VISIBLE
        toggleButton.visibility = View.VISIBLE
        dragHandle.visibility = View.VISIBLE
        close.visibility = View.VISIBLE
//        title.visibility = View.VISIBLE
        answer.visibility = View.VISIBLE
    }
    fun hideAnswerOther() {
//        look.layoutParams.height = 200  //88
//        look.layoutParams.width = 200
//        windowManager!!.updateViewLayout(toggleButton, toggleButton.layoutParams)
//        toggleButton.visibility = View.INVISIBLE
        title.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        title.setTextColor("#7F000000".toColorInt())
        toggleButton.visibility = View.GONE
        look.visibility = View.GONE
        dragHandle.visibility = View.GONE
        close.visibility = View.GONE
//        title.visibility = View.GONE
        answer.visibility = View.GONE
    }

    fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    @Suppress("unused")
    fun pxToDp(context: Context, px: Int): Int {
        return (px / context.resources.displayMetrics.density).toInt()
    }
}
