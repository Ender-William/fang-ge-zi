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

        const val PHOTO_SIZE_MAIN = 60f
        const val PHOTO_SIZE_MEDIUM = 50f
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
        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E2E28")
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5E5E54")
            textSize = 10f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E2E28")
            textSize = 12f
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

        // 1. 标题
        val title = "鸽巢管家 · 鸽子血统档案"
        val titleWidth = titlePaint.measureText(title)
        canvas.drawText(title, (PAGE_WIDTH - titleWidth) / 2f, 22f, titlePaint)

        val dateStr = "生成日期: ${SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date())}"
        canvas.drawText(dateStr, PAGE_WIDTH - MARGIN - labelPaint.measureText(dateStr), 22f, labelPaint)

        // 2. 当前鸽子精简信息区
        val infoTop = 35f

        // 左：照片
        val photoX = MARGIN + 5f
        val photoY = infoTop + 3f
        drawPhotoOrPlaceholder(canvas, pigeon.photoPath, photoX, photoY, PHOTO_SIZE_MAIN, boxPaint)
        val eyeX = photoX + PHOTO_SIZE_MAIN + 8f
        drawPhotoOrPlaceholder(canvas, pigeon.eyePhotoPath, eyeX, photoY, PHOTO_SIZE_MAIN, boxPaint)

        // 中：基本信息（紧凑两列）
        val infoX = eyeX + PHOTO_SIZE_MAIN + 15f
        val infoY = photoY + 5f
        val lineH = 16f

        val infoCol1X = infoX
        val infoCol2X = infoX + 140f
        val rows = listOf(
            infoCol1X to "名字: ${pigeon.name}",
            infoCol2X to "脚环号: ${pigeon.ringNumber}",
            infoCol1X to "性别: ${pigeon.gender.displayName}",
            infoCol2X to "羽色: ${pigeon.color ?: "未记录"}",
            infoCol1X to "出生: ${formatDate(pigeon.birthDate)}",
            infoCol2X to "状态: ${pigeon.status.displayName}",
            infoCol1X to "笼位: ${pigeon.cageNumber ?: "未记录"}",
            infoCol2X to "鸽舍: ${loft?.name ?: "未分配"}"
        )
        rows.forEachIndexed { idx, (x, text) ->
            val y = infoY + (idx / 2) * lineH
            val display = if (text.length > 16) text.take(16) + "…" else text
            canvas.drawText(display, x, y, valuePaint)
        }

        // 右：鸽舍卡片
        val loftCardW = 230f
        val loftCardH = if (loft != null) 58f else 36f
        val loftCardL = PAGE_WIDTH - MARGIN - loftCardW
        val loftRect = RectF(loftCardL, infoTop, PAGE_WIDTH - MARGIN, infoTop + loftCardH)
        canvas.drawRoundRect(loftRect, 6f, 6f, boxBgPaint)
        canvas.drawRoundRect(loftRect, 6f, 6f, boxPaint)

        val loftTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A64B0F")
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("鸽舍信息", loftCardL + 8f, infoTop + 16f, loftTitlePaint)
        val loftTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E2E28")
            textSize = 10f
        }
        if (loft != null) {
            canvas.drawText(loft.name, loftCardL + 8f, infoTop + 32f, loftTextPaint)
            canvas.drawText(loft.location ?: "位置未填写", loftCardL + 8f, infoTop + 46f, loftTextPaint)
        } else {
            canvas.drawText("未分配鸽舍", loftCardL + 8f, infoTop + 32f, loftTextPaint)
        }

        // 3. 血统区 — 横向三列展开
        val pedigreeTop = 120f
        val pedigreeBottom = PAGE_HEIGHT - 25f
        val pedigreeHeight = pedigreeBottom - pedigreeTop

        val col1X = MARGIN
        val col2X = MARGIN + 250f
        val col3X = MARGIN + 500f
        val colWidth = 240f

        // 第1代：父母
        val gen1 = extractGeneration(lineage, 1)
        drawPedigreeColumn(
            canvas, "第一代 父母", gen1,
            col1X, pedigreeTop, colWidth, pedigreeHeight,
            valuePaint, labelPaint, smallValuePaint, boxPaint, headerBgPaint
        )

        // 第2代：祖父母
        val gen2 = extractGeneration(lineage, 2)
        drawPedigreeColumn(
            canvas, "第二代 祖父母", gen2,
            col2X, pedigreeTop, colWidth, pedigreeHeight,
            valuePaint, labelPaint, smallValuePaint, boxPaint, headerBgPaint
        )

        // 第3代：曾祖父母
        val gen3 = extractGeneration(lineage, 3)
        drawPedigreeColumn(
            canvas, "第三代 曾祖父母", gen3,
            col3X, pedigreeTop, colWidth, pedigreeHeight,
            valuePaint, labelPaint, smallValuePaint, boxPaint, headerBgPaint
        )

        // 页脚
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#7A7A6E")
            textSize = 9f
        }
        canvas.drawText("本档案由《鸽巢管家》APP 自动生成", MARGIN, PAGE_HEIGHT - 10f, footerPaint)
    }

    private fun drawPedigreeColumn(
        canvas: Canvas,
        header: String,
        pigeons: List<PigeonBrief?>,
        colX: Float,
        colTop: Float,
        colWidth: Float,
        colHeight: Float,
        valuePaint: Paint,
        labelPaint: Paint,
        smallValuePaint: Paint,
        boxPaint: Paint,
        headerBgPaint: Paint
    ) {
        val headerHeight = 20f
        val contentTop = colTop + headerHeight
        val contentHeight = colHeight - headerHeight

        // 表头背景
        canvas.drawRect(colX, colTop, colX + colWidth, contentTop, headerBgPaint)
        canvas.drawRect(colX, colTop, colX + colWidth, contentTop, boxPaint)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A64B0F")
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(header, colX + colWidth / 2f, colTop + 14f, headerPaint)
        headerPaint.textAlign = Paint.Align.LEFT

        // 列外框
        canvas.drawRect(colX, contentTop, colX + colWidth, colTop + colHeight, boxPaint)

        val cellCount = pigeons.size.coerceAtLeast(1)
        val cellHeight = contentHeight / cellCount

        // 根据格子高度动态选择照片尺寸
        val photoSize = when {
            cellHeight >= 100f -> 60f
            cellHeight >= 60f -> 42f
            cellHeight >= 45f -> 34f
            else -> 28f
        }

        val smallBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D1D1C5")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        pigeons.forEachIndexed { index, brief ->
            val cellTop = contentTop + index * cellHeight
            if (index > 0) {
                canvas.drawLine(colX, cellTop, colX + colWidth, cellTop, boxPaint)
            }

            if (brief == null) {
                val cx = colX + colWidth / 2f
                val cy = cellTop + cellHeight / 2f + 4f
                labelPaint.textAlign = Paint.Align.CENTER
                canvas.drawText("未记录", cx, cy, labelPaint)
                labelPaint.textAlign = Paint.Align.LEFT
                return@forEachIndexed
            }

            val pad = 6f
            val photoX = colX + pad
            val photoY = cellTop + (cellHeight - photoSize) / 2f
            drawPhotoOrPlaceholder(canvas, brief.photoPath, photoX, photoY, photoSize, smallBoxPaint)

            val textX = photoX + photoSize + 6f
            val textBase = photoY + photoSize / 2f

            // 根据可用宽度计算最大字符数（约每字符 11pt）
            val textAreaWidth = colWidth - photoSize - pad * 3
            val maxChars = (textAreaWidth / 11f).toInt()

            val nameText = if (brief.name.length > maxChars) brief.name.take(maxChars) + "…" else brief.name
            val ringText = if (brief.ringNumber.length > maxChars) brief.ringNumber.take(maxChars) + "…" else brief.ringNumber
            val detailText = "${brief.gender.displayName}  ${brief.color ?: "未记录"}"

            when {
                cellHeight >= 80f -> {
                    // 大格子：三行文字
                    canvas.drawText(nameText, textX, textBase - 6f, valuePaint)
                    canvas.drawText(ringText, textX, textBase + 8f, labelPaint)
                    canvas.drawText(detailText, textX, textBase + 22f, labelPaint)
                }
                cellHeight >= 55f -> {
                    // 中等格子：三行紧凑文字
                    val nameP = Paint(valuePaint).apply { textSize = 11f }
                    val detailP = Paint(labelPaint).apply { textSize = 9f }
                    canvas.drawText(nameText, textX, textBase - 4f, nameP)
                    canvas.drawText(ringText, textX, textBase + 8f, detailP)
                    canvas.drawText(detailText, textX, textBase + 19f, detailP)
                }
                else -> {
                    // 小格子：两行文字
                    val nameP = Paint(smallValuePaint).apply { textSize = 10f }
                    val detailP = Paint(labelPaint).apply { textSize = 9f }
                    canvas.drawText(nameText, textX, textBase - 2f, nameP)
                    canvas.drawText(ringText, textX, textBase + 10f, detailP)
                    // 极小格子放不下第三行，省略性别羽色
                    if (cellHeight >= 38f) {
                        canvas.drawText(detailText, textX, textBase + 21f, detailP)
                    }
                }
            }
        }
    }

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
            android.util.Log.d("PigeonPdfGenerator", "photoPath=$path exists=${file.exists()}")
            if (file.exists()) {
                try {
                    val bitmap = BitmapFactory.decodeFile(path)
                    android.util.Log.d("PigeonPdfGenerator", "decoded bitmap=${bitmap != null} size=${bitmap?.width}x${bitmap?.height}")
                    bitmap?.let {
                        val scaled = Bitmap.createScaledBitmap(it, size.toInt(), size.toInt(), true)
                        canvas.drawBitmap(scaled, x, y, null)
                        if (scaled != it) scaled.recycle()
                        return
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PigeonPdfGenerator", "decode failed: ${e.message}")
                }
            }
        }

        canvas.drawRect(rect, borderPaint)
        val placeholderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B5B5A8")
            textSize = if (size > 50f) 12f else 9f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("无照片", x + size / 2f, y + size / 2f + 4f, placeholderPaint)
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
