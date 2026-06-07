package com.pigeonnest.domain.usecase.pigeon

import app.cash.turbine.test
import com.pigeonnest.TestFixtures
import com.pigeonnest.domain.repository.PigeonRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchPigeonsUseCaseTest {

    private val repository: PigeonRepository = mockk()
    private val useCase = SearchPigeonsUseCase(repository)

    @Test
    fun `blank query returns all pigeons`() = runTest {
        val pigeons = listOf(TestFixtures.createPigeon())
        every { repository.getAllPigeons() } returns flowOf(pigeons)

        useCase("").test {
            assertEquals(pigeons, awaitItem())
            awaitComplete()
        }

        verify { repository.getAllPigeons() }
    }

    @Test
    fun `whitespace-only query returns all pigeons`() = runTest {
        val pigeons = listOf(TestFixtures.createPigeon())
        every { repository.getAllPigeons() } returns flowOf(pigeons)

        useCase("   ").test {
            assertEquals(pigeons, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `non-blank query trims and searches`() = runTest {
        val pigeons = listOf(TestFixtures.createPigeon())
        every { repository.searchPigeons("小白") } returns flowOf(pigeons)

        useCase("  小白  ").test {
            assertEquals(pigeons, awaitItem())
            awaitComplete()
        }

        verify { repository.searchPigeons("小白") }
    }
}
