package com.pawel.memento.ui.categories

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pawel.memento.R
import com.pawel.memento.databinding.FragmentCategoryDetailBinding
import com.pawel.memento.ui.adapters.MementoAdapter
import com.pawel.memento.ui.home.HomeViewModel

class CategoryDetailFragment : Fragment() {
    private var _binding: FragmentCategoryDetailBinding? = null
    private val binding get() = _binding!!
    private val args: CategoryDetailFragmentArgs by navArgs()
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: MementoAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoryDetailBinding.inflate(inflater, container, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = args.categoryName
        adapter = MementoAdapter(
            onToggleComplete = { viewModel.toggleCompleted(it.memento) },
            onClick = {
                findNavController().navigate(
                    CategoryDetailFragmentDirections.actionCategoryDetailFragmentToMementoEditorFragment(
                        it.memento.id.toInt()
                    )
                )
            },
            onLongClick = { item ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_memento)
                    .setMessage(getString(R.string.delete_memento_confirm, item.memento.title))
                    .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteMemento(item.memento) }
                    .setNegativeButton(R.string.cancel, null).show()
                true
            }
        )
        binding.recyclerView.adapter = adapter
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(
                CategoryDetailFragmentDirections.actionCategoryDetailFragmentToMementoEditorFragment(0)
            )
        }
        viewModel.setSelectedCategory(args.categoryId)
        viewModel.mementos.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
