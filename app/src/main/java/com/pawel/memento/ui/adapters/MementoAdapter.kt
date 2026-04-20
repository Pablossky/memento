package com.pawel.memento.ui.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pawel.memento.data.model.MementoWithCategory
import com.pawel.memento.databinding.ItemMementoBinding
import com.pawel.memento.util.ColorUtils
import com.pawel.memento.util.DateTimeUtils

class MementoAdapter(
    private val onToggleComplete: (MementoWithCategory) -> Unit,
    private val onClick: (MementoWithCategory) -> Unit,
    private val onLongClick: (MementoWithCategory) -> Boolean
) : ListAdapter<MementoWithCategory, MementoAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemMementoBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: MementoWithCategory) {
            val m = item.memento
            b.cardView.setCardBackgroundColor(ColorUtils.PASTEL_COLORS[m.colorTagIndex % ColorUtils.PASTEL_COLORS.size])
            b.viewPriorityBar.setBackgroundColor(ColorUtils.PRIORITY_COLORS[m.priority] ?: 0xFF888888.toInt())
            b.tvTitle.text = m.title
            b.tvDescription.text = m.description
            b.tvDescription.isVisible = m.description.isNotBlank()
            if (m.dueDateTime != null) {
                b.tvDueDate.isVisible = true
                b.tvDueDate.text = DateTimeUtils.formatDateTime(m.dueDateTime)
                b.tvDueDate.setTextColor(if (DateTimeUtils.isOverdue(m.dueDateTime) && !m.isCompleted) 0xFFB71C1C.toInt() else 0xFF555555.toInt())
            } else b.tvDueDate.isVisible = false
            item.category?.let {
                b.tvCategory.isVisible = true; b.tvCategory.text = it.name
                b.tvCategory.setBackgroundColor(ColorUtils.CATEGORY_COLORS[it.colorIndex % ColorUtils.CATEGORY_COLORS.size])
            } ?: run { b.tvCategory.isVisible = false }
            b.checkCompleted.isChecked = m.isCompleted
            if (m.isCompleted) { b.tvTitle.paintFlags = b.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG; b.tvTitle.alpha = 0.5f }
            else { b.tvTitle.paintFlags = b.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv(); b.tvTitle.alpha = 1f }
            b.checkCompleted.setOnClickListener { onToggleComplete(item) }
            b.root.setOnClickListener { onClick(item) }
            b.root.setOnLongClickListener { onLongClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemMementoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MementoWithCategory>() {
            override fun areItemsTheSame(a: MementoWithCategory, b: MementoWithCategory) = a.memento.id == b.memento.id
            override fun areContentsTheSame(a: MementoWithCategory, b: MementoWithCategory) = a == b
        }
    }
}
