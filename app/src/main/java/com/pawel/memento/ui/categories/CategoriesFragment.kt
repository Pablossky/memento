package com.pawel.memento.ui.categories

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.pawel.memento.R
import com.pawel.memento.data.model.Category
import com.pawel.memento.databinding.FragmentCategoriesBinding
import com.pawel.memento.ui.adapters.CategoryAdapter
import com.pawel.memento.util.ColorUtils

class CategoriesFragment : Fragment() {
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoriesViewModel by viewModels()
    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CategoryAdapter(onClick = {}, onEdit = { showDialog(it) }, onDelete = { confirmDelete(it) })
        binding.recyclerView.adapter = adapter
        binding.fabAddCategory.setOnClickListener { showDialog(null) }
        viewModel.allCategories.observe(viewLifecycleOwner) { cats ->
            adapter.submitList(cats)
            binding.tvEmpty.visibility = if (cats.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showDialog(existing: Category?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etCategoryName)
        var selectedColor = existing?.colorIndex ?: 0
        if (existing != null) etName.setText(existing.name)
        val cg = dialogView.findViewById<ChipGroup>(R.id.chipGroupColor)
        ColorUtils.CATEGORY_COLORS.forEachIndexed { i, color ->
            cg.addView(Chip(requireContext()).apply {
                isCheckable = true; isChecked = (i == selectedColor)
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(color); text = ""
                setOnClickListener { selectedColor = i }
            })
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) R.string.add_category else R.string.edit_category)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isBlank()) return@setPositiveButton
                if (existing == null) viewModel.insertCategory(Category(name = name, colorIndex = selectedColor))
                else viewModel.updateCategory(existing.copy(name = name, colorIndex = selectedColor))
            }
            .setNegativeButton(R.string.cancel, null).show()
    }

    private fun confirmDelete(cat: Category) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_category)
            .setMessage(getString(R.string.delete_category_confirm, cat.name))
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteCategory(cat) }
            .setNegativeButton(R.string.cancel, null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
