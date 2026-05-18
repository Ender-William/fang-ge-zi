package com.pigeonnest.presentation.loftmanage

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.pigeonnest.databinding.FragmentLoftDetailBinding
import com.pigeonnest.presentation.pigeonlist.PigeonListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoftDetailFragment : Fragment() {

    private var _binding: FragmentLoftDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoftDetailViewModel by viewModels()
    private val args: LoftDetailFragmentArgs by navArgs()
    private lateinit var adapter: PigeonListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoftDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.loadLoft(args.loftId)
    }

    private fun setupRecyclerView() {
        adapter = PigeonListAdapter(
            onItemClick = { pigeon ->
                val action = LoftDetailFragmentDirections
                    .actionLoftDetailToPigeonDetail(pigeon.id)
                findNavController().navigate(action)
            },
            onItemLongClick = { /* no-op */ }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loft.collect { loft ->
                    loft?.let {
                        binding.textLoftName.text = it.name
                        binding.textLoftInfo.text = buildString {
                            it.location?.let { loc -> append("位置: $loc") }
                            it.description?.let { desc ->
                                if (isNotEmpty()) append(" · ")
                                append(desc)
                            }
                        }.ifEmpty { "暂无位置信息" }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pigeons.collect { pigeons ->
                    adapter.submitList(pigeons)
                    binding.textPigeonCount.text = "共 ${pigeons.size} 羽鸽子"
                    binding.emptyView.visibility = if (pigeons.isEmpty()) View.VISIBLE else View.GONE
                    binding.recyclerView.visibility = if (pigeons.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
