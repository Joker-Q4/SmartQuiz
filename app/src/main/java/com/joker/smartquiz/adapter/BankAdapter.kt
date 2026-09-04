package com.joker.smartquiz.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.joker.smartquiz.R
import com.joker.smartquiz.database.entity.InputTitle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 题库列表适配器。
 * @param onItemClick   单击：进入题目列表
 * @param onItemLongClick 长按：弹出管理菜单（重命名/删除）
 * @author Joker
 * @since 2026/08/11
 */
class BankAdapter(
    private val onItemClick: (InputTitle) -> Unit,
    private val onItemLongClick: (InputTitle) -> Unit
) : ListAdapter<Pair<InputTitle, Int>, BankAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bank, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (title, count) = getItem(position)
        holder.name.text = title.fileName
        holder.info.text = holder.itemView.context.getString(R.string.bank_info_format, count, formatTime(title.currentTime))
        holder.itemView.setOnClickListener { onItemClick(title) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(title)
            true
        }
    }

    private fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tv_bank_name)
        val info: TextView = view.findViewById(R.id.tv_bank_info)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Pair<InputTitle, Int>>() {
            override fun areItemsTheSame(oldItem: Pair<InputTitle, Int>, newItem: Pair<InputTitle, Int>): Boolean =
                oldItem.first.id == newItem.first.id

            override fun areContentsTheSame(oldItem: Pair<InputTitle, Int>, newItem: Pair<InputTitle, Int>): Boolean =
                oldItem.first.id == newItem.first.id &&
                        oldItem.first.fileName == newItem.first.fileName &&
                        oldItem.first.currentTime == newItem.first.currentTime &&
                        oldItem.second == newItem.second
        }
    }
}
