package com.pigeonnest.presentation.settings

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
import com.pigeonnest.R
import com.pigeonnest.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.buttonFontSize.setOnClickListener {
            showFontSizeDialog()
        }

        binding.switchHighContrast.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setHighContrast(isChecked)
        }

        binding.buttonExport.setOnClickListener {
            viewModel.exportBackup()
        }

        binding.buttonImport.setOnClickListener {
            Toast.makeText(requireContext(), "请选择备份文件", Toast.LENGTH_SHORT).show()
        }

        binding.buttonAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { settings ->
                    binding.textFontSizeValue.text = settings.fontSizeLabel
                    // 临时移除 listener 避免代码设置时触发回调导致循环
                    binding.switchHighContrast.setOnCheckedChangeListener(null)
                    binding.switchHighContrast.isChecked = settings.highContrast
                    binding.switchHighContrast.setOnCheckedChangeListener { _, isChecked ->
                        viewModel.setHighContrast(isChecked)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.toastMessage.collect { message ->
                    message?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recreateNeeded.collect { needed ->
                    if (needed) {
                        viewModel.clearRecreateFlag()
                        activity?.recreate()
                    }
                }
            }
        }
    }

    private fun showFontSizeDialog() {
        val sizes = arrayOf("标准", "大", "超大")
        AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
            .setTitle("字体大小")
            .setItems(sizes) { _, which ->
                viewModel.setFontSize(which)
            }
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
            .setTitle("关于放鸽子")
            .setMessage("版本 1.0.0\n\n专为养鸽爱好者设计\n\n用心记录每一羽，温暖守护鸽家族")
            .setPositiveButton("确定", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
