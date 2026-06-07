package com.pigeonnest.data.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.model.PigeonBrief
import com.pigeonnest.domain.repository.LineageResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PigeonPdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val PAGE_WIDTH = 842   // A4 Landscape @ 72dpi
        const val PAGE_HEIGHT = 595
        const val MARGIN = 30f

        const val PHOTO_SIZE_MAIN = 85f
        const val PHOTO_SIZE_SMALL = 36f
    }

    suspend fun generate(
        pigeon: Pigeon,
        lineage: LineageResult,
        loft: Loft?
    ): File = withContext(Dispatchers.IO) {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawPage(canvas, pigeon, lineage, loft)

        pdfDocument.finishPage(page)

        val exportDir = File(context.cacheDir, "export_preview").apply { mkdirs() }
        exportDir.listFiles()?.forEach { it.delete() }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
        val file = File(exportDir, "pigeon_${pigeon.ringNumber}_${timestamp}.pdf")
        FileOutputStream(file).use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        file
    }

    private fun drawPage(
        canvas: Canvas,
        pigeon: Pigeon,
        lineage: LineageResult,
        loft: Loft?
    ) {
        // 填充白色背景，避免某些渲染器识别为空白页
        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E2E28")
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5E5E54")
            textSize = 11f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E2E28")
            textSize = 13f
        }
        val smallValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E2E28")
            textSize = 10f
        }
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E8E8E0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val boxBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFFDF9")
            style = Paint.Style.FILL
        }
        val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFF5EB")
            style = Paint.Style.FILL
        }
        val altRowBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FAF7F2")
            style = Paint.Style.FILL
        }

        // 1. 标题
        val title = "鸽巢管家 · 鸽子血统档案"
        val titleWidth = titlePaint.measureText(title)
        canvas.drawText(title, (PAGE_WIDTH - titleWidth) / 2f, 22f, titlePaint)

        val dateStr = "生成日期: ${SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date())}"
        canvas.drawText(dateStr, PAGE_WIDTH - MARGIN - labelPaint.measureText(dateStr), 22f, labelPaint)

        // 2. 当前鸽子信息区（三列布局）
        val infoTop = 38f

        // 左列：照片
        val photoX = MARGIN + 10f
        val photoY = infoTop + 5f
        drawPhotoOrPlaceholder(canvas, pigeon.photoPath, photoX, photoY, PHOTO_SIZE_MAIN, boxPaint)
        canvas.drawText("全身照", photoX + 18f, photoY + PHOTO_SIZE_MAIN + 14f, labelPaint)

        val eyePhotoX = photoX + PHOTO_SIZE_MAIN + 12f
        drawPhotoOrPlaceholder(canvas, pigeon.eyePhotoPath, eyePhotoX, photoY, PHOTO_SIZE_MAIN, boxPaint)
        canvas.drawText("眼睛照", eyePhotoX + 18f, photoY + PHOTO_SIZE_MAIN + 14f, labelPaint)

        // 中列：基本信息
        val infoX = eyePhotoX + PHOTO_SIZE_MAIN + 22f
        val infoY = photoY + 8f
        val lineHeight = 19f

        val infoPairs = listOf(
            "名字" to pigeon.name,
            "脚环号" to pigeon.ringNumber,
            "性别" to pigeon.gender.displayName,
            "羽色" to (pigeon.color ?: "未记录"),
            "出生日期" to formatDate(pigeon.birthDate),
            "当前状态" to pigeon.status.displayName,
            "笼位号" to (pigeon.cageNumber ?: "未记录"),
            "备注" to (pigeon.notes ?: "无")
        )
        infoPairs.forEachIndexed { index, (label, value) ->
            val y = infoY + index * lineHeight
            val displayValue = if (value.length > 14) value.take(14) + "…" else value
            canvas.drawText("$label: $displayValue", infoX, y, valuePaint)
        }

        // 右列：鸽舍卡片
        val loftCardWidth = 260f
        val loftCardHeight = if (loft != null) 68f else 42f
        val loftCardLeft = PAGE_WIDTH - MARGIN - loftCardWidth
        val loftCardRect = RectF(loftCardLeft, infoTop, PAGE_WIDTH - MARGIN, infoTop + loftCardHeight)
        canvas.drawRoundRect(loftCardRect, 6f, 6f, boxBgPaint)
        canvas.drawRoundRect(loftCardRect, 6f, 6f, boxPaint)

        val loftTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A64B0F")
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("鸽舍信息", loftCardLeft + 10f, infoTop + 18f, loftTitlePaint)
        val loftTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E2E28")
            textSize = 11f
        }
        if (loft != null) {
            canvas.drawText(loft.name, loftCardLeft + 10f, infoTop + 36f, loftTextPaint)
            canvas.drawText(loft.location ?: "位置未填写", loftCardLeft + 10f, infoTop + 52f, loftTextPaint)
        } else {
            canvas.drawText("未分配鸽舍", loftCardLeft + 10f, infoTop + 36f, loftTextPaint)
        }

        var currentY = photoY + PHOTO_SIZE_MAIN + 28f

        // 3. 血统区
        canvas.drawText("上三代血统", MARGIN, currentY, titlePaint)
        currentY += 14f

        val labelColWidth = 55f
        val tableLeft = MARGIN
        val tableRight = PAGE_WIDTH - MARGIN
        val contentWidth = tableRight - tableLeft - labelColWidth

        // 第1代：父母（2只）
        drawGenerationRow(
            canvas, "父母", extractGeneration(lineage, 1), generation = 1,
            tableLeft, currentY, labelColWidth, contentWidth, rowHeight = 72f,
            valuePaint, labelPaint, smallValuePaint, boxPaint, headerBgPaint, null
        )
        currentY += 72f

        // 第2代：祖父母（4只）
        drawGenerationRow(
            canvas, "祖父母", extractGeneration(lineage, 2), generation = 2,
            tableLeft, currentY, labelColWidth, contentWidth, rowHeight = 72f,
            valuePaint, labelPaint, smallValuePaint, boxPaint, headerBgPaint, altRowBgPaint
        )
        currentY += 72f

        // 第3代：曾祖父母（8只）
        drawGenerationRow(
            canvas, "曾祖父母", extractGeneration(lineage, 3), generation = 3,
            tableLeft, currentY, labelColWidth, contentWidth, rowHeight = 55f,
            valuePaint, labelPaint, smallValuePaint, boxPaint, headerBgPaint, null
        )
        currentY += 55f

        // 页脚
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#7A7A6E")
            textSize = 9f
        }
        canvas.drawText("本档案由《鸽巢管家》APP 自动生成", MARGIN, PAGE_HEIGHT - 12f, footerPaint)
    }

    /** 按 BFS 提取指定代的全部鸽子（ generation=1 为父母，2 为祖父母，3 为曾祖父母） */
    private fun extractGeneration(lineage: LineageResult, generation: Int): List<PigeonBrief?> {
        var currentNodes: List<LineageResult?> = listOf(lineage)
        repeat(generation) {
            val nextNodes = mutableListOf<LineageResult?>()
            for (node in currentNodes) {
                nextNodes.add(node?.father)
                nextNodes.add(node?.mother)
            }
            currentNodes = nextNodes
        }
        return currentNodes.map { it?.pigeon }
    }

    private fun drawGenerationRow(
        canvas: Canvas,
        label: String,
        pigeons: List<PigeonBrief?>,
        generation: Int,
        tableLeft: Float,
        rowTop: Float,
        labelColWidth: Float,
        contentWidth: Float,
        rowHeight: Float,
        valuePaint: Paint,
        labelPaint: Paint,
        smallValuePaint: Paint,
        boxPaint: Paint,
        bgPaint: Paint,
        altBgPaint: Paint?
    ) {
        val tableRight = tableLeft + labelColWidth + contentWidth

        // 表头背景（整行顶部一小条）
        canvas.drawRect(tableLeft, rowTop, tableRight, rowTop + 18f, bgPaint)
        canvas.drawRect(tableLeft, rowTop, tableRight, rowTop + rowHeight, boxPaint)

        // 标签列文字
        val labelCenterY = rowTop + 12f
        canvas.drawText(label, tableLeft + 6f, labelCenterY, valuePaint)

        // 标签与内容分隔线
        canvas.drawLine(tableLeft + labelColWidth, rowTop, tableLeft + labelColWidth, rowTop + rowHeight, boxPaint)

        // 交替行背景（数据区）
        altBgPaint?.let {
            canvas.drawRect(tableLeft + labelColWidth, rowTop + 18f, tableRight, rowTop + rowHeight, it)
        }

        val cellWidth = contentWidth / pigeons.size.coerceAtLeast(1)
        val smallBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D1D1C5")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        pigeons.forEachIndexed { index, brief ->
            val cellLeft = tableLeft + labelColWidth + index * cellWidth
            if (index > 0) {
                canvas.drawLine(cellLeft, rowTop, cellLeft, rowTop + rowHeight, boxPaint)
            }

            if (brief == null) {
                val centerX = cellLeft + cellWidth / 2f
                val centerY = rowTop + 18f + (rowHeight - 18f) / 2f + 4f
                labelPaint.textAlign = Paint.Align.CENTER
                canvas.drawText("未记录", centerX, centerY, labelPaint)
                labelPaint.textAlign = Paint.Align.LEFT
                return@forEachIndexed
            }

            if (generation <= 2) {
                // 第1、2代有照片+文字
                val padding = 6f
                val photoSize = PHOTO_SIZE_SMALL
                val photoX = cellLeft + padding
                val photoY = rowTop + 18f + (rowHeight - 18f - photoSize) / 2f
                drawPhotoOrPlaceholder(canvas, brief.photoPath, photoX, photoY, photoSize, smallBoxPaint)

                val textX = photoX + photoSize + 6f
                val textYBase = photoY + photoSize / 2f
                val displayName = if (brief.name.length > 8) brief.name.take(8) + "…" else brief.name
                val displayRing = if (brief.ringNumber.length > 10) brief.ringNumber.take(10) + "…" else brief.ringNumber
                canvas.drawText(displayName, textX, textYBase - 2f, valuePaint)
                canvas.drawText(displayRing, textX, textYBase + 11f, labelPaint)
            } else {
                // 第3代太挤，纯文字居中
                val centerX = cellLeft + cellWidth / 2f
                val centerY = rowTop + 18f + (rowHeight - 18f) / 2f
                val displayName = if (brief.name.length > 5) brief.name.take(5) + "…" else brief.name
                val displayRing = if (brief.ringNumber.length > 8) brief.ringNumber.take(8) + "…" else brief.ringNumber
                smallValuePaint.textAlign = Paint.Align.CENTER
                labelPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(displayName, centerX, centerY - 2f, smallValuePaint)
                canvas.drawText(displayRing, centerX, centerY + 11f, labelPaint)
                smallValuePaint.textAlign = Paint.Align.LEFT
                labelPaint.textAlign = Paint.Align.LEFT
            }
        }
    }

    private fun drawPhotoOrPlaceholder(
        canvas: Canvas,
        photoPath: String?,
        x: Float,
        y: Float,
        size: Float,
        borderPaint: Paint
    ) {
        val rect = RectF(x, y, x + size, y + size)
        canvas.drawRect(rect, Paint().apply {
            color = Color.parseColor("#F5F5F0")
            style = Paint.Style.FILL
        })

        photoPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                try {
                    val bitmap = BitmapFactory.decodeFile(path)
                    bitmap?.let {
                        val scaled = Bitmap.createScaledBitmap(it, size.toInt(), size.toInt(), true)
                        canvas.drawBitmap(scaled, x, y, null)
                        if (scaled != it) scaled.recycle()
                        return
                    }
                } catch (_: Exception) {
                    // 解码失败则回退到占位图
                }
            }
        }

        canvas.drawRect(rect, borderPaint)
        val placeholderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B5B5A8")
            textSize = if (size > 60f) 14f else 10f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("无照片", x + size / 2f, y + size / 2f + 5f, placeholderPaint)
    }

    private fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return "未记录"
        return SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(timestamp))
    }

    fun getFileUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
