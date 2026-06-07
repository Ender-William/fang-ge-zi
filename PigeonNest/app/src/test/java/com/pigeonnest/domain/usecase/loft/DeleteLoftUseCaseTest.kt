package com.pigeonnest.domain.usecase.loft

import com.pigeonnest.domain.repository.LoftRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteLoftUseCaseTest {

    private val repository: LoftRepository = mockk()
    private val useCase = DeleteLoftUseCase(repository)

    @Test
    fun `delete loft delegates to repository`() = runTest {
        coEvery { repository.deleteLoft("loft-1") } returns Result.success(Unit)

        val result = useCase("loft-1")

        assertTrue(result.isSuccess)
        coVerify { repository.deleteLoft("loft-1") }
    }

    @Test
    fun `delete loft returns failure when repository fails`() = runTest {
        coEvery { repository.deleteLoft("loft-1") } returns Result.failure(Exception("Has pigeons"))

        val result = useCase("loft-1")

        assertTrue(result.isFailure)
    }
}
