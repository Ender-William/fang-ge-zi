package com.pigeonnest.presentation.familygraph

import android.graphics.RectF
import com.pigeonnest.domain.model.*
import com.pigeonnest.domain.repository.FamilyRepository
import java.util.ArrayDeque

object GraphLayoutManager {

    suspend fun buildGraphFromPigeon(
        centerPigeon: Pigeon,
        familyRepository: FamilyRepository,
        depth: Int = 10
    ): LayoutResult {
        val graphData = familyRepository.getGraphData(centerPigeon.id, depth)
        val layoutManager = GraphLayoutEngine()
        return layoutManager.calculateLayout(graphData)
    }
}

class GraphLayoutEngine {

    fun calculateLayout(graphData: GraphData): LayoutResult {
        val allNodes = graphData.allNodes

        if (allNodes.isEmpty()) {
            val root = graphData.rootNode
            root.x = 0f
            root.y = 0f
            root.level = 0
            return LayoutResult(
                root,
                listOf(root),
                emptyList(),
                RectF(-150f, -100f, 350f, 220f)
            )
        }

        // BFS from root to assign generational levels
        val levels = mutableMapOf<GraphNode, Int>()
        val queue = ArrayDeque<Pair<GraphNode, Int>>()
        val visited = mutableSetOf<String>()

        queue.add(graphData.rootNode to 0)
        visited.add(graphData.rootNode.pigeonId)

        while (queue.isNotEmpty()) {
            val (node, level) = queue.removeFirst()
            levels[node] = level

            // Descendants: level + 1
            node.children.forEach { child ->
                if (child.pigeonId !in visited) {
                    visited.add(child.pigeonId)
                    queue.add(child to level + 1)
                }
            }

            // Ancestors: level - 1
            node.father?.let { father ->
                if (father.pigeonId !in visited) {
                    visited.add(father.pigeonId)
                    queue.add(father to level - 1)
                }
            }
            node.mother?.let { mother ->
                if (mother.pigeonId !in visited) {
                    visited.add(mother.pigeonId)
                    queue.add(mother to level - 1)
                }
            }

            // Mate: same level
            node.mate?.let { mate ->
                if (mate.pigeonId !in visited) {
                    visited.add(mate.pigeonId)
                    queue.add(mate to level)
                }
            }
        }

        // Group nodes by level
        val nodesByLevel = levels.entries
            .groupBy { it.value }
            .mapValues { entry -> entry.value.map { it.key } }
            .toSortedMap()

        // Position nodes vertically and horizontally
        val minLevel = nodesByLevel.keys.first()

        nodesByLevel.forEach { (level, nodes) ->
            val normalizedLevel = level - minLevel
            val y = normalizedLevel * (GraphNode.NODE_HEIGHT + GraphNode.VERTICAL_GAP)
            nodes.forEach {
                it.y = y
                it.level = level
            }

            val totalWidth = nodes.size * GraphNode.NODE_WIDTH +
                maxOf(0, nodes.size - 1) * GraphNode.HORIZONTAL_GAP
            val startX = -totalWidth / 2f
            nodes.forEachIndexed { index, node ->
                node.x = startX + index * (GraphNode.NODE_WIDTH + GraphNode.HORIZONTAL_GAP)
            }
        }

        // Calculate bounds
        val minX = allNodes.minOf { it.x }
        val maxX = allNodes.maxOf { it.x + GraphNode.NODE_WIDTH }
        val minY = allNodes.minOf { it.y }
        val maxY = allNodes.maxOf { it.y + GraphNode.NODE_HEIGHT }

        return LayoutResult(
            graphData.rootNode,
            allNodes,
            graphData.edges,
            RectF(minX - 50, minY - 50, maxX + 50, maxY + 50)
        )
    }
}
