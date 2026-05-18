package com.pigeonnest.presentation.familylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pigeonnest.R
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
            },
            onEditClick = { family ->
                showEditFamilyNameDialog(family)
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

    private fun showEditFamilyNameDialog(family: com.pigeonnest.domain.model.FamilyGroup) {
        val editText = EditText(requireContext()).apply {
            setText(family.customName ?: family.displayName)
            hint = "输入家族名称"
        }
        AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
            .setTitle("编辑家族名称")
            .setView(editText)
            .setPositiveButton("保存") { _, _ ->
                val name = editText.text.toString().trim()
                viewModel.setCustomFamilyName(family.rootPigeonId, name)
                Toast.makeText(requireContext(), "家族名称已更新", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .setNeutralButton("恢复默认") { _, _ ->
                viewModel.setCustomFamilyName(family.rootPigeonId, null)
                Toast.makeText(requireContext(), "已恢复默认名称", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
