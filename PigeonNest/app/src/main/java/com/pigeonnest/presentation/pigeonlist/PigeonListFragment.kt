package com.pigeonnest.presentation.pigeonlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pigeonnest.R
import com.pigeonnest.databinding.FragmentPigeonListBinding
import com.pigeonnest.domain.model.Pigeon
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PigeonListFragment : Fragment() {

    private var _binding: FragmentPigeonListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PigeonListViewModel by viewModels()
    private lateinit var adapter: PigeonListAdapter
    private lateinit var recentAdapter: RecentPigeonAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPigeonListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupRecentRecyclerView()
        setupSearch()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = PigeonListAdapter(
            onItemClick = { pigeon ->
                val action = PigeonListFragmentDirections
                    .actionPigeonListToPigeonDetail(pigeon.id)
                findNavController().navigate(action)
            },
            onItemLongClick = { pigeon ->
                showPigeonOptionsDialog(pigeon)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupRecentRecyclerView() {
        recentAdapter = RecentPigeonAdapter(
            onItemClick = { pigeon ->
                val action = PigeonListFragmentDirections
                    .actionPigeonListToPigeonDetail(pigeon.id)
                findNavController().navigate(action)
            }
        )
        binding.recyclerRecent.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        binding.recyclerRecent.adapter = recentAdapter
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText ?: "")
                return true
            }
        })
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is PigeonListUiState.Loading -> showLoading()
                        is PigeonListUiState.Success -> showPigeons(state.pigeons)
                        is PigeonListUiState.Empty -> showEmpty()
                        is PigeonListUiState.Error -> showError(state.message)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recentPigeons.collect { recent ->
                    if (recent.isEmpty()) {
                        binding.labelRecent.visibility = View.GONE
                        binding.recyclerRecent.visibility = View.GONE
                    } else {
                        binding.labelRecent.visibility = View.VISIBLE
                        binding.recyclerRecent.visibility = View.VISIBLE
                        recentAdapter.submitList(recent)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.fabAddPigeon.setOnClickListener {
            val action = PigeonListFragmentDirections
                .actionPigeonListToPigeonEdit(null)
            findNavController().navigate(action)
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
    }

    private fun showPigeons(pigeons: List<Pigeon>) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        adapter.submitList(pigeons)
    }

    private fun showEmpty() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
    }

    private fun showError(@Suppress("UNUSED_PARAMETER") message: String) {
        binding.progressBar.visibility = View.GONE
    }

    private fun showPigeonOptionsDialog(pigeon: Pigeon) {
        AlertDialog.Builder(requireContext())
            .setTitle(pigeon.name)
            .setItems(arrayOf("查看详情", "编辑", "删除")) { _, which ->
                when (which) {
                    0 -> {
                        val action = PigeonListFragmentDirections
                            .actionPigeonListToPigeonDetail(pigeon.id)
                        findNavController().navigate(action)
                    }
                    1 -> {
                        val action = PigeonListFragmentDirections
                            .actionPigeonListToPigeonEdit(pigeon.id)
                        findNavController().navigate(action)
                    }
                    2 -> showDeleteConfirmDialog(pigeon)
                }
            }
            .show()
    }

    private fun showDeleteConfirmDialog(pigeon: Pigeon) {
        AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
            .setTitle("确认删除？")
            .setMessage("删除后将无法恢复。鸽子「${pigeon.name}」及其家族关系都将被删除。")
            .setPositiveButton("确认删除") { _, _ ->
                viewModel.deletePigeon(pigeon.id)
            }
            .setNegativeButton("先不删", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
