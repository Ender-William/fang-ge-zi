package com.pigeonnest.data.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.pigeonnest.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val TAG = "PhotoStorage"
    }

    private val photosDir: File by lazy {
        File(context.filesDir, "photos").apply { mkdirs() }
    }

    suspend fun savePigeonPhoto(
        pigeonId: String,
        sourceUri: Uri,
        maxSizeKB: Int = 500
    ): String = withContext(Dispatchers.IO) {
        val pigeonPhotoDir = File(photosDir, pigeonId).apply { mkdirs() }
        val fileName = "${System.currentTimeMillis()}.jpg"
        val destFile = File(pigeonPhotoDir, fileName)

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            val bitmap = BitmapFactory.decodeStream(input)
                ?: throw IllegalArgumentException("无法解码图片，格式可能不受支持")
            val compressed = compressBitmap(bitmap, maxSizeKB)
            destFile.outputStream().use {
                compressed.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }
            if (bitmap !== compressed) {
                bitmap.recycle()
                compressed.recycle()
            }
            Log.d(TAG, "照片已保存: ${destFile.absolutePath} (${destFile.length()} bytes)")
        } ?: throw IllegalArgumentException("无法读取照片文件")

        destFile.absolutePath
    }

    private fun compressBitmap(original: Bitmap, maxSizeKB: Int): Bitmap {
        val maxDimension = 1200
        val scale = minOf(
            maxDimension.toFloat() / original.width,
            maxDimension.toFloat() / original.height,
            1f
        )

        return if (scale < 1f) {
            val newWidth = (original.width * scale).toInt()
            val newHeight = (original.height * scale).toInt()
            Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
        } else {
            original
        }
    }

    fun loadPhoto(imageView: ImageView, photoPath: String?, placeholderRes: Int = R.drawable.ic_pigeon_placeholder) {
        Glide.with(imageView.context)
            .load(photoPath)
            .placeholder(placeholderRes)
            .error(placeholderRes)
            .centerCrop()
            .into(imageView)
    }

    fun getAllPhotoFiles(): List<File> {
        val files = photosDir.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() in listOf("jpg", "jpeg", "png") }
            .toList()
        Log.d(TAG, "扫描到 ${files.size} 个照片文件")
        return files
    }

    suspend fun deletePigeonPhotos(pigeonId: String) = withContext(Dispatchers.IO) {
        File(photosDir, pigeonId).deleteRecursively()
    }

    suspend fun importPhotosFromDirectory(sourceDir: File): Int = withContext(Dispatchers.IO) {
        var count = 0
        sourceDir.walkTopDown()
            .filter { it.isFile }
            .forEach { sourceFile ->
                val relativePath = sourceFile.relativeTo(sourceDir).path
                val destFile = File(photosDir, relativePath)
                destFile.parentFile?.mkdirs()
                sourceFile.copyTo(destFile, overwrite = true)
                count++
                Log.d(TAG, "导入照片: $relativePath -> ${destFile.absolutePath}")
            }
        Log.d(TAG, "共导入 $count 个照片文件")
        count
    }
}
