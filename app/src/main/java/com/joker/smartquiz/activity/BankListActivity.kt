@file:OptIn(DelicateCoroutinesApi::class)
@file:Suppress("DeferredResultUnused")

package com.joker.smartquiz.activity

import android.content.Intent
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
import com.joker.smartquiz.adapter.BankAdapter
import com.joker.smartquiz.database.AppDatabase
import com.joker.smartquiz.database.entity.InputTitle
import com.joker.smartquiz.ui.FastScroller
import com.joker.smartquiz.utils.toast
import com.lxj.xpopup.XPopup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.jvm.java

/**
 * 题库管理：列出所有导入的题库，支持查看题目、重命名、删除与统计。
 * 单击进入题目列表，长按弹出管理菜单。
 * @author Joker
 * @since 2026/08/11
 */
class BankListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    private val adapter = BankAdapter(
        onItemClick = { title -> openQuestions(title) },
        onItemLongClick = { title -> showBankMenu(title) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 15+ 强制使用边缘到边缘模式
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_bank_list)

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

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "题库管理"

        recyclerView = findViewById(R.id.recycler_view)
        emptyView = findViewById(R.id.tv_empty)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 添加可拖动的快速滚动条
        val scrollbarThumb = findViewById<View>(R.id.scrollbar_thumb)
        FastScroller(recyclerView, scrollbarThumb)
    }

    override fun onResume() {
        super.onResume()
        refresh() // 从题目列表返回后刷新题数等统计
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun refresh() {
        GlobalScope.async {
            val banks = AppDatabase.getInstance().inputTitleDao().getAll() ?: emptyList()
            val counts = banks.associate { it.id to AppDatabase.getInstance().inputDataDao().countByTitle(it.id) }
            val totalQuestions = counts.values.sum()
            CoroutineScope(Dispatchers.Main).launch {
                supportActionBar?.subtitle = "共 ${banks.size} 个题库 / $totalQuestions 道题"
                adapter.submitList(banks.map { it to (counts[it.id] ?: 0) })
                emptyView.isVisible = banks.isEmpty()
                recyclerView.isVisible = banks.isNotEmpty()
            }
        }
    }

    private fun openQuestions(title: InputTitle) {
        val intent = Intent(this, QuestionListActivity::class.java)
        intent.putExtra("titleId", title.id)
        intent.putExtra("fileName", title.fileName)
        startActivity(intent)
    }

    private fun showBankMenu(title: InputTitle) {
        XPopup.Builder(this)
            .asBottomList(title.fileName, arrayOf("重命名", "删除题库")) { position, _ ->
                when (position) {
                    0 -> renameBank(title)
                    1 -> deleteBank(title)
                }
            }
            .show()
    }

    private fun renameBank(title: InputTitle) {
        XPopup.Builder(this)
            .asInputConfirm("重命名题库", "请输入新的题库名称", title.fileName, "题库名称") { text ->
                if (text.isNullOrBlank()) {
                    toast("名称不能为空")
                    return@asInputConfirm
                }
                GlobalScope.async {
                    title.fileName = text.trim()
                    AppDatabase.getInstance().inputTitleDao().modify(title)
                    CoroutineScope(Dispatchers.Main).launch { refresh() }
                }
            }
            .show()
    }

    private fun deleteBank(title: InputTitle) {
        val count = AppDatabase.getInstance().inputDataDao().countByTitle(title.id)
        XPopup.Builder(this)
            .asConfirm("删除题库", "确定删除「${title.fileName}」及其 $count 道题目？\n此操作不可恢复。") {
                GlobalScope.async {
                    AppDatabase.getInstance().inputDataDao().removeByTitle(title.id)
                    AppDatabase.getInstance().inputTitleDao().remove(title)
                    CoroutineScope(Dispatchers.Main).launch {
                        toast("已删除「${title.fileName}」")
                        refresh()
                    }
                }
            }
            .show()
    }
}
