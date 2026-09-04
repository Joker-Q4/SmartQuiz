package com.joker.smartquiz.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.joker.smartquiz.R
import com.joker.smartquiz.database.entity.InputData

/**
 * 题目列表适配器。
 * @param onItemClick   单击：查看题目详情
 * @param onItemLongClick 长按：删除该题
 * @author Joker
 * @since 2026/08/11
 */
class QuestionAdapter(
    private val onItemClick: (InputData) -> Unit,
    private val onItemLongClick: (InputData) -> Unit
) : ListAdapter<InputData, QuestionAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_question, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val data = getItem(position)
        // 编号 + 题干
        holder.question.text = holder.itemView.context.getString(R.string.question_number_format, position + 1, data.question ?: "")
        // 所有选项
        val options = listOf(data.col_a, data.col_b, data.col_c, data.col_d, data.col_e, data.col_f)
            .filter { !it.isNullOrBlank() }
            .joinToString("\n")
        holder.options.text = options
        holder.options.isVisible = options.isNotEmpty()
        // 答案
        holder.answer.text = holder.itemView.context.getString(R.string.answer_format, data.answer ?: "")
        holder.itemView.setOnClickListener { onItemClick(data) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(data)
            true
        }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val question: TextView = view.findViewById(R.id.tv_question)
        val options: TextView = view.findViewById(R.id.tv_options)
        val answer: TextView = view.findViewById(R.id.tv_answer)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<InputData>() {
            override fun areItemsTheSame(oldItem: InputData, newItem: InputData): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: InputData, newItem: InputData): Boolean =
                oldItem.question == newItem.question &&
                        oldItem.answer == newItem.answer &&
                        oldItem.col_a == newItem.col_a &&
                        oldItem.col_b == newItem.col_b &&
                        oldItem.col_c == newItem.col_c &&
                        oldItem.col_d == newItem.col_d &&
                        oldItem.col_e == newItem.col_e &&
                        oldItem.col_f == newItem.col_f
        }
    }
}
