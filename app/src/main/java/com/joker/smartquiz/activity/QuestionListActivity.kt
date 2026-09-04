@file:OptIn(DelicateCoroutinesApi::class)
@file:Suppress("DEPRECATION", "DeferredResultUnused")

package com.joker.smartquiz.activity

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joker.smartquiz.R
import com.joker.smartquiz.adapter.QuestionAdapter
import com.joker.smartquiz.database.AppDatabase
import com.joker.smartquiz.database.entity.InputData
import com.joker.smartquiz.ui.FastScroller
import com.joker.smartquiz.utils.toast
import com.lxj.xpopup.XPopup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * 题目列表：展示某个题库下的所有题目。
 * 单击查看题目详情，长按删除该题。
 * @author Joker
 * @since 2026/08/11
 */
class QuestionListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    private var titleId: String = ""
    private var fileName: String = ""

    private val adapter = QuestionAdapter(
        onItemClick = { data -> showDetail(data) },
        onItemLongClick = { data -> deleteQuestion(data) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 15+ 强制使用边缘到边缘模式
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_question_list)

        // 添加状态栏背景 View，确保状态栏显示品牌色
        val statusBarBackground = View(this).apply {
            setBackgroundColor(getColor(R.color.brand_primary))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                0
            )
        }
        (findViewById<View>(android.R.id.content) as FrameLayout).addView(statusBarBackground)

        // 监听窗口内边距变化，设置状态栏背景高度
        ViewCompat.setOnApplyWindowInsetsListener(statusBarBackground) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.layoutParams.height = insets.top
            v.requestLayout()
            WindowInsetsCompat.CONSUMED
        }

        titleId = intent.getStringExtra("titleId") ?: ""
        fileName = intent.getStringExtra("fileName") ?: ""

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = fileName.ifBlank { "题目列表" }

        recyclerView = findViewById(R.id.recycler_view)
        emptyView = findViewById(R.id.tv_empty)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 添加可拖动的快速滚动条
        val scrollbarThumb = findViewById<View>(R.id.scrollbar_thumb)
        FastScroller(recyclerView, scrollbarThumb)

        refresh()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun refresh() {
        GlobalScope.async {
            val list = AppDatabase.getInstance().inputDataDao().getByTitle(titleId) ?: emptyList()
            CoroutineScope(Dispatchers.Main).launch {
                supportActionBar?.subtitle = "共 ${list.size} 道题"
                adapter.submitList(list)
                emptyView.isVisible = list.isEmpty()
                recyclerView.isVisible = list.isNotEmpty()
            }
        }
    }

    private fun showDetail(data: InputData) {
        val sb = StringBuilder()
        sb.append("【题目】\n").append(data.question ?: "").append("\n\n")
        listOf(data.col_a, data.col_b, data.col_c, data.col_d, data.col_e, data.col_f)
            .filter { !it.isNullOrBlank() }
            .forEach { sb.append(it).append("\n") }
        sb.append("\n【答案】\n").append(data.answer ?: "")

        XPopup.Builder(this)
            .asConfirm("题目详情", sb.toString(), null, "关闭", null, null, true)
            .show()
    }

    private fun deleteQuestion(data: InputData) {
        XPopup.Builder(this)
            .asConfirm("删除题目", "确定删除这道题目？\n${data.question ?: ""}") {
                GlobalScope.async {
                    AppDatabase.getInstance().inputDataDao().remove(data)
                    CoroutineScope(Dispatchers.Main).launch {
                        toast("已删除该题")
                        refresh()
                    }
                }
            }
            .show()
    }
}
