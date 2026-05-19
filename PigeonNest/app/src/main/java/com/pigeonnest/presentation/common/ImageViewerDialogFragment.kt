package com.pigeonnest.presentation.common

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.pigeonnest.R
import com.pigeonnest.databinding.DialogImageViewerBinding
import java.io.OutputStream

class ImageViewerDialogFragment : DialogFragment() {

    private var _binding: DialogImageViewerBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_IMAGE_PATH = "image_path"
        private const val ARG_PIGEON_NAME = "pigeon_name"

        fun newInstance(imagePath: String, pigeonName: String = "pigeon"): ImageViewerDialogFragment {
            return ImageViewerDialogFragment().apply {
                arguments = bundleOf(
                    ARG_IMAGE_PATH to imagePath,
                    ARG_PIGEON_NAME to pigeonName
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogImageViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imagePath = arguments?.getString(ARG_IMAGE_PATH)
        if (imagePath == null) {
            dismiss()
            return
        }

        // 加载图片到 PhotoView
        Glide.with(this)
            .load(imagePath)
            .into(binding.photoView)

        binding.buttonClose.setOnClickListener {
            dismiss()
        }

        binding.buttonSave.setOnClickListener {
            saveImageToGallery(imagePath)
        }
    }

    private fun saveImageToGallery(imagePath: String) {
        Glide.with(this)
            .asBitmap()
            .load(imagePath)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val fileName = "pigeon_${System.currentTimeMillis()}.jpg"
                    val pigeonName = arguments?.getString(ARG_PIGEON_NAME) ?: "pigeon"

                    val saved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        saveImageAndroidQ(resource, fileName, pigeonName)
                    } else {
                        saveImageLegacy(resource, fileName)
                    }

                    if (saved) {
                        Toast.makeText(requireContext(), "已保存到相册", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "保存失败", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun saveImageAndroidQ(bitmap: Bitmap, fileName: String, pigeonName: String): Boolean {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/放鸽子")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri: Uri? = requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let {
                requireContext().contentResolver.openOutputStream(it)?.use { outputStream: OutputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                requireContext().contentResolver.update(it, contentValues, null, null)
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun saveImageLegacy(bitmap: Bitmap, fileName: String): Boolean {
        return try {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = java.io.File(picturesDir, "放鸽子").apply { mkdirs() }
            val file = java.io.File(appDir, fileName)
            file.outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
            }

            // 通知相册扫描新文件
            val uri = Uri.fromFile(file)
            requireContext().sendBroadcast(
                android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
