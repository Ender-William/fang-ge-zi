package com.pigeonnest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FamilyGraphTest {

    @Test
    fun `GraphNode equals is based only on pigeonId`() {
        val node1 = GraphNode("p1", PigeonBrief("p1", "R1", "A", Gender.MALE, null))
        val node2 = GraphNode("p1", PigeonBrief("p1", "R2", "B", Gender.FEMALE, null))

        assertEquals(node1, node2)
        assertEquals(node1.hashCode(), node2.hashCode())
    }

    @Test
    fun `GraphNode not equals for different pigeonId`() {
        val node1 = GraphNode("p1", PigeonBrief("p1", "R1", "A", Gender.MALE, null))
        val node2 = GraphNode("p2", PigeonBrief("p2", "R1", "A", Gender.MALE, null))

        assertNotEquals(node1, node2)
    }

    @Test
    fun `GraphNode equals handles circular references without stack overflow`() {
        val node1 = GraphNode("p1", PigeonBrief("p1", "R1", "A", Gender.MALE, null))
        val node2 = GraphNode("p2", PigeonBrief("p2", "R2", "B", Gender.FEMALE, null))

        // Create circular reference
        node1.mate = node2
        node2.mate = node1

        // equals should still work based on pigeonId only
        val node1Copy = GraphNode("p1", PigeonBrief("p1", "R1", "A", Gender.MALE, null))
        assertEquals(node1, node1Copy)
    }

    @Test
    fun `GraphEdge holds correct nodes and type`() {
        val from = GraphNode("p1", PigeonBrief("p1", "R1", "A", Gender.MALE, null))
        val to = GraphNode("p2", PigeonBrief("p2", "R2", "B", Gender.FEMALE, null))
        val edge = GraphEdge(from, to, EdgeType.MATE)

        assertEquals(from, edge.fromNode)
        assertEquals(to, edge.toNode)
        assertEquals(EdgeType.MATE, edge.type)
    }
}
