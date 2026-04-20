package com.pawel.memento.ui.home

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pawel.memento.R
import com.pawel.memento.data.model.SortOrder
import com.pawel.memento.databinding.FragmentHomeBinding
import com.pawel.memento.ui.adapters.MementoAdapter

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: MementoAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MementoAdapter(
            onToggleComplete = { viewModel.toggleCompleted(it.memento) },
            onClick = { findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMementoEditorFragment(it.memento.id)) },
            onLongClick = { item ->
                MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.delete_memento)
                    .setMessage(getString(R.string.delete_memento_confirm, item.memento.title))
                    .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteMemento(item.memento) }
                    .setNegativeButton(R.string.cancel, null).show(); true
            }
        )
        binding.recyclerView.adapter = adapter
        binding.chipAll.setOnClickListener      { viewModel.setFilterTab(FilterTab.ALL) }
        binding.chipToday.setOnClickListener    { viewModel.setFilterTab(FilterTab.TODAY) }
        binding.chipUpcoming.setOnClickListener { viewModel.setFilterTab(FilterTab.UPCOMING) }
        binding.chipCompleted.setOnClickListener{ viewModel.setFilterTab(FilterTab.COMPLETED) }
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMementoEditorFragment(0L))
        }
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_home, menu)
                val sv = menu.findItem(R.id.action_search).actionView as SearchView
                sv.queryHint = getString(R.string.search_hint)
                sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(q: String?) = false
                    override fun onQueryTextChange(q: String?): Boolean { viewModel.setSearchQuery(q.orEmpty()); return true }
                })
            }
            override fun onMenuItemSelected(item: MenuItem) = when (item.itemId) {
                R.id.sort_by_date      -> { viewModel.setSortOrder(SortOrder.BY_DATE_ASC); true }
                R.id.sort_by_date_desc -> { viewModel.setSortOrder(SortOrder.BY_DATE_DESC); true }
                R.id.sort_by_priority  -> { viewModel.setSortOrder(SortOrder.BY_PRIORITY_DESC); true }
                R.id.sort_by_title     -> { viewModel.setSortOrder(SortOrder.BY_TITLE_ASC); true }
                R.id.sort_by_created   -> { viewModel.setSortOrder(SortOrder.BY_CREATED_DESC); true }
                else -> false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewModel.mementos.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            while (binding.chipGroupCategories.childCount > 1) binding.chipGroupCategories.removeViewAt(1)
            categories.forEach { category ->
                binding.chipGroupCategories.addView(Chip(requireContext()).apply {
                    text = category.name; isCheckable = true
                    setOnClickListener {
                        viewModel.setSelectedCategory(if (viewModel.selectedCategory.value == category.id) null else category.id)
                    }
                })
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
