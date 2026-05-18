package com.pigeonnest.presentation.loftmanage

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
import com.pigeonnest.databinding.FragmentLoftListBinding
import com.pigeonnest.domain.model.Loft
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoftListFragment : Fragment() {

    private var _binding: FragmentLoftListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoftListViewModel by viewModels()
    private lateinit var adapter: LoftListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoftListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = LoftListAdapter(
            onItemClick = { loft ->
                val action = LoftListFragmentDirections
                    .actionLoftListToLoftDetail(loft.id)
                findNavController().navigate(action)
            },
            onItemLongClick = { loft ->
                showLoftOptions(loft)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lofts.collect { lofts ->
                    adapter.submitList(lofts)
                    binding.emptyView.visibility = if (lofts.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun setupListeners() {
        binding.fabAddLoft.setOnClickListener {
            showAddLoftDialog()
        }
    }

    private fun showAddLoftDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "鸽舍名称"
        }
        AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
            .setTitle("添加鸽舍")
            .setView(editText)
            .setPositiveButton("添加") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotBlank()) {
                    viewModel.addLoft(name)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showLoftOptions(loft: Loft) {
        AlertDialog.Builder(requireContext())
            .setTitle(loft.name)
            .setItems(arrayOf("编辑", "删除")) { _, which ->
                when (which) {
                    0 -> showEditLoftDialog(loft)
                    1 -> showDeleteConfirm(loft)
                }
            }
            .show()
    }

    private fun showEditLoftDialog(loft: Loft) {
        val editText = EditText(requireContext()).apply {
            setText(loft.name)
        }
        AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
            .setTitle("编辑鸽舍")
            .setView(editText)
            .setPositiveButton("保存") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotBlank()) {
                    viewModel.updateLoft(loft.copy(name = name))
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showDeleteConfirm(loft: Loft) {
        AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
            .setTitle("确认删除？")
            .setMessage("删除鸽舍「${loft.name}」？如果其中有鸽子将无法删除。")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteLoft(loft.id) { success, msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
