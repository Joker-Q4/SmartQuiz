package com.joker.smartquiz.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.joanzapata.iconify.widget.IconTextView
import com.joker.smartquiz.R
import com.joker.smartquiz.action.Action

/**
 * 主界面功能列表适配器：每个功能以「图标 + 标题 + 说明」的卡片展示。
 * @author Joker
 * @since 2026/08/11
 */
class ActionAdapter(
    private val onClick: (Action<*>) -> Unit
) : ListAdapter<Action<*>, ActionAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_action, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val action = getItem(position)
        holder.icon.text = iconFor(action.name)
        holder.title.text = action.name
        holder.subtitle.text = subtitleFor(action.name)
        holder.itemView.setOnClickListener { onClick(action) }
    }

    private fun iconFor(name: String): String = when (name) {
        "使用教程" -> "{fa-question-circle}"
        "获取权限" -> "{fa-key}"
        "自动扫描" -> "{fa-crosshairs}"
        "选择文件" -> "{fa-folder-open}"
        "题库管理" -> "{fa-database}"
        else -> "{fa-circle}"
    }

    private fun subtitleFor(name: String): String = when (name) {
        "使用教程" -> "首次使用？跟着 5 步快速上手"
        "获取权限" -> "开启无障碍服务，用于识别屏幕内容"
        "自动扫描" -> "选择题库，悬浮窗实时显示参考答案"
        "选择文件" -> "导入 xls / xlsx / csv 题库文件"
        "题库管理" -> "查看、重命名、删除题库与题目"
        else -> ""
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: IconTextView = view.findViewById(R.id.action_icon)
        val title: TextView = view.findViewById(R.id.action_title)
        val subtitle: TextView = view.findViewById(R.id.action_subtitle)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Action<*>>() {
            override fun areItemsTheSame(oldItem: Action<*>, newItem: Action<*>): Boolean =
                oldItem.name == newItem.name

            override fun areContentsTheSame(oldItem: Action<*>, newItem: Action<*>): Boolean =
                oldItem.name == newItem.name
        }
    }
}
