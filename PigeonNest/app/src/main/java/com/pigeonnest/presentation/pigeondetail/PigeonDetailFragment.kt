package com.pigeonnest.presentation.pigeondetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pigeonnest.R
import com.pigeonnest.data.file.PhotoStorageManager
import com.pigeonnest.databinding.FragmentPigeonDetailBinding
import com.pigeonnest.presentation.common.ImageViewerDialogFragment
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.model.Pigeon
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class PigeonDetailFragment : Fragment() {

    private var _binding: FragmentPigeonDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PigeonDetailViewModel by viewModels()
    private val args: PigeonDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPigeonDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadPigeon(args.pigeonId)
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pigeon.collect { pigeon ->
                    pigeon?.let { bindPigeon(it) }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.familyRelation.collect { relation ->
                    bindFamilyRelation(relation)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateResult.collect { result ->
                    result?.let {
                        if (it.isSuccess) {
                            Toast.makeText(requireContext(), "家族关系已更新", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                it.exceptionOrNull()?.message ?: "更新失败",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        viewModel.clearUpdateResult()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteResult.collect { result ->
                    result?.let {
                        if (it.isSuccess) {
                            Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                it.exceptionOrNull()?.message ?: "删除失败",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        viewModel.clearDeleteResult()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonEdit.setOnClickListener {
            val action = PigeonDetailFragmentDirections
                .actionPigeonDetailToPigeonEdit(args.pigeonId)
            findNavController().navigate(action)
        }

        binding.buttonDelete.setOnClickListener {
            viewModel.pigeon.value?.let { showDeleteConfirmDialog(it) }
        }

        binding.buttonEditFamily.setOnClickListener {
            showEditFamilyDialog()
        }

        binding.buttonViewFamily.setOnClickListener {
            val action = PigeonDetailFragmentDirections
                .actionPigeonDetailToFamilyGraph(args.pigeonId)
            findNavController().navigate(action)
        }

        binding.buttonChangeLocation.setOnClickListener {
            val action = PigeonDetailFragmentDirections
                .actionPigeonDetailToLocationSet(args.pigeonId)
            findNavController().navigate(action)
        }

        binding.buttonEditFamily.setOnClickListener {
            showEditFamilyDialog()
        }
    }

    private fun bindPigeon(pigeon: Pigeon) {
        binding.textName.text = pigeon.name
        binding.textRingNumber.text = "脚环号: ${pigeon.ringNumber}"

        val genderColor = when (pigeon.gender) {
            Gender.MALE -> ContextCompat.getColor(requireContext(), R.color.male_blue)
            Gender.FEMALE -> ContextCompat.getColor(requireContext(), R.color.female_pink)
            else -> ContextCompat.getColor(requireContext(), R.color.neutral_gray)
        }
        binding.imageAvatar.setBorderColor(genderColor)

        binding.imageAvatar.setBorderColor(genderColor)
        binding.imageEyePhoto.setBorderColor(genderColor)

        PhotoStorageManager(requireContext()).loadPhoto(
            binding.imageAvatar,
            pigeon.photoPath
        )

        // 点击全身照放大查看
        pigeon.photoPath?.let { path ->
            binding.imageAvatar.setOnClickListener {
                ImageViewerDialogFragment.newInstance(path, pigeon.name)
                    .show(parentFragmentManager, "image_viewer")
            }
        }

        PhotoStorageManager(requireContext()).loadPhoto(
            binding.imageEyePhoto,
            pigeon.eyePhotoPath
        )

        // 点击眼睛照片放大查看
        pigeon.eyePhotoPath?.let { path ->
            binding.imageEyePhoto.setOnClickListener {
                ImageViewerDialogFragment.newInstance(path, "${pigeon.name} - 眼睛照片")
                    .show(parentFragmentManager, "image_viewer_eye")
            }
        }

        val infoBuilder = StringBuilder()
        infoBuilder.append("品种: ${pigeon.color ?: "未记录"}")
        infoBuilder.append(" · ${pigeon.gender.displayName}")
        pigeon.birthDate?.let {
            infoBuilder.append(" · ${calculateAge(it)}岁")
        }
        binding.chipBasicInfo.text = infoBuilder.toString()

        binding.textLocation.text = if (pigeon.loft != null) {
            "${pigeon.loft.name}${pigeon.cageNumber?.let { "-$it" } ?: ""}"
        } else {
            "未分配鸽舍"
        }

        binding.textStatus.text = "状态: ${pigeon.status.displayName}"

        binding.textNotes.text = pigeon.notes ?: "暂无备注"
    }

    private fun bindFamilyRelation(relation: com.pigeonnest.domain.model.FamilyRelation?) {
        if (relation == null) {
            binding.textFather.text = "父亲: 未记录"
            binding.textMother.text = "母亲: 未记录"
            binding.textMate.text = "配偶: 未记录"
            binding.textChildren.text = "后代: 未记录"
            return
        }

        binding.textFather.text = relation.father?.let {
            "父亲: ${it.name} (${it.ringNumber})"
        } ?: "父亲: 未记录"

        binding.textMother.text = relation.mother?.let {
            "母亲: ${it.name} (${it.ringNumber})"
        } ?: "母亲: 未记录"

        binding.textMate.text = relation.mate?.let {
            "配偶: ${it.name} (${it.ringNumber})"
        } ?: "配偶: 未记录"

        binding.textChildren.text = if (relation.children.isNotEmpty()) {
            "后代: ${relation.children.joinToString(", ") { it.name }}"
        } else {
            "后代: 未记录"
        }
    }

    private fun showEditFamilyDialog() {
        val currentPigeon = viewModel.pigeon.value ?: return
        val allPigeons = viewModel.allPigeons.value
        val currentRelation = viewModel.familyRelation.value

        // Filter out current pigeon
        val otherPigeons = allPigeons.filter { it.id != currentPigeon.id }

        val malePigeons = otherPigeons.filter { it.gender == Gender.MALE }
        val femalePigeons = otherPigeons.filter { it.gender == Gender.FEMALE }
        val allForMate = otherPigeons.filter { it.gender != currentPigeon.gender || currentPigeon.gender == Gender.UNKNOWN }

        // Build dialog items
        val fatherNames = arrayOf("不设置") + malePigeons.map { "${it.name} (${it.ringNumber})" }.toTypedArray()
        val fatherIds = arrayOf<String?>(null) + malePigeons.map { it.id }.toTypedArray()
        val currentFatherIndex = currentRelation?.father?.let { f ->
            fatherIds.indexOfFirst { it == f.id }.coerceAtLeast(0)
        } ?: 0

        val motherNames = arrayOf("不设置") + femalePigeons.map { "${it.name} (${it.ringNumber})" }.toTypedArray()
        val motherIds = arrayOf<String?>(null) + femalePigeons.map { it.id }.toTypedArray()
        val currentMotherIndex = currentRelation?.mother?.let { m ->
            motherIds.indexOfFirst { it == m.id }.coerceAtLeast(0)
        } ?: 0

        val mateNames = arrayOf("不设置") + allForMate.map { "${it.name} (${it.ringNumber})" }.toTypedArray()
        val mateIds = arrayOf<String?>(null) + allForMate.map { it.id }.toTypedArray()
        val currentMateIndex = currentRelation?.mate?.let { m ->
            mateIds.indexOfFirst { it == m.id }.coerceAtLeast(0)
        } ?: 0

        // Track selections
        var selectedFatherIndex = currentFatherIndex
        var selectedMotherIndex = currentMotherIndex
        var selectedMateIndex = currentMateIndex

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_family, null)

        // Father selection
        val btnFather = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.button_select_father)
        btnFather?.text = fatherNames[selectedFatherIndex]
        btnFather?.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
                .setTitle("选择父亲 (雄鸽)")
                .setSingleChoiceItems(fatherNames, selectedFatherIndex) { _, which ->
                    selectedFatherIndex = which
                }
                .setPositiveButton("确定") { _, _ ->
                    btnFather.text = fatherNames[selectedFatherIndex]
                }
                .setNegativeButton("取消", null)
                .show()
        }

        // Mother selection
        val btnMother = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.button_select_mother)
        btnMother?.text = motherNames[selectedMotherIndex]
        btnMother?.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
                .setTitle("选择母亲 (雌鸽)")
                .setSingleChoiceItems(motherNames, selectedMotherIndex) { _, which ->
                    selectedMotherIndex = which
                }
                .setPositiveButton("确定") { _, _ ->
                    btnMother.text = motherNames[selectedMotherIndex]
                }
                .setNegativeButton("取消", null)
                .show()
        }

        // Mate selection
        val btnMate = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.button_select_mate)
        btnMate?.text = mateNames[selectedMateIndex]
        btnMate?.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
                .setTitle("选择配偶")
                .setSingleChoiceItems(mateNames, selectedMateIndex) { _, which ->
                    selectedMateIndex = which
                }
                .setPositiveButton("确定") { _, _ ->
                    btnMate.text = mateNames[selectedMateIndex]
                }
                .setNegativeButton("取消", null)
                .show()
        }

        AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
            .setTitle("编辑家族关系")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                viewModel.updateFamilyRelation(
                    pigeonId = currentPigeon.id,
                    fatherId = fatherIds[selectedFatherIndex],
                    motherId = motherIds[selectedMotherIndex],
                    mateId = mateIds[selectedMateIndex]
                )
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun calculateAge(birthDateMillis: Long): Int {
        val birthYear = SimpleDateFormat("yyyy", Locale.CHINA).format(Date(birthDateMillis)).toInt()
        val currentYear = SimpleDateFormat("yyyy", Locale.CHINA).format(Date()).toInt()
        return maxOf(0, currentYear - birthYear)
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
