package com.pigeonnest.presentation.loftmanage

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.pigeonnest.CoroutineTestRule
import com.pigeonnest.TestFixtures
import com.pigeonnest.domain.repository.LoftRepository
import com.pigeonnest.domain.usecase.pigeon.GetPigeonListUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LoftDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val loftRepository: LoftRepository = mockk()
    private val getPigeonListUseCase: GetPigeonListUseCase = mockk()

    private fun createViewModel() = LoftDetailViewModel(loftRepository, getPigeonListUseCase)

    @Test
    fun `loadLoft sets loft and pigeons`() = runTest {
        val loft = TestFixtures.createLoft(id = "l1", name = "娴嬭瘯楦借垗")
        val pigeons = listOf(TestFixtures.createPigeon(loft = loft))
        coEvery { loftRepository.getLoftById("l1") } returns loft
        every { getPigeonListUseCase(loftId = "l1") } returns flowOf(pigeons)

        val viewModel = createViewModel()
        viewModel.loadLoft("l1")

        advanceUntilIdle()
        assertEquals(loft, viewModel.loft.value)
        assertEquals(pigeons, viewModel.pigeons.value)
    }

    @Test
    fun `loadLoft returns null when loft not found`() = runTest {
        coEvery { loftRepository.getLoftById("unknown") } returns null
        every { getPigeonListUseCase(loftId = "unknown") } returns flowOf(emptyList())

        val viewModel = createViewModel()
        viewModel.loadLoft("unknown")

        advanceUntilIdle()
        assertEquals(null, viewModel.loft.value)
        assertEquals(emptyList<com.pigeonnest.domain.model.Pigeon>(), viewModel.pigeons.value)
    }
}
