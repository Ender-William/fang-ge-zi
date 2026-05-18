package com.pigeonnest.presentation.familylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pigeonnest.databinding.FragmentFamilyListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FamilyListFragment : Fragment() {

    private var _binding: FragmentFamilyListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FamilyListViewModel by viewModels()
    private lateinit var adapter: FamilyListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFamilyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        viewModel.loadFamilies()
    }

    private fun setupRecyclerView() {
        adapter = FamilyListAdapter(
            onItemClick = { family ->
                val action = FamilyListFragmentDirections
                    .actionFamilyListToFamilyGraph(family.rootPigeonId)
                findNavController().navigate(action)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.families.collect { families ->
                    adapter.submitList(families)
                    binding.emptyView.visibility = if (families.isEmpty()) View.VISIBLE else View.GONE
                    binding.recyclerView.visibility = if (families.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { loading ->
                    binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
