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

        const val PHOTO_SIZE_MAIN = 70f
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

        // 1. 标题（限制宽度，避免与日期重叠或被截断）
        val rawTitle = "${loft?.name ?: "鸽巢管家"} · 鸽子血统档案"
        val dateStr = "生成日期: ${SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date())}"
        val dateWidth = labelPaint.measureText(dateStr)
        val maxTitleWidth = PAGE_WIDTH - MARGIN * 2f - dateWidth - 20f
        val titleChars = titlePaint.breakText(rawTitle, true, maxTitleWidth, null)
        val title = if (titleChars < rawTitle.length) rawTitle.take(titleChars.coerceAtLeast(1)) + "…" else rawTitle
        val titleWidth = titlePaint.measureText(title)
        canvas.drawText(title, (PAGE_WIDTH - titleWidth) / 2f, 22f, titlePaint)
        canvas.drawText(dateStr, PAGE_WIDTH - MARGIN - dateWidth, 22f, labelPaint)

        // 2. 当前鸽子精简信息区
        val infoTop = 32f

        // 左：照片
        val photoX = MARGIN + 5f
        val photoY = infoTop + 3f
        drawPhotoOrPlaceholder(canvas, pigeon.photoPath, photoX, photoY, PHOTO_SIZE_MAIN, boxPaint)
        val eyeX = photoX + PHOTO_SIZE_MAIN + 10f
        drawPhotoOrPlaceholder(canvas, pigeon.eyePhotoPath, eyeX, photoY, PHOTO_SIZE_MAIN, boxPaint)

        // 中：基本信息（紧凑两列）
        val infoX = eyeX + PHOTO_SIZE_MAIN + 18f
        val infoY = photoY + 5f
        val lineH = 14f

        val infoCol1X = infoX
        val infoCol2X = infoX + 180f
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

        // 比赛成绩（跨行显示）
        val achievementY = infoY + 4 * lineH
        val achievementText = "成绩: ${pigeon.achievement ?: "未填写"}"
        val achievementDisplay = if (achievementText.length > 40) achievementText.take(40) + "…" else achievementText
        canvas.drawText(achievementDisplay, infoCol1X, achievementY, valuePaint)

        // 3. 血统区 — 横向三列展开
        val pedigreeTop = 112f
        val pedigreeBottom = PAGE_HEIGHT - 12f
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
        canvas.drawText("本档案由《鸽巢管家》APP 自动生成", MARGIN, PAGE_HEIGHT - 7f, footerPaint)
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
            val contentW = colWidth - pad * 2

            // 脚环号 paint：加粗，尺寸随格子递减
            val ringPaint = Paint(when {
                cellHeight >= 80f -> valuePaint
                cellHeight >= 55f -> Paint(valuePaint).apply { textSize = 11f }
                else -> Paint(smallValuePaint).apply { textSize = 10f }
            }).apply {
                typeface = Typeface.DEFAULT_BOLD
            }
            val ringLineHeight = ringPaint.textSize * 1.2f

            // 预计算脚环号行数
            val ringLines = run {
                var remaining = brief.ringNumber
                var lines = 0
                while (remaining.isNotEmpty()) {
                    val chars = ringPaint.breakText(remaining, true, contentW, null)
                    if (chars <= 0) break
                    remaining = remaining.drop(chars)
                    lines++
                }
                lines.coerceAtLeast(1)
            }
            val ringHeight = ringLines * ringLineHeight

            // 信息 paint（性别、羽色、成绩）
            val infoPaint = Paint(labelPaint).apply {
                textSize = when {
                    cellHeight >= 80f -> 10f
                    cellHeight >= 55f -> 9f
                    else -> 8f
                }
            }
            val infoLineHeight = infoPaint.textSize * 1.2f
            val infoMaxWidth = contentW - photoSize - 4f

            // 将信息拆为两段：【性别+羽色】和【成绩】，成绩强制单独一行
            val firstLine = buildString {
                append(brief.gender.displayName)
                brief.color?.let { append("  $it") }
            }
            val secondLine = brief.achievement?.takeIf { it.isNotBlank() }

            // 计算总行数（第一段 wrap + 1 空行 + 第二段 wrap）
            fun countWrappedLines(text: String): Int {
                var remaining = text
                var lines = 0
                while (remaining.isNotEmpty()) {
                    val chars = infoPaint.breakText(remaining, true, infoMaxWidth, null)
                    if (chars <= 0) break
                    remaining = remaining.drop(chars)
                    lines++
                }
                return lines.coerceAtLeast(1)
            }
            val firstLines = countWrappedLines(firstLine)
            val secondLines = secondLine?.let { countWrappedLines(it) } ?: 0
            val infoLines = firstLines + (if (secondLine != null) 1 else 0) + secondLines
            val infoHeight = infoLines * infoLineHeight

            // 整体垂直居中
            val gap = 4f
            val totalContentH = ringHeight + gap + maxOf(photoSize, infoHeight)
            val startY = cellTop + (cellHeight - totalContentH) / 2f

            // 脚环号绘制（上方，左对齐）
            val photoX = colX + pad
            val ringBaseline = startY + ringPaint.textSize * 0.85f
            var remaining = brief.ringNumber
            var currentY = ringBaseline
            while (remaining.isNotEmpty()) {
                val chars = ringPaint.breakText(remaining, true, contentW, null)
                if (chars <= 0) break
                canvas.drawText(remaining.take(chars), photoX, currentY, ringPaint)
                remaining = remaining.drop(chars)
                currentY += ringLineHeight
            }

            // 照片绘制（下方偏左）
            val photoY = startY + ringHeight + gap
            drawPhotoOrPlaceholder(canvas, brief.photoPath, photoX, photoY, photoSize, smallBoxPaint)

            // 信息绘制（照片右侧，顶部对齐）
            val infoX = photoX + photoSize + 4f
            var infoCurrentY = photoY + infoPaint.textSize * 0.85f

            // 第一段：性别+羽色
            var infoRemaining = firstLine
            while (infoRemaining.isNotEmpty()) {
                val chars = infoPaint.breakText(infoRemaining, true, infoMaxWidth, null)
                if (chars <= 0) break
                canvas.drawText(infoRemaining.take(chars), infoX, infoCurrentY, infoPaint)
                infoRemaining = infoRemaining.drop(chars)
                infoCurrentY += infoLineHeight
            }

            // 第二段：成绩（强制另起一行）
            secondLine?.let {
                infoCurrentY += infoLineHeight // 空一行
                var achRemaining = it
                while (achRemaining.isNotEmpty()) {
                    val chars = infoPaint.breakText(achRemaining, true, infoMaxWidth, null)
                    if (chars <= 0) break
                    canvas.drawText(achRemaining.take(chars), infoX, infoCurrentY, infoPaint)
                    achRemaining = achRemaining.drop(chars)
                    infoCurrentY += infoLineHeight
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
