package com.pigeonnest.domain.model

import android.graphics.RectF

enum class EdgeType { PARENT_CHILD, MATE }

class GraphNode(
    val pigeonId: String,
    val pigeonBrief: PigeonBrief,
    var x: Float = 0f,
    var y: Float = 0f,
    var level: Int = 0,
    val children: MutableList<GraphNode> = mutableListOf(),
    var father: GraphNode? = null,
    var mother: GraphNode? = null,
    var mate: GraphNode? = null
) {
    companion object {
        const val NODE_WIDTH = 200f
        const val NODE_HEIGHT = 100f
        const val HORIZONTAL_GAP = 60f
        const val VERTICAL_GAP = 120f
        const val MATE_GAP = 40f
    }

    // 只基于 pigeonId，避免循环引用导致 hashCode 无限递归
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GraphNode) return false
        return pigeonId == other.pigeonId
    }

    override fun hashCode(): Int = pigeonId.hashCode()

    override fun toString(): String = "GraphNode($pigeonId)"
}

data class GraphEdge(
    val fromNode: GraphNode,
    val toNode: GraphNode,
    val type: EdgeType
)

data class LayoutResult(
    val rootNode: GraphNode,
    val allNodes: List<GraphNode>,
    val edges: List<GraphEdge>,
    val bounds: RectF
)

data class GraphData(
    val rootNode: GraphNode,
    val allNodes: List<GraphNode>,
    val edges: List<GraphEdge>
)
