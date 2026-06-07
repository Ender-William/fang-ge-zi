package com.pigeonnest.presentation.familygraph

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.pigeonnest.CoroutineTestRule
import com.pigeonnest.TestFixtures
import com.pigeonnest.domain.model.GraphNode
import com.pigeonnest.domain.model.PigeonBrief
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.repository.PigeonRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FamilyGraphViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val pigeonRepository: PigeonRepository = mockk()
    private val familyRepository: FamilyRepository = mockk()

    private fun createViewModel() = FamilyGraphViewModel(pigeonRepository, familyRepository)

    @Test
    fun `loadGraph with valid pigeon sets success result`() = runTest {
        val pigeon = TestFixtures.createPigeon(id = "p1")
        every { pigeonRepository.getPigeonById("p1") } returns flowOf(pigeon)

        // Mock GraphLayoutManager
        mockkObject(GraphLayoutManager)
        coEvery { GraphLayoutManager.buildGraphFromPigeon(any(), any(), any()) } returns com.pigeonnest.domain.model.LayoutResult(
            rootNode = GraphNode("p1", PigeonBrief("p1", "R1", "A", Gender.MALE, null)),
            allNodes = emptyList(),
            edges = emptyList(),
            bounds = android.graphics.RectF()
        )

        val viewModel = createViewModel()
        viewModel.loadGraph("p1")

        advanceUntilIdle()
        assertTrue(viewModel.graphData.value!!.isSuccess)
    }

    @Test
    fun `loadGraph with nonexistent pigeon sets failure result`() = runTest {
        every { pigeonRepository.getPigeonById("unknown") } returns flowOf(null)

        val viewModel = createViewModel()
        viewModel.loadGraph("unknown")

        advanceUntilIdle()
        assertTrue(viewModel.graphData.value!!.isFailure)
    }

    @Test
    fun `setDepth clamps to valid range and reloads`() = runTest {
        val pigeon = TestFixtures.createPigeon(id = "p1")
        every { pigeonRepository.getPigeonById("p1") } returns flowOf(pigeon)
        mockkObject(GraphLayoutManager)
        coEvery { GraphLayoutManager.buildGraphFromPigeon(any(), any(), any()) } returns com.pigeonnest.domain.model.LayoutResult(
            rootNode = GraphNode("p1", PigeonBrief("p1", "R1", "A", Gender.MALE, null)),
            allNodes = emptyList(),
            edges = emptyList(),
            bounds = android.graphics.RectF()
        )

        val viewModel = createViewModel()
        viewModel.loadGraph("p1")
        advanceUntilIdle()

        // Test below MIN becomes UNLIMITED (0)
        viewModel.setDepth(0)
        advanceUntilIdle()
        assertEquals(0, viewModel.currentDepth.value)

        // Test above MAX clamps
        viewModel.setDepth(25)
        advanceUntilIdle()
        assertEquals(20, viewModel.currentDepth.value)
    }

    @Test
    fun `increaseDepth cycles from unlimited to min`() = runTest {
        val viewModel = createViewModel()
        viewModel.setDepth(0) // UNLIMITED
        viewModel.increaseDepth()
        assertEquals(1, viewModel.currentDepth.value)
    }

    @Test
    fun `decreaseDepth cycles from min to unlimited`() = runTest {
        val viewModel = createViewModel()
        viewModel.setDepth(1)
        viewModel.decreaseDepth()
        assertEquals(0, viewModel.currentDepth.value)
    }

    @Test
    fun `decreaseDepth decreases when above min`() = runTest {
        val viewModel = createViewModel()
        viewModel.setDepth(5)
        viewModel.decreaseDepth()
        assertEquals(4, viewModel.currentDepth.value)
    }
}
