package com.pigeonnest.domain.usecase.pigeon

import app.cash.turbine.test
import com.pigeonnest.TestFixtures
import com.pigeonnest.domain.model.PigeonStatus
import com.pigeonnest.domain.repository.PigeonRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPigeonListUseCaseTest {

    private val repository: PigeonRepository = mockk()
    private val useCase = GetPigeonListUseCase(repository)

    @Test
    fun `invoke with no params returns all pigeons`() = runTest {
        val pigeons = listOf(TestFixtures.createPigeon())
        every { repository.getAllPigeons() } returns flowOf(pigeons)

        useCase().test {
            assertEquals(pigeons, awaitItem())
            awaitComplete()
        }

        verify { repository.getAllPigeons() }
    }

    @Test
    fun `invoke with loftId returns pigeons by loft`() = runTest {
        val pigeons = listOf(TestFixtures.createPigeon(loft = TestFixtures.createLoft()))
        every { repository.getPigeonsByLoft("loft-1") } returns flowOf(pigeons)

        useCase(loftId = "loft-1").test {
            assertEquals(pigeons, awaitItem())
            awaitComplete()
        }

        verify { repository.getPigeonsByLoft("loft-1") }
    }

    @Test
    fun `invoke with status returns pigeons by status`() = runTest {
        val pigeons = listOf(TestFixtures.createPigeon(status = PigeonStatus.SOLD))
        every { repository.getPigeonsByStatus(PigeonStatus.SOLD) } returns flowOf(pigeons)

        useCase(status = PigeonStatus.SOLD).test {
            assertEquals(pigeons, awaitItem())
            awaitComplete()
        }

        verify { repository.getPigeonsByStatus(PigeonStatus.SOLD) }
    }

    @Test
    fun `invoke with both loftId and status prefers loftId`() = runTest {
        val pigeons = listOf(TestFixtures.createPigeon())
        every { repository.getPigeonsByLoft("loft-1") } returns flowOf(pigeons)

        useCase(loftId = "loft-1", status = PigeonStatus.ACTIVE).test {
            assertEquals(pigeons, awaitItem())
            awaitComplete()
        }

        verify { repository.getPigeonsByLoft("loft-1") }
        verify(exactly = 0) { repository.getPigeonsByStatus(any()) }
    }
}
