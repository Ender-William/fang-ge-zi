package com.pigeonnest.domain.usecase.loft

import app.cash.turbine.test
import com.pigeonnest.TestFixtures
import com.pigeonnest.domain.repository.LoftRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetLoftListUseCaseTest {

    private val repository: LoftRepository = mockk()
    private val useCase = GetLoftListUseCase(repository)

    @Test
    fun `invoke returns all lofts`() = runTest {
        val lofts = listOf(TestFixtures.createLoft())
        every { repository.getAllLofts() } returns flowOf(lofts)

        useCase().test {
            assertEquals(lofts, awaitItem())
            awaitComplete()
        }
    }
}
