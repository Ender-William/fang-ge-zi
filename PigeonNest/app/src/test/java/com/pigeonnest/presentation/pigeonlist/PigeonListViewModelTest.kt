package com.pigeonnest.presentation.pigeonlist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.pigeonnest.CoroutineTestRule
import com.pigeonnest.TestFixtures
import com.pigeonnest.domain.usecase.pigeon.DeletePigeonUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonListUseCase
import com.pigeonnest.domain.usecase.pigeon.SearchPigeonsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PigeonListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val getPigeonListUseCase: GetPigeonListUseCase = mockk()
    private val searchPigeonsUseCase: SearchPigeonsUseCase = mockk()
    private val deletePigeonUseCase: DeletePigeonUseCase = mockk()

    @Test
    fun `init loads pigeons and emits Success when data exists`() = runTest {
        val pigeons = listOf(TestFixtures.createPigeon())
        every { getPigeonListUseCase() } returns flowOf(pigeons)

        val viewModel = PigeonListViewModel(getPigeonListUseCase, searchPigeonsUseCase, deletePigeonUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PigeonListUiState.Success)
        assertEquals(pigeons, (state as PigeonListUiState.Success).pigeons)
    }

    @Test
    fun `init emits Empty when no pigeons`() = runTest {
        every { getPigeonListUseCase() } returns flowOf(emptyList())

        val viewModel = PigeonListViewModel(getPigeonListUseCase, searchPigeonsUseCase, deletePigeonUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PigeonListUiState.Empty)
    }

    @Test
    fun `recentPigeons takes at most 10 items`() = runTest {
        val pigeons = (1..15).map { TestFixtures.createPigeon(id = "p$it", name = "P$it") }
        every { getPigeonListUseCase() } returns flowOf(pigeons)

        val viewModel = PigeonListViewModel(getPigeonListUseCase, searchPigeonsUseCase, deletePigeonUseCase)
        advanceUntilIdle()

        assertEquals(10, viewModel.recentPigeons.value.size)
    }

    @Test
    fun `search switches to search use case`() = runTest {
        val allPigeons = listOf(TestFixtures.createPigeon())
        val searchResults = listOf(TestFixtures.createPigeon(name = "搜索结果"))
        every { getPigeonListUseCase() } returns flowOf(allPigeons)
        every { searchPigeonsUseCase("test") } returns flowOf(searchResults)

        val viewModel = PigeonListViewModel(getPigeonListUseCase, searchPigeonsUseCase, deletePigeonUseCase)
        advanceUntilIdle()

        viewModel.search("test")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PigeonListUiState.Success)
        assertEquals("搜索结果", (state as PigeonListUiState.Success).pigeons[0].name)
    }

    @Test
    fun `deletePigeon calls use case`() = runTest {
        every { getPigeonListUseCase() } returns flowOf(emptyList())
        coEvery { deletePigeonUseCase("p1") } returns Result.success(Unit)

        val viewModel = PigeonListViewModel(getPigeonListUseCase, searchPigeonsUseCase, deletePigeonUseCase)
        viewModel.deletePigeon("p1")
        advanceUntilIdle()

        coVerify { deletePigeonUseCase("p1") }
    }
}
