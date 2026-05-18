package com.pigeonnest.presentation.locationset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.pigeonnest.databinding.FragmentLocationSetBinding
import com.pigeonnest.domain.model.Loft
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationSetFragment : Fragment() {

    private var _binding: FragmentLocationSetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationSetViewModel by viewModels()
    private val args: LocationSetFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationSetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        val adapter = LoftSelectAdapter { loft ->
            showConfirmDialog(loft)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lofts.collect { lofts ->
                    (binding.recyclerView.adapter as? LoftSelectAdapter)?.submitList(lofts)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showConfirmDialog(loft: Loft) {
        AlertDialog.Builder(requireContext())
            .setTitle("确认变更位置")
            .setMessage("确定将鸽子移动到「${loft.name}」？")
            .setPositiveButton("确认") { _, _ ->
                viewModel.updateLocation(args.pigeonId, loft.id) { success, msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        findNavController().navigateUp()
                    }
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
