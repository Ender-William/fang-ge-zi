package com.pigeonnest.domain.usecase.family

import com.pigeonnest.domain.model.GraphData
import com.pigeonnest.domain.model.GraphNode
import com.pigeonnest.domain.model.PigeonBrief
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.repository.FamilyRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetFamilyGraphUseCaseTest {

    private val repository: FamilyRepository = mockk()
    private val useCase = GetFamilyGraphUseCase(repository)

    @Test
    fun `invoke with default depth returns graph data`() = runTest {
        val rootNode = GraphNode("p1", PigeonBrief("p1", "R1", "A", Gender.MALE, null))
        val graphData = GraphData(rootNode, listOf(rootNode), emptyList())
        coEvery { repository.getGraphData("pigeon-1", 3) } returns graphData

        val result = useCase("pigeon-1")

        assertEquals(rootNode, result.rootNode)
    }

    @Test
    fun `invoke with custom depth passes correct value`() = runTest {
        val rootNode = GraphNode("p1", PigeonBrief("p1", "R1", "A", Gender.MALE, null))
        val graphData = GraphData(rootNode, listOf(rootNode), emptyList())
        coEvery { repository.getGraphData("pigeon-1", 10) } returns graphData

        val result = useCase("pigeon-1", depth = 10)

        assertEquals(rootNode, result.rootNode)
    }
}
