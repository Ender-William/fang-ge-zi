package com.pigeonnest.presentation.loftmanage

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.pigeonnest.CoroutineTestRule
import com.pigeonnest.TestFixtures
import com.pigeonnest.domain.usecase.loft.DeleteLoftUseCase
import com.pigeonnest.domain.usecase.loft.GetLoftListUseCase
import com.pigeonnest.domain.usecase.loft.SaveLoftUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoftListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val getLoftListUseCase: GetLoftListUseCase = mockk()
    private val saveLoftUseCase: SaveLoftUseCase = mockk()
    private val deleteLoftUseCase: DeleteLoftUseCase = mockk()

    private fun createViewModel(): LoftListViewModel {
        every { getLoftListUseCase() } returns flowOf(emptyList())
        return LoftListViewModel(getLoftListUseCase, saveLoftUseCase, deleteLoftUseCase)
    }

    @Test
    fun `init loads lofts`() = runTest {
        val lofts = listOf(TestFixtures.createLoft())
        every { getLoftListUseCase() } returns flowOf(lofts)

        val viewModel = LoftListViewModel(getLoftListUseCase, saveLoftUseCase, deleteLoftUseCase)

        advanceUntilIdle()
        assertEquals(lofts, viewModel.lofts.value)
    }

    @Test
    fun `addLoft calls save use case`() = runTest {
        coEvery { saveLoftUseCase(name = "新鸽舍") } returns Result.success("loft-1")

        val viewModel = createViewModel()
        viewModel.addLoft("新鸽舍")

        advanceUntilIdle()
        coVerify { saveLoftUseCase(name = "新鸽舍") }
    }

    @Test
    fun `updateLoft calls save with all fields`() = runTest {
        val loft = com.pigeonnest.domain.model.Loft(
            id = "loft-1",
            name = "更新鸽舍",
            location = "屋顶",
            description = "描述",
            capacity = 50
        )
        coEvery { saveLoftUseCase(id = loft.id, name = loft.name, location = loft.location, description = loft.description, capacity = loft.capacity) } returns Result.success(loft.id)

        val viewModel = createViewModel()
        viewModel.updateLoft(loft)

        advanceUntilIdle()
        coVerify { saveLoftUseCase(id = loft.id, name = loft.name, location = loft.location, description = loft.description, capacity = loft.capacity) }
    }

    @Test
    fun `deleteLoft returns success callback`() = runTest {
        coEvery { deleteLoftUseCase("loft-1") } returns Result.success(Unit)

        val viewModel = createViewModel()
        var success = false
        var message = ""
        viewModel.deleteLoft("loft-1") { s, m ->
            success = s
            message = m
        }

        advanceUntilIdle()
        assertTrue(success)
        assertEquals("删除成功", message)
    }

    @Test
    fun `deleteLoft returns failure callback`() = runTest {
        coEvery { deleteLoftUseCase("loft-1") } returns Result.failure(Exception("Has pigeons"))

        val viewModel = createViewModel()
        var success = true
        var message = ""
        viewModel.deleteLoft("loft-1") { s, m ->
            success = s
            message = m
        }

        advanceUntilIdle()
        assertFalse(success)
        assertEquals("Has pigeons", message)
    }
}
