package com.pigeonnest.presentation.familygraph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.content.ContextCompat
import com.pigeonnest.R
import com.pigeonnest.domain.model.*

class FamilyGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val nodeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.graph_node_border)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val nodeSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary)
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_primary)
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }
    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.graph_edge_normal)
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    private val mateEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.graph_edge_mate)
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val currentMatrix = Matrix()
    private val savedMatrix = Matrix()
    private val matrixValues = FloatArray(9)

    private val minScale = 0.3f
    private val maxScale = 3.0f

    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())

    private var layoutResult: LayoutResult? = null
    private var selectedNodeId: String? = null
    private var onNodeClickListener: ((String) -> Unit)? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val result = layoutResult ?: return

        canvas.save()
        canvas.concat(currentMatrix)

        result.edges.forEach { edge ->
            drawEdge(canvas, edge)
        }

        result.allNodes.forEach { node ->
            drawSingleNode(canvas, node)
        }

        canvas.restore()
    }

    private fun drawSingleNode(canvas: Canvas, node: GraphNode) {
        val rect = RectF(
            node.x,
            node.y,
            node.x + GraphNode.NODE_WIDTH,
            node.y + GraphNode.NODE_HEIGHT
        )

        val cornerRadius = 16f
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, nodePaint)

        val borderPaint = if (node.pigeonId == selectedNodeId) {
            nodeSelectedPaint
        } else {
            nodeBorderPaint
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        val displayText = if (node.pigeonBrief.name.length > 6) {
            node.pigeonBrief.name.substring(0, 6) + "..."
        } else {
            node.pigeonBrief.name
        }
        canvas.drawText(
            displayText,
            node.x + GraphNode.NODE_WIDTH / 2,
            node.y + GraphNode.NODE_HEIGHT / 2 + 12f,
            textPaint
        )

        val genderMarker = when (node.pigeonBrief.gender) {
            Gender.MALE -> "雄"
            Gender.FEMALE -> "雌"
            else -> "?"
        }
        val genderPaint = Paint(textPaint).apply { textSize = 24f }
        canvas.drawText(
            genderMarker,
            node.x + GraphNode.NODE_WIDTH - 30f,
            node.y + 30f,
            genderPaint
        )
    }

    private fun drawEdge(canvas: Canvas, edge: GraphEdge) {
        val paint = when (edge.type) {
            EdgeType.MATE -> mateEdgePaint
            EdgeType.PARENT_CHILD -> edgePaint
        }

        val startX = edge.fromNode.x + GraphNode.NODE_WIDTH / 2
        val startY = edge.fromNode.y + if (edge.type == EdgeType.PARENT_CHILD)
            GraphNode.NODE_HEIGHT else GraphNode.NODE_HEIGHT / 2
        val endX = edge.toNode.x + if (edge.type == EdgeType.MATE)
            0f else GraphNode.NODE_WIDTH / 2
        val endY = edge.toNode.y + if (edge.type == EdgeType.PARENT_CHILD)
            0f else GraphNode.NODE_HEIGHT / 2

        when (edge.type) {
            EdgeType.PARENT_CHILD -> {
                val midY = startY + (endY - startY) / 2
                canvas.drawLine(startX, startY, startX, midY, paint)
                canvas.drawLine(startX, midY, endX, midY, paint)
                canvas.drawLine(endX, midY, endX, endY, paint)
            }
            EdgeType.MATE -> {
                canvas.drawLine(
                    edge.fromNode.x + GraphNode.NODE_WIDTH,
                    startY,
                    edge.toNode.x,
                    endY,
                    paint
                )
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(currentMatrix)
            }
        }
        return true
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            currentMatrix.postTranslate(-distanceX, -distanceY)
            invalidate()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (getCurrentScale() > 1.5f) {
                currentMatrix.reset()
            } else {
                currentMatrix.postScale(2f, 2f, e.x, e.y)
            }
            constrainScale()
            invalidate()
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val invertedMatrix = Matrix()
            currentMatrix.invert(invertedMatrix)
            val point = floatArrayOf(e.x, e.y)
            invertedMatrix.mapPoints(point)

            val hitNode = findNodeAt(point[0], point[1])
            hitNode?.let {
                selectedNodeId = it.pigeonId
                onNodeClickListener?.invoke(it.pigeonId)
                invalidate()
                return true
            }
            return false
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newScale = getCurrentScale() * scaleFactor

            if (newScale in minScale..maxScale) {
                currentMatrix.postScale(
                    scaleFactor, scaleFactor,
                    detector.focusX, detector.focusY
                )
            }
            invalidate()
            return true
        }
    }

    private fun findNodeAt(x: Float, y: Float): GraphNode? {
        val result = layoutResult ?: return null
        val expandedWidth = GraphNode.NODE_WIDTH * 1.2f
        val expandedHeight = GraphNode.NODE_HEIGHT * 1.2f
        val halfExpandW = (expandedWidth - GraphNode.NODE_WIDTH) / 2
        val halfExpandH = (expandedHeight - GraphNode.NODE_HEIGHT) / 2

        return result.allNodes.find { node ->
            x >= node.x - halfExpandW && x <= node.x + GraphNode.NODE_WIDTH + halfExpandW &&
            y >= node.y - halfExpandH && y <= node.y + GraphNode.NODE_HEIGHT + halfExpandH
        }
    }

    private fun getCurrentScale(): Float {
        currentMatrix.getValues(matrixValues)
        return matrixValues[Matrix.MSCALE_X]
    }

    private fun constrainScale() {
        val scale = getCurrentScale()
        if (scale < minScale) {
            val factor = minScale / scale
            currentMatrix.postScale(factor, factor, width / 2f, height / 2f)
        } else if (scale > maxScale) {
            val factor = maxScale / scale
            currentMatrix.postScale(factor, factor, width / 2f, height / 2f)
        }
    }

    fun setGraphData(result: LayoutResult) {
        this.layoutResult = result
        currentMatrix.reset()
        invalidate()
    }

    fun setOnNodeClickListener(listener: (String) -> Unit) {
        this.onNodeClickListener = listener
    }

    fun zoomIn() {
        val newScale = getCurrentScale() * 1.25f
        if (newScale <= maxScale) {
            currentMatrix.postScale(1.25f, 1.25f, width / 2f, height / 2f)
            invalidate()
        }
    }

    fun zoomOut() {
        val newScale = getCurrentScale() * 0.8f
        if (newScale >= minScale) {
            currentMatrix.postScale(0.8f, 0.8f, width / 2f, height / 2f)
            invalidate()
        }
    }

    fun resetView() {
        currentMatrix.reset()
        invalidate()
    }
}
