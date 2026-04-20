package com.pawel.memento.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pawel.memento.data.model.Category
import com.pawel.memento.databinding.ItemCategoryBinding
import com.pawel.memento.util.ColorUtils

class CategoryAdapter(
    private val onClick: (Category) -> Unit,
    private val onEdit: (Category) -> Unit,
    private val onDelete: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemCategoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(cat: Category) {
            b.tvCategoryName.text = cat.name
            b.viewColorDot.setBackgroundColor(ColorUtils.CATEGORY_COLORS[cat.colorIndex % ColorUtils.CATEGORY_COLORS.size])
            b.root.setOnClickListener { onClick(cat) }
            b.btnEdit.setOnClickListener { onEdit(cat) }
            b.btnDelete.setOnClickListener { onDelete(cat) }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(a: Category, b: Category) = a.id == b.id
            override fun areContentsTheSame(a: Category, b: Category) = a == b
        }
    }
}
