package com.pigeonnest.presentation.pigeonedit

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.pigeonnest.R
import com.pigeonnest.databinding.FragmentPigeonEditBinding
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.model.PigeonStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class PigeonEditFragment : Fragment() {

    private var _binding: FragmentPigeonEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PigeonEditViewModel by viewModels()
    private val args: PigeonEditFragmentArgs by navArgs()

    private var currentPhotoFile: File? = null
    private var currentEyePhotoFile: File? = null

    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.setPhotoUri(it)
            binding.imagePreview.setImageURI(it)
            binding.imagePreview.visibility = View.VISIBLE
        }
    }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            currentPhotoFile?.let { file ->
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )
                viewModel.setPhotoUri(uri)
                binding.imagePreview.setImageURI(uri)
                binding.imagePreview.visibility = View.VISIBLE
            }
        }
    }

    private val pickEyePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.setEyePhotoUri(it)
            binding.imageEyePreview.setImageURI(it)
            binding.imageEyePreview.visibility = View.VISIBLE
        }
    }

    private val takeEyePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            currentEyePhotoFile?.let { file ->
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )
                viewModel.setEyePhotoUri(uri)
                binding.imageEyePreview.setImageURI(uri)
                binding.imageEyePreview.visibility = View.VISIBLE
            }
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "需要相机权限才能拍照", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPigeonEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGenderSelection()
        setupColorGrid()
        setupDatePicker()
        setupStatusSelection()
        setupPhotoButtons()
        setupFamilyRelation()
        setupStepNavigation()
        setupObservers()
        setupListeners()

        args.pigeonId?.let { id ->
            viewModel.loadPigeon(id)
        }
    }

    private fun setupGenderSelection() {
        binding.radioMale.setOnClickListener { viewModel.setGender(Gender.MALE) }
        binding.radioFemale.setOnClickListener { viewModel.setGender(Gender.FEMALE) }
        binding.radioUnknown.setOnClickListener { viewModel.setGender(Gender.UNKNOWN) }
    }

    private fun setupColorGrid() {
        val colorMap = mapOf(
            binding.buttonColorRain to "雨点",
            binding.buttonColorGray to "灰",
            binding.buttonColorRed to "红",
            binding.buttonColorWhite to "白",
            binding.buttonColorBlack to "黑",
            binding.buttonColorFlower to "花"
        )

        colorMap.forEach { (button, color) ->
            button.setOnClickListener {
                viewModel.setColor(color)
                updateColorSelection(button)
                binding.textCustomColor.visibility = View.GONE
            }
        }

        binding.buttonColorCustom.setOnClickListener {
            showCustomColorDialog()
        }
    }

    private fun showCustomColorDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "请输入羽色名称"
            setText(viewModel.pigeon.value?.color ?: "")
        }
        AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
            .setTitle("自定义羽色")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                val color = editText.text.toString().trim()
                if (color.isNotBlank()) {
                    viewModel.setColor(color)
                    clearColorButtonSelection()
                    binding.textCustomColor.text = "已选: $color"
                    binding.textCustomColor.visibility = View.VISIBLE
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun clearColorButtonSelection() {
        val buttons = listOf(
            binding.buttonColorRain,
            binding.buttonColorGray,
            binding.buttonColorRed,
            binding.buttonColorWhite,
            binding.buttonColorBlack,
            binding.buttonColorFlower
        )
        buttons.forEach { btn ->
            btn.setBackgroundColor(resources.getColor(R.color.white, null))
            btn.setTextColor(resources.getColor(R.color.text_primary, null))
        }
    }

    private fun updateColorSelection(selectedButton: MaterialButton) {
        val buttons = listOf(
            binding.buttonColorRain,
            binding.buttonColorGray,
            binding.buttonColorRed,
            binding.buttonColorWhite,
            binding.buttonColorBlack,
            binding.buttonColorFlower
        )
        buttons.forEach { btn ->
            if (btn == selectedButton) {
                btn.setBackgroundColor(resources.getColor(R.color.primary, null))
                btn.setTextColor(resources.getColor(R.color.white, null))
            } else {
                btn.setBackgroundColor(resources.getColor(R.color.white, null))
                btn.setTextColor(resources.getColor(R.color.text_primary, null))
            }
        }
    }

    private fun setupDatePicker() {
        binding.buttonBirthDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("选择出生日期")
                .setSelection(viewModel.pigeon.value?.birthDate ?: MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            picker.addOnPositiveButtonClickListener { selection ->
                viewModel.setBirthDate(selection)
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(selection))
                binding.buttonBirthDate.text = dateStr
            }
            picker.show(parentFragmentManager, "birth_date_picker")
        }
    }

    private fun setupStatusSelection() {
        val statusMap = mapOf(
            binding.buttonStatusActive to PigeonStatus.ACTIVE,
            binding.buttonStatusSold to PigeonStatus.SOLD,
            binding.buttonStatusDeceased to PigeonStatus.DECEASED,
            binding.buttonStatusGifted to PigeonStatus.GIFTED
        )

        statusMap.forEach { (button, status) ->
            button.setOnClickListener {
                viewModel.setStatus(status)
                updateStatusSelection(button)
            }
        }

        // 默认选中"在养"
        updateStatusSelection(binding.buttonStatusActive)
    }

    private fun updateStatusSelection(selectedButton: MaterialButton) {
        val buttons = listOf(
            binding.buttonStatusActive,
            binding.buttonStatusSold,
            binding.buttonStatusDeceased,
            binding.buttonStatusGifted
        )
        buttons.forEach { btn ->
            if (btn == selectedButton) {
                btn.setBackgroundColor(resources.getColor(R.color.primary, null))
                btn.setTextColor(resources.getColor(R.color.white, null))
            } else {
                btn.setBackgroundColor(resources.getColor(R.color.white, null))
                btn.setTextColor(resources.getColor(R.color.text_primary, null))
            }
        }
    }

    private fun setupPhotoButtons() {
        binding.buttonTakePhoto.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    launchCamera()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
                        .setTitle("需要相机权限")
                        .setMessage("拍照功能需要访问相机权限，请在设置中开启。")
                        .setPositiveButton("去设置") { _, _ ->
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                        .setNegativeButton("取消", null)
                        .show()
                }
                else -> {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

        binding.buttonPickPhoto.setOnClickListener {
            pickPhotoLauncher.launch("image/*")
        }

        binding.buttonTakeEyePhoto.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    launchEyeCamera()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
                        .setTitle("需要相机权限")
                        .setMessage("拍照功能需要访问相机权限，请在设置中开启。")
                        .setPositiveButton("去设置") { _, _ ->
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                        .setNegativeButton("取消", null)
                        .show()
                }
                else -> {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

        binding.buttonPickEyePhoto.setOnClickListener {
            pickEyePhotoLauncher.launch("image/*")
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = File.createTempFile(
                "pigeon_${System.currentTimeMillis()}",
                ".jpg",
                requireContext().cacheDir
            )
            currentPhotoFile = photoFile
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            takePhotoLauncher.launch(uri)
        } catch (e: SecurityException) {
            Toast.makeText(
                requireContext(),
                "相机权限被拒绝，请在系统设置中开启相机权限",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "启动相机失败: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun launchEyeCamera() {
        try {
            val photoFile = File.createTempFile(
                "pigeon_eye_${System.currentTimeMillis()}",
                ".jpg",
                requireContext().cacheDir
            )
            currentEyePhotoFile = photoFile
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            takeEyePhotoLauncher.launch(uri)
        } catch (e: SecurityException) {
            Toast.makeText(
                requireContext(),
                "相机权限被拒绝，请在系统设置中开启相机权限",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "启动相机失败: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupStepNavigation() {
        binding.buttonNext.setOnClickListener {
            if (viewModel.currentStep.value < 3) {
                viewModel.nextStep()
            } else {
                savePigeon()
            }
        }

        binding.buttonPrev.setOnClickListener {
            viewModel.prevStep()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pigeon.collect { pigeon ->
                    pigeon?.let {
                        binding.editName.setText(it.name)
                        binding.editRingNumber.setText(it.ringNumber)
                        binding.editNotes.setText(it.notes ?: "")

                        when (it.gender) {
                            Gender.MALE -> binding.radioMale.isChecked = true
                            Gender.FEMALE -> binding.radioFemale.isChecked = true
                            else -> binding.radioUnknown.isChecked = true
                        }
                        viewModel.setGender(it.gender)

                        it.birthDate?.let { date ->
                            viewModel.setBirthDate(date)
                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(date))
                            binding.buttonBirthDate.text = dateStr
                        }

                        it.color?.let { color ->
                            viewModel.setColor(color)
                            val colorButtons = mapOf(
                                "雨点" to binding.buttonColorRain,
                                "灰" to binding.buttonColorGray,
                                "红" to binding.buttonColorRed,
                                "白" to binding.buttonColorWhite,
                                "黑" to binding.buttonColorBlack,
                                "花" to binding.buttonColorFlower
                            )
                            colorButtons[color]?.let { btn ->
                                updateColorSelection(btn)
                                binding.textCustomColor.visibility = View.GONE
                            } ?: run {
                                clearColorButtonSelection()
                                binding.textCustomColor.text = "已选: $color"
                                binding.textCustomColor.visibility = View.VISIBLE
                            }
                        }

                        it.cageNumber?.let { cage ->
                            viewModel.setCageNumber(cage)
                            binding.editCageNumber.setText(cage)
                        }

                        it.loft?.let { loft ->
                            viewModel.setLoftId(loft.id)
                            binding.buttonSelectLoft.text = loft.name
                        }

                        // 加载已有状态
                        viewModel.setStatus(it.status)
                        val statusButtons = mapOf(
                            PigeonStatus.ACTIVE to binding.buttonStatusActive,
                            PigeonStatus.SOLD to binding.buttonStatusSold,
                            PigeonStatus.DECEASED to binding.buttonStatusDeceased,
                            PigeonStatus.GIFTED to binding.buttonStatusGifted
                        )
                        statusButtons[it.status]?.let { btn ->
                            updateStatusSelection(btn)
                        }

                        // 加载原照片预览
                        it.photoPath?.let { path ->
                            val file = java.io.File(path)
                            if (file.exists()) {
                                binding.imagePreview.setImageURI(android.net.Uri.fromFile(file))
                                binding.imagePreview.visibility = View.VISIBLE
                            }
                        }

                        // 加载原眼睛照片预览
                        it.eyePhotoPath?.let { path ->
                            val file = java.io.File(path)
                            if (file.exists()) {
                                binding.imageEyePreview.setImageURI(android.net.Uri.fromFile(file))
                                binding.imageEyePreview.visibility = View.VISIBLE
                            }
                        }

                        viewModel.onPigeonLoaded(it)
                        updateFamilyRelationButtons()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentStep.collect { step ->
                    updateStepUI(step)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveResult.collect { result ->
                    result?.let {
                        if (it.isSuccess) {
                            Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                it.exceptionOrNull()?.message ?: "保存失败",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        viewModel.clearResult()
                    }
                }
            }
        }
    }

    private fun updateStepUI(step: Int) {
        binding.containerStep1.visibility = if (step == 1) View.VISIBLE else View.GONE
        binding.containerStep2.visibility = if (step == 2) View.VISIBLE else View.GONE
        binding.containerStep3.visibility = if (step == 3) View.VISIBLE else View.GONE

        binding.textStepTitle.text = "第 $step 步，共 3 步"
        binding.textStepSubtitle.text = when (step) {
            1 -> "基本信息"
            2 -> "位置信息"
            else -> "家族关系"
        }

        binding.buttonPrev.visibility = if (step > 1) View.VISIBLE else View.GONE
        binding.buttonNext.text = if (step < 3) "下一步" else "保存"

        binding.dotStep1.setBackgroundResource(
            if (step >= 1) R.drawable.bg_step_active else R.drawable.bg_step_inactive
        )
        binding.lineStep1.setBackgroundColor(
            resources.getColor(if (step >= 2) R.color.primary else R.color.divider, null)
        )
        binding.dotStep2.setBackgroundResource(
            if (step >= 2) R.drawable.bg_step_active else R.drawable.bg_step_inactive
        )
        binding.lineStep2.setBackgroundColor(
            resources.getColor(if (step >= 3) R.color.primary else R.color.divider, null)
        )
        binding.dotStep3.setBackgroundResource(
            if (step >= 3) R.drawable.bg_step_active else R.drawable.bg_step_inactive
        )
    }

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonSelectLoft.setOnClickListener {
            showLoftSelectionDialog()
        }

        binding.buttonSelectFather.setOnClickListener {
            showFamilySelectionDialog("father")
        }

        binding.buttonSelectMother.setOnClickListener {
            showFamilySelectionDialog("mother")
        }

        binding.buttonSelectMate.setOnClickListener {
            showFamilySelectionDialog("mate")
        }
    }

    private fun updateFamilyRelationButtons() {
        val allPigeons = viewModel.allPigeons.value
        val currentPigeonId = args.pigeonId

        val father = allPigeons.find { it.id == viewModel.fatherId.value }
        binding.buttonSelectFather.text = father?.let { "${it.name} (${it.ringNumber})" } ?: "不设置"

        val mother = allPigeons.find { it.id == viewModel.motherId.value }
        binding.buttonSelectMother.text = mother?.let { "${it.name} (${it.ringNumber})" } ?: "不设置"

        val mate = allPigeons.find { it.id == viewModel.mateId.value }
        binding.buttonSelectMate.text = mate?.let { "${it.name} (${it.ringNumber})" } ?: "不设置"
    }

    private fun showFamilySelectionDialog(relationType: String) {
        val allPigeons = viewModel.allPigeons.value
        val currentPigeonId = args.pigeonId

        // 排除当前鸽子
        val otherPigeons = allPigeons.filter { it.id != currentPigeonId }

        val candidates = when (relationType) {
            "father" -> otherPigeons.filter { it.gender == Gender.MALE }
            "mother" -> otherPigeons.filter { it.gender == Gender.FEMALE }
            else -> {
                // 配偶：排除同性（如果当前鸽子性别已知）
                val currentGender = viewModel.pigeon.value?.gender
                if (currentGender != null && currentGender != Gender.UNKNOWN) {
                    otherPigeons.filter { it.gender != currentGender || it.gender == Gender.UNKNOWN }
                } else {
                    otherPigeons
                }
            }
        }

        val items = arrayOf("不设置") + candidates.map { "${it.name} (${it.ringNumber})" }.toTypedArray()
        val ids = arrayOf<String?>(null) + candidates.map { it.id }.toTypedArray()

        val currentIndex = when (relationType) {
            "father" -> ids.indexOfFirst { it == viewModel.fatherId.value }.coerceAtLeast(0)
            "mother" -> ids.indexOfFirst { it == viewModel.motherId.value }.coerceAtLeast(0)
            else -> ids.indexOfFirst { it == viewModel.mateId.value }.coerceAtLeast(0)
        }

        val title = when (relationType) {
            "father" -> "选择父亲"
            "mother" -> "选择母亲"
            else -> "选择配偶"
        }

        AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
            .setTitle(title)
            .setSingleChoiceItems(items, currentIndex) { _, _ -> }
            .setPositiveButton("确定") { dialog, _ ->
                val listView = (dialog as AlertDialog).listView
                val selectedPosition = listView.checkedItemPosition
                val selectedId = ids[selectedPosition]
                when (relationType) {
                    "father" -> viewModel.setFatherId(selectedId)
                    "mother" -> viewModel.setMotherId(selectedId)
                    else -> viewModel.setMateId(selectedId)
                }
                updateFamilyRelationButtons()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun setupFamilyRelation() {
        // 初始化家族关系按钮状态
        updateFamilyRelationButtons()
    }

    private fun showLoftSelectionDialog() {
        val lofts = viewModel.lofts.value
        if (lofts.isEmpty()) {
            AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
                .setTitle("选择鸽棚")
                .setMessage("暂无鸽棚，请先前往「鸽舍管理」添加鸽棚。")
                .setPositiveButton("确定", null)
                .show()
            return
        }

        val items = (lofts.map { it.name } + "暂不选择").toTypedArray()
        val loftIds = (lofts.map { it.id } + null).toTypedArray()

        AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
            .setTitle("选择鸽棚")
            .setItems(items) { _, which ->
                val selectedId = loftIds[which]
                viewModel.setLoftId(selectedId)
                binding.buttonSelectLoft.text = if (selectedId != null) items[which] else "选择鸽棚"
            }
            .show()
    }

    private fun savePigeon() {
        val name = binding.editName.text.toString().trim()
        val ringNumber = binding.editRingNumber.text.toString().trim()
        val notes = binding.editNotes.text.toString().trim().ifEmpty { null }
        val cageNumber = binding.editCageNumber.text.toString().trim().ifEmpty { null }
        viewModel.setCageNumber(cageNumber)

        viewModel.save(
            id = args.pigeonId,
            name = name,
            ringNumber = ringNumber,
            notes = notes
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
