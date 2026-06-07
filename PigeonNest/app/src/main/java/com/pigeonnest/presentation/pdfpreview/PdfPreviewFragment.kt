package com.pigeonnest.presentation.pdfpreview

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pigeonnest.R
import com.pigeonnest.databinding.FragmentPdfPreviewBinding
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfPreviewFragment : Fragment() {

    private var _binding: FragmentPdfPreviewBinding? = null
    private val binding get() = _binding!!
    private val args: PdfPreviewFragmentArgs by navArgs()

    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null

    private val savePdfLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { targetUri ->
                copyPdfToUri(targetUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        renderPdf(args.pdfFilePath)

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonConfirmExport.setOnClickListener {
            showExportOptionsDialog()
        }
    }

    private fun renderPdf(filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(requireContext(), "PDF 文件不存在", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
                return
            }

            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor!!)

            val page = pdfRenderer!!.openPage(0)
            val width = page.width
            val height = page.height

            // 使用页面原始尺寸渲染
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            binding.imagePdfPreview.setImageBitmap(bitmap)
            // PhotoView：让 PDF 宽度优先填满屏幕，同时支持双指缩放/拖动
            binding.imagePdfPreview.post {
                val viewW = binding.imagePdfPreview.width.toFloat()
                val viewH = binding.imagePdfPreview.height.toFloat()
                val bitmapW = bitmap.width.toFloat()
                val bitmapH = bitmap.height.toFloat()
                val scaleX = viewW / bitmapW
                val scaleY = viewH / bitmapH
                // 宽度优先填满，若高度撑不下则按高度适配
                val finalScale = if (bitmapH * scaleX <= viewH) scaleX else scaleY
                binding.imagePdfPreview.setScale(finalScale, false)
            }
            binding.progressLoading.visibility = View.GONE
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "预览 PDF 失败: ${e.message}", Toast.LENGTH_LONG).show()
            binding.progressLoading.visibility = View.GONE
        }
    }

    private fun showExportOptionsDialog() {
        val options = arrayOf("保存到手机", "分享给朋友（微信/QQ等）")
        AlertDialog.Builder(requireContext(), R.style.ElderlyDialogTheme)
            .setTitle("导出方式")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> savePdfToDevice()
                    1 -> sharePdf()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun savePdfToDevice() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "鸽子血统档案_${timestamp}.pdf")
        }
        savePdfLauncher.launch(intent)
    }

    private fun sharePdf() {
        try {
            val file = File(args.pdfFilePath)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_SUBJECT, "鸽子血统档案")
            }
            startActivity(Intent.createChooser(intent, "分享血统档案"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "分享失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun copyPdfToUri(targetUri: Uri) {
        try {
            requireContext().contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                FileInputStream(File(args.pdfFilePath)).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Toast.makeText(requireContext(), "已保存 PDF", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "保存失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            pdfRenderer?.close()
            fileDescriptor?.close()
        } catch (_: Exception) {
        }
        _binding = null
    }
}
