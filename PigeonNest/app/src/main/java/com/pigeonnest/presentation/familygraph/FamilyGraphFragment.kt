package com.pigeonnest.presentation.familygraph

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
import com.pigeonnest.R
import com.pigeonnest.databinding.FragmentFamilyGraphBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FamilyGraphFragment : Fragment() {

    private var _binding: FragmentFamilyGraphBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FamilyGraphViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFamilyGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pigeonId = arguments?.getString("pigeonId")
        if (pigeonId.isNullOrBlank()) {
            showEmptyState()
        } else {
            showGraph()
            viewModel.loadGraph(pigeonId)
            setupObservers()
        }
        setupListeners()
    }

    private fun showEmptyState() {
        binding.graphView.visibility = View.GONE
        binding.layoutControls.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
    }

    private fun showGraph() {
        binding.graphView.visibility = View.VISIBLE
        binding.layoutControls.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.graphData.collect { result ->
                    result?.let {
                        if (it.isSuccess) {
                            val layoutResult = it.getOrNull()
                            layoutResult?.let { lr ->
                                binding.graphView.setGraphData(lr)
                            }
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentDepth.collect { depth ->
                    binding.textDepthValue.text = depth.toString()
                    updateDepthButtonsState(depth)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.buttonZoomIn.setOnClickListener {
            binding.graphView.zoomIn()
        }

        binding.buttonZoomOut.setOnClickListener {
            binding.graphView.zoomOut()
        }

        binding.buttonCenter.setOnClickListener {
            binding.graphView.resetView()
        }

        binding.buttonDepthDecrease.setOnClickListener {
            viewModel.decreaseDepth()
        }

        binding.buttonDepthIncrease.setOnClickListener {
            viewModel.increaseDepth()
        }

        binding.buttonSelectPigeon.setOnClickListener {
            findNavController().navigate(R.id.pigeonListFragment)
        }

        binding.graphView.setOnNodeClickListener { pigeonId ->
            val action = FamilyGraphFragmentDirections
                .actionFamilyGraphToPigeonDetail(pigeonId)
            findNavController().navigate(action)
        }
    }

    private fun updateDepthButtonsState(depth: Int) {
        binding.buttonDepthDecrease.isEnabled = depth > FamilyGraphViewModel.MIN_DEPTH
        binding.buttonDepthIncrease.isEnabled = depth < FamilyGraphViewModel.MAX_DEPTH
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
