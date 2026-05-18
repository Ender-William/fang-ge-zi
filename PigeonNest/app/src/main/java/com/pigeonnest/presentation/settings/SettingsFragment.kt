package com.pigeonnest.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pigeonnest.R
import com.pigeonnest.databinding.DialogAboutBinding
import com.pigeonnest.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    private val pickBackupLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importBackup(it)
        } ?: Toast.makeText(requireContext(), "未选择文件", Toast.LENGTH_SHORT).show()
    }

    private val createBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportBackup(it)
        } ?: Toast.makeText(requireContext(), "未选择保存位置", Toast.LENGTH_SHORT).show()
    }

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
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.CHINA)
                .format(java.util.Date())
            createBackupLauncher.launch("pigeonnest_backup_${timestamp}.zip")
        }

        binding.buttonImport.setOnClickListener {
            pickBackupLauncher.launch("application/zip")
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
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.importSuccess.collect { success ->
                    if (success) {
                        viewModel.clearImportSuccess()
                        showImportRestartDialog()
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

    private fun showImportRestartDialog() {
        AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
            .setTitle("导入完成")
            .setMessage("数据已成功恢复。应用需要重启才能完全生效。")
            .setPositiveButton("立即重启") { _, _ ->
                val intent = requireActivity().intent
                requireActivity().finish()
                startActivity(intent)
            }
            .setNegativeButton("稍后手动重启", null)
            .show()
    }

    private fun showAboutDialog() {
        val dialogBinding = DialogAboutBinding.inflate(LayoutInflater.from(requireContext()))
        dialogBinding.textGithub.movementMethod = LinkMovementMethod.getInstance()
        AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
            .setTitle("关于放鸽子")
            .setView(dialogBinding.root)
            .setPositiveButton("确定", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
