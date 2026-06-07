package com.pigeonnest.domain.usecase.family

import com.pigeonnest.TestFixtures
import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.repository.LineageResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetLineageUseCaseTest {

    private val repository: FamilyRepository = mockk()
    private val useCase = GetLineageUseCase(repository)

    @Test
    fun `invoke with default generations returns lineage`() = runTest {
        val pigeon = TestFixtures.createPigeonBrief()
        val lineage = LineageResult(pigeon = pigeon, generation = 0)
        coEvery { repository.getLineage("pigeon-1", 3) } returns lineage

        val result = useCase("pigeon-1")

        assertEquals(pigeon, result.pigeon)
        assertEquals(0, result.generation)
    }

    @Test
    fun `invoke with custom generations passes correct value`() = runTest {
        val pigeon = TestFixtures.createPigeonBrief()
        val lineage = LineageResult(pigeon = pigeon, generation = 0)
        coEvery { repository.getLineage("pigeon-1", 5) } returns lineage

        val result = useCase("pigeon-1", generations = 5)

        assertEquals(pigeon, result.pigeon)
    }
}
