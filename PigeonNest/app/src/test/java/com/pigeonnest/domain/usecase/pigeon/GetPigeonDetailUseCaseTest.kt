package com.pigeonnest.domain.usecase.pigeon

import app.cash.turbine.test
import com.pigeonnest.TestFixtures
import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.repository.PigeonRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GetPigeonDetailUseCaseTest {

    private val pigeonRepository: PigeonRepository = mockk()
    private val familyRepository: FamilyRepository = mockk()
    private val useCase = GetPigeonDetailUseCase(pigeonRepository, familyRepository)

    @Test
    fun `invoke enriches pigeon with family relation`() = runTest {
        val pigeon = TestFixtures.createPigeon()
        val familyRelation = TestFixtures.createFamilyRelation(pigeonId = pigeon.id)

        every { pigeonRepository.getPigeonById(pigeon.id) } returns flowOf(pigeon)
        coEvery { familyRepository.getFamilyRelation(pigeon.id) } returns familyRelation

        useCase(pigeon.id).test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(familyRelation, result?.familyRelation)
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns null when pigeon not found`() = runTest {
        every { pigeonRepository.getPigeonById("unknown") } returns flowOf(null)

        useCase("unknown").test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }
}
