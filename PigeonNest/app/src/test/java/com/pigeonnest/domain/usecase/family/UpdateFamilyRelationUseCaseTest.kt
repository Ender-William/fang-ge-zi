package com.pigeonnest.domain.usecase.family

import com.pigeonnest.domain.repository.FamilyRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateFamilyRelationUseCaseTest {

    private val repository: FamilyRepository = mockk()
    private val useCase = UpdateFamilyRelationUseCase(repository)

    @Test
    fun `update parents only calls updateParents`() = runTest {
        coEvery { repository.updateParents("p1", "father-1", "mother-1") } returns Result.success(Unit)

        val result = useCase("p1", fatherId = "father-1", motherId = "mother-1")

        assertTrue(result.isSuccess)
        coVerify { repository.updateParents("p1", "father-1", "mother-1") }
        coVerify(exactly = 0) { repository.updateMate(any(), any()) }
    }

    @Test
    fun `update mate only calls updateMate`() = runTest {
        coEvery { repository.updateMate("p1", "mate-1") } returns Result.success(Unit)

        val result = useCase("p1", mateId = "mate-1")

        assertTrue(result.isSuccess)
        coVerify { repository.updateMate("p1", "mate-1") }
        coVerify(exactly = 0) { repository.updateParents(any(), any(), any()) }
    }

    @Test
    fun `update both parents and mate calls both`() = runTest {
        coEvery { repository.updateParents("p1", "father-1", "mother-1") } returns Result.success(Unit)
        coEvery { repository.updateMate("p1", "mate-1") } returns Result.success(Unit)

        val result = useCase("p1", fatherId = "father-1", motherId = "mother-1", mateId = "mate-1")

        assertTrue(result.isSuccess)
        coVerify { repository.updateParents("p1", "father-1", "mother-1") }
        coVerify { repository.updateMate("p1", "mate-1") }
    }

    @Test
    fun `returns parent failure when updateParents fails`() = runTest {
        coEvery { repository.updateParents("p1", "father-1", null) } returns Result.failure(Exception("Invalid father"))

        val result = useCase("p1", fatherId = "father-1")

        assertTrue(result.isFailure)
        assertEquals("Invalid father", result.exceptionOrNull()?.message)
    }

    @Test
    fun `returns mate failure when updateMate fails`() = runTest {
        coEvery { repository.updateMate("p1", "mate-1") } returns Result.failure(Exception("Invalid mate"))

        val result = useCase("p1", mateId = "mate-1")

        assertTrue(result.isFailure)
        assertEquals("Invalid mate", result.exceptionOrNull()?.message)
    }

    @Test
    fun `returns mate failure when both fail and parent succeeds`() = runTest {
        coEvery { repository.updateParents("p1", "father-1", null) } returns Result.success(Unit)
        coEvery { repository.updateMate("p1", "mate-1") } returns Result.failure(Exception("Invalid mate"))

        val result = useCase("p1", fatherId = "father-1", mateId = "mate-1")

        assertTrue(result.isFailure)
        assertEquals("Invalid mate", result.exceptionOrNull()?.message)
    }

    @Test
    fun `no params returns success without calling repository`() = runTest {
        val result = useCase("p1")

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { repository.updateParents(any(), any(), any()) }
        coVerify(exactly = 0) { repository.updateMate(any(), any()) }
    }
}
