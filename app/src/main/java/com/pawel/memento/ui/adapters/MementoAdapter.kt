package com.pawel.memento.ui.adapters

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pawel.memento.R
import com.pawel.memento.data.model.MementoWithCategory
import com.pawel.memento.databinding.ItemMementoBinding
import com.pawel.memento.util.ColorUtils
import com.pawel.memento.util.DateTimeUtils
import java.util.Calendar

class MementoAdapter(
    private val onToggleComplete: (MementoWithCategory) -> Unit,
    private val onClick: (MementoWithCategory) -> Unit,
    private val onLongClick: (MementoWithCategory) -> Boolean,
    private val onToggleOccurrence: ((item: MementoWithCategory, occurrenceIndex: Int, checked: Boolean) -> Unit)? = null
) : ListAdapter<MementoWithCategory, MementoAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemMementoBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: MementoWithCategory) {
            val m = item.memento
            b.cardView.setCardBackgroundColor(ColorUtils.PASTEL_COLORS[m.colorTagIndex % ColorUtils.PASTEL_COLORS.size])
            b.viewPriorityBar.setBackgroundColor(ColorUtils.PRIORITY_COLORS[m.priority] ?: 0xFF888888.toInt())
            b.tvTitle.text = m.title
            if (m.isCompleted) {
                b.tvTitle.paintFlags = b.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                b.tvTitle.alpha = 0.5f
            } else {
                b.tvTitle.paintFlags = b.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                b.tvTitle.alpha = 1f
            }
            b.tvDescription.text = m.description
            b.tvDescription.isVisible = m.description.isNotBlank()
            if (m.dueDateTime != null) {
                b.tvDueDate.isVisible = true
                b.tvDueDate.text = DateTimeUtils.formatDateTime(m.dueDateTime)
                b.tvDueDate.setTextColor(
                    if (DateTimeUtils.isOverdue(m.dueDateTime) && !m.isCompleted) 0xFFB71C1C.toInt()
                    else 0xFF555555.toInt()
                )
            } else b.tvDueDate.isVisible = false
            item.category?.let {
                b.tvCategory.isVisible = true; b.tvCategory.text = it.name
                b.tvCategory.setBackgroundColor(ColorUtils.CATEGORY_COLORS[it.colorIndex % ColorUtils.CATEGORY_COLORS.size])
            } ?: run { b.tvCategory.isVisible = false }
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val activeMask = if (m.lastCompletedDate >= todayStart) m.completionMask else 0
            if (m.dailyCount > 1 && onToggleOccurrence != null) {
                b.checkCompleted.isVisible = false
                b.llOccurrences.isVisible = true
                b.llOccurrences.removeAllViews()
                val tint = ContextCompat.getColorStateList(b.root.context, R.color.secondary_variant)
                    ?: ColorStateList.valueOf(0xFF888888.toInt())
                for (i in 0 until m.dailyCount) {
                    val cb = CheckBox(b.root.context).apply {
                        isChecked = (activeMask shr i) and 1 == 1
                        buttonTintList = tint
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        setOnClickListener { onToggleOccurrence.invoke(item, i, isChecked) }
                    }
                    b.llOccurrences.addView(cb)
                }
            } else {
                b.checkCompleted.isVisible = true
                b.llOccurrences.isVisible = false
                b.checkCompleted.isChecked = m.isCompleted
                b.checkCompleted.setOnClickListener { onToggleComplete(item) }
            }
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
