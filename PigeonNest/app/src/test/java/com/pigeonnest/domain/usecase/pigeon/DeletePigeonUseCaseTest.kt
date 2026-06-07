package com.pigeonnest.domain.usecase.pigeon

import com.pigeonnest.domain.repository.PigeonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class DeletePigeonUseCaseTest {

    private val repository: PigeonRepository = mockk()
    private val useCase = DeletePigeonUseCase(repository)

    @Test
    fun `delete pigeon delegates to repository`() = runTest {
        coEvery { repository.deletePigeon("pigeon-1") } returns Result.success(Unit)

        val result = useCase("pigeon-1")

        assertTrue(result.isSuccess)
        coVerify { repository.deletePigeon("pigeon-1") }
    }

    @Test
    fun `delete pigeon returns failure when repository fails`() = runTest {
        coEvery { repository.deletePigeon("pigeon-1") } returns Result.failure(Exception("Not found"))

        val result = useCase("pigeon-1")

        assertTrue(result.isFailure)
    }
}
