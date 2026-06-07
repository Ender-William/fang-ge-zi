package com.pigeonnest.presentation.pigeonedit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.pigeonnest.CoroutineTestRule
import com.pigeonnest.TestFixtures
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.model.PigeonStatus
import com.pigeonnest.domain.usecase.loft.GetLoftListUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonDetailUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonListUseCase
import com.pigeonnest.domain.usecase.pigeon.SavePigeonUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PigeonEditViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val getPigeonDetailUseCase: GetPigeonDetailUseCase = mockk()
    private val savePigeonUseCase: SavePigeonUseCase = mockk()
    private val getLoftListUseCase: GetLoftListUseCase = mockk()
    private val getPigeonListUseCase: GetPigeonListUseCase = mockk()

    private fun createViewModel() = PigeonEditViewModel(
        getPigeonDetailUseCase, savePigeonUseCase, getLoftListUseCase, getPigeonListUseCase
    )

    @Test
    fun `init loads lofts and all pigeons`() = runTest {
        val lofts = listOf(TestFixtures.createLoft())
        val pigeons = listOf(TestFixtures.createPigeon())
        every { getLoftListUseCase() } returns flowOf(lofts)
        every { getPigeonListUseCase() } returns flowOf(pigeons)

        val viewModel = createViewModel()

        advanceUntilIdle()
        assertEquals(lofts, viewModel.lofts.value)
        assertEquals(pigeons, viewModel.allPigeons.value)
    }

    @Test
    fun `onPigeonLoaded populates form fields`() = runTest {
        every { getLoftListUseCase() } returns flowOf(emptyList())
        every { getPigeonListUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        val familyRelation = TestFixtures.createFamilyRelation(
            pigeonId = "p1",
            father = TestFixtures.createPigeonBrief(id = "f1"),
            mother = TestFixtures.createPigeonBrief(id = "m1"),
            mate = TestFixtures.createPigeonBrief(id = "mate1")
        )
        val pigeon = TestFixtures.createPigeon(
            gender = Gender.MALE,
            familyRelation = familyRelation
        )

        viewModel.onPigeonLoaded(pigeon)

        assertEquals("f1", viewModel.fatherId.value)
        assertEquals("m1", viewModel.motherId.value)
        assertEquals("mate1", viewModel.mateId.value)
    }

    @Test
    fun `step navigation works correctly`() = runTest {
        every { getLoftListUseCase() } returns flowOf(emptyList())
        every { getPigeonListUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        assertEquals(1, viewModel.currentStep.value)

        viewModel.nextStep()
        assertEquals(2, viewModel.currentStep.value)

        viewModel.nextStep()
        assertEquals(3, viewModel.currentStep.value)

        viewModel.nextStep()
        assertEquals(3, viewModel.currentStep.value) // capped at 3

        viewModel.prevStep()
        assertEquals(2, viewModel.currentStep.value)

        viewModel.prevStep()
        assertEquals(1, viewModel.currentStep.value)

        viewModel.prevStep()
        assertEquals(1, viewModel.currentStep.value) // capped at 1
    }

    @Test
    fun `setStep clamps to valid range`() = runTest {
        every { getLoftListUseCase() } returns flowOf(emptyList())
        every { getPigeonListUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        viewModel.setStep(0)
        assertEquals(1, viewModel.currentStep.value)

        viewModel.setStep(5)
        assertEquals(3, viewModel.currentStep.value)
    }

    @Test
    fun `save calls use case with collected params`() = runTest {
        every { getLoftListUseCase() } returns flowOf(emptyList())
        every { getPigeonListUseCase() } returns flowOf(emptyList())

        coEvery { savePigeonUseCase(any()) } returns Result.success("p1")

        val viewModel = createViewModel()
        viewModel.setGender(Gender.FEMALE)
        viewModel.setColor("白")
        viewModel.setLoftId("loft-1")
        viewModel.setStatus(PigeonStatus.SOLD)
        viewModel.setFatherId("f1")

        viewModel.save(id = null, name = "小白", ringNumber = "2024-001", notes = "备注")

        advanceUntilIdle()
        assertTrue(viewModel.saveResult.value!!.isSuccess)
    }

    @Test
    fun `clearResult resets save result`() = runTest {
        every { getLoftListUseCase() } returns flowOf(emptyList())
        every { getPigeonListUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        viewModel.clearResult()
        assertNull(viewModel.saveResult.value)
    }
}
