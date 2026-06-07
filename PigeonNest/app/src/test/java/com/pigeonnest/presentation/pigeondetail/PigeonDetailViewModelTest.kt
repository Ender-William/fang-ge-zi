package com.pigeonnest.presentation.pigeondetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.pigeonnest.CoroutineTestRule
import com.pigeonnest.TestFixtures
import com.pigeonnest.data.file.PigeonPdfGenerator
import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.repository.LoftRepository
import com.pigeonnest.domain.repository.PigeonRepository
import com.pigeonnest.domain.usecase.family.GetLineageUseCase
import com.pigeonnest.domain.usecase.family.UpdateFamilyRelationUseCase
import com.pigeonnest.domain.usecase.pigeon.DeletePigeonUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonDetailUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonListUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

class PigeonDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val getPigeonDetailUseCase: GetPigeonDetailUseCase = mockk()
    private val deletePigeonUseCase: DeletePigeonUseCase = mockk()
    private val familyRepository: FamilyRepository = mockk()
    private val updateFamilyRelationUseCase: UpdateFamilyRelationUseCase = mockk()
    private val getPigeonListUseCase: GetPigeonListUseCase = mockk()
    private val getLineageUseCase: GetLineageUseCase = mockk()
    private val loftRepository: LoftRepository = mockk()
    private val pigeonPdfGenerator: PigeonPdfGenerator = mockk()

    private fun createViewModel() = PigeonDetailViewModel(
        getPigeonDetailUseCase, deletePigeonUseCase, familyRepository,
        updateFamilyRelationUseCase, getPigeonListUseCase, getLineageUseCase,
        loftRepository, pigeonPdfGenerator
    )

    @Test
    fun `loadPigeon sets pigeon state`() = runTest {
        val pigeon = TestFixtures.createPigeon()
        every { getPigeonDetailUseCase("p1") } returns flowOf(pigeon)
        coEvery { familyRepository.getFamilyRelation("p1") } returns null
        every { getPigeonListUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        viewModel.loadPigeon("p1")

        advanceUntilIdle()
        assertEquals(pigeon, viewModel.pigeon.value)
    }

    @Test
    fun `updateFamilyRelation sets updateResult on success`() = runTest {
        coEvery { updateFamilyRelationUseCase("p1", "f1", "m1", null) } returns Result.success(Unit)
        coEvery { familyRepository.getFamilyRelation("p1") } returns null

        val viewModel = createViewModel()
        viewModel.updateFamilyRelation("p1", "f1", "m1", null)

        advanceUntilIdle()
        assertTrue(viewModel.updateResult.value!!.isSuccess)
    }

    @Test
    fun `deletePigeon sets deleteResult`() = runTest {
        coEvery { deletePigeonUseCase("p1") } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.deletePigeon("p1")

        advanceUntilIdle()
        assertTrue(viewModel.deleteResult.value!!.isSuccess)
    }

    @Test
    fun `generateExportPdf generates file and sets pdfPreviewFile`() = runTest {
        val pigeon = TestFixtures.createPigeon(loft = Loft("l1", "娴嬭瘯楦借垗"))
        every { getPigeonDetailUseCase("p1") } returns flowOf(pigeon)
        coEvery { getLineageUseCase("p1", 3) } returns mockk()
        coEvery { loftRepository.getLoftById("l1") } returns Loft("l1", "娴嬭瘯楦借垗")
        val mockFile = mockk<File>()
        coEvery { pigeonPdfGenerator.generate(any(), any(), any()) } returns mockFile

        val viewModel = createViewModel()
        viewModel.generateExportPdf("p1")

        advanceUntilIdle()
        assertEquals(mockFile, viewModel.pdfPreviewFile.value)
        assertEquals(false, viewModel.isGeneratingPdf.value)
        assertNull(viewModel.pdfError.value)
    }

    @Test
    fun `generateExportPdf sets error when pigeon not found`() = runTest {
        every { getPigeonDetailUseCase("p1") } returns flowOf(null)

        val viewModel = createViewModel()
        viewModel.generateExportPdf("p1")

        advanceUntilIdle()
        assertNotNull(viewModel.pdfError.value)
        assertEquals(false, viewModel.isGeneratingPdf.value)
    }

    @Test
    fun `clearUpdateResult resets state`() = runTest {
        coEvery { updateFamilyRelationUseCase("p1", null, null, null) } returns Result.success(Unit)
        coEvery { familyRepository.getFamilyRelation("p1") } returns null

        val viewModel = createViewModel()
        viewModel.updateFamilyRelation("p1", null, null, null)
        advanceUntilIdle()

        viewModel.clearUpdateResult()
        assertNull(viewModel.updateResult.value)
    }
}
