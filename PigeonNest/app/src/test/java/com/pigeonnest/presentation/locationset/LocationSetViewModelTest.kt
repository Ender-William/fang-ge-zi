package com.pigeonnest.presentation.locationset

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.pigeonnest.CoroutineTestRule
import com.pigeonnest.TestFixtures
import com.pigeonnest.domain.usecase.loft.GetLoftListUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonDetailUseCase
import com.pigeonnest.domain.usecase.pigeon.SavePigeonUseCase
import io.mockk.coEvery
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

class LocationSetViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val getLoftListUseCase: GetLoftListUseCase = mockk()
    private val getPigeonDetailUseCase: GetPigeonDetailUseCase = mockk()
    private val savePigeonUseCase: SavePigeonUseCase = mockk()

    private fun createViewModel(): LocationSetViewModel {
        every { getLoftListUseCase() } returns flowOf(emptyList())
        return LocationSetViewModel(getLoftListUseCase, getPigeonDetailUseCase, savePigeonUseCase)
    }

    @Test
    fun `init loads lofts`() = runTest {
        val lofts = listOf(TestFixtures.createLoft())
        every { getLoftListUseCase() } returns flowOf(lofts)

        val viewModel = LocationSetViewModel(getLoftListUseCase, getPigeonDetailUseCase, savePigeonUseCase)

        advanceUntilIdle()
        assertEquals(lofts, viewModel.lofts.value)
    }

    @Test
    fun `updateLocation succeeds when pigeon exists`() = runTest {
        val pigeon = TestFixtures.createPigeon(id = "p1", name = "小白", ringNumber = "2024-001")
        every { getPigeonDetailUseCase("p1") } returns flowOf(pigeon)
        coEvery { savePigeonUseCase(any()) } returns Result.success("p1")

        val viewModel = createViewModel()
        var success = false
        var message = ""
        viewModel.updateLocation("p1", "loft-2") { s, m ->
            success = s
            message = m
        }

        advanceUntilIdle()
        assertTrue(success)
        assertEquals("位置变更成功", message)
    }

    @Test
    fun `updateLocation fails when pigeon not found`() = runTest {
        every { getPigeonDetailUseCase("p1") } returns flowOf(null)

        val viewModel = createViewModel()
        var success = true
        var message = ""
        viewModel.updateLocation("p1", "loft-2") { s, m ->
            success = s
            message = m
        }

        advanceUntilIdle()
        assertFalse(success)
        assertEquals("鸽子不存在", message)
    }

    @Test
    fun `updateLocation fails when save fails`() = runTest {
        val pigeon = TestFixtures.createPigeon(id = "p1", name = "小白", ringNumber = "2024-001")
        every { getPigeonDetailUseCase("p1") } returns flowOf(pigeon)
        coEvery { savePigeonUseCase(any()) } returns Result.failure(Exception("Save error"))

        val viewModel = createViewModel()
        var success = true
        var message = ""
        viewModel.updateLocation("p1", "loft-2") { s, m ->
            success = s
            message = m
        }

        advanceUntilIdle()
        assertFalse(success)
        assertEquals("Save error", message)
    }
}
