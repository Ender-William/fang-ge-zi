package com.pigeonnest.domain.usecase.pigeon

import android.net.Uri
import com.pigeonnest.TestFixtures
import com.pigeonnest.data.file.PhotoStorageManager
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.model.PigeonStatus
import com.pigeonnest.domain.repository.LoftRepository
import com.pigeonnest.domain.repository.PigeonRepository
import com.pigeonnest.domain.usecase.family.UpdateFamilyRelationUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SavePigeonUseCaseTest {

    private val pigeonRepository: PigeonRepository = mockk()
    private val loftRepository: LoftRepository = mockk()
    private val updateFamilyRelationUseCase: UpdateFamilyRelationUseCase = mockk()
    private val photoStorage: PhotoStorageManager = mockk()
    private val useCase = SavePigeonUseCase(
        pigeonRepository, loftRepository, updateFamilyRelationUseCase, photoStorage
    )

    @Test
    fun `save new pigeon with valid params succeeds`() = runTest {
        val loft = TestFixtures.createLoft()
        coEvery { loftRepository.getLoftById("loft-1") } returns loft
        coEvery { pigeonRepository.savePigeon(any()) } returns Result.success("pigeon-1")
        coEvery { updateFamilyRelationUseCase(any(), any(), any(), any()) } returns Result.success(Unit)

        val result = useCase(
            SavePigeonUseCase.Params(
                ringNumber = "2024-001",
                name = "小白",
                loftId = "loft-1",
                gender = Gender.MALE,
                status = PigeonStatus.ACTIVE
            )
        )

        assertTrue(result.isSuccess)
        coVerify { pigeonRepository.savePigeon(any()) }
    }

    @Test
    fun `save pigeon trims input fields`() = runTest {
        val loft = TestFixtures.createLoft()
        coEvery { loftRepository.getLoftById("loft-1") } returns loft
        coEvery { pigeonRepository.savePigeon(any()) } returns Result.success("pigeon-1")
        coEvery { updateFamilyRelationUseCase(any(), any(), any(), any()) } returns Result.success(Unit)

        val pigeonSlot = slot<com.pigeonnest.domain.model.Pigeon>()
        coEvery { pigeonRepository.savePigeon(capture(pigeonSlot)) } returns Result.success("pigeon-1")

        useCase(
            SavePigeonUseCase.Params(
                ringNumber = "  2024-001  ",
                name = "  小白  ",
                color = "  白  ",
                loftId = "loft-1"
            )
        )

        assertEquals("2024-001", pigeonSlot.captured.ringNumber)
        assertEquals("小白", pigeonSlot.captured.name)
        assertEquals("白", pigeonSlot.captured.color)
    }

    @Test
    fun `save pigeon with blank ringNumber fails`() = runTest {
        val result = useCase(
            SavePigeonUseCase.Params(ringNumber = "", name = "小白")
        )

        assertTrue(result.isFailure)
        assertEquals("足环号不能为空", result.exceptionOrNull()?.message)
    }

    @Test
    fun `save pigeon with blank name fails`() = runTest {
        val result = useCase(
            SavePigeonUseCase.Params(ringNumber = "2024-001", name = "")
        )

        assertTrue(result.isFailure)
        assertEquals("鸽子名称不能为空", result.exceptionOrNull()?.message)
    }

    @Test
    fun `save pigeon with too long ringNumber fails`() = runTest {
        val result = useCase(
            SavePigeonUseCase.Params(ringNumber = "A".repeat(51), name = "小白")
        )

        assertTrue(result.isFailure)
        assertEquals("足环号过长（最多50字符）", result.exceptionOrNull()?.message)
    }

    @Test
    fun `save pigeon with too long name fails`() = runTest {
        val result = useCase(
            SavePigeonUseCase.Params(ringNumber = "2024-001", name = "A".repeat(31))
        )

        assertTrue(result.isFailure)
        assertEquals("名称过长（最多30字符）", result.exceptionOrNull()?.message)
    }

    @Test
    fun `save pigeon preserves existing id when updating`() = runTest {
        val loft = TestFixtures.createLoft()
        coEvery { loftRepository.getLoftById("loft-1") } returns loft
        coEvery { pigeonRepository.savePigeon(any()) } returns Result.success("existing-id")
        coEvery { updateFamilyRelationUseCase(any(), any(), any(), any()) } returns Result.success(Unit)

        val pigeonSlot = slot<com.pigeonnest.domain.model.Pigeon>()
        coEvery { pigeonRepository.savePigeon(capture(pigeonSlot)) } returns Result.success("existing-id")

        useCase(
            SavePigeonUseCase.Params(
                id = "existing-id",
                ringNumber = "2024-001",
                name = "小白",
                loftId = "loft-1"
            )
        )

        assertEquals("existing-id", pigeonSlot.captured.id)
    }

    @Test
    fun `save pigeon saves photo when photoUri provided`() = runTest {
        val loft = TestFixtures.createLoft()
        val photoUri = mockk<Uri>()
        coEvery { loftRepository.getLoftById("loft-1") } returns loft
        coEvery { photoStorage.savePigeonPhoto(any(), any()) } returns "/photos/new.jpg"
        coEvery { pigeonRepository.savePigeon(any()) } returns Result.success("pigeon-1")
        coEvery { updateFamilyRelationUseCase(any(), any(), any(), any()) } returns Result.success(Unit)

        useCase(
            SavePigeonUseCase.Params(
                ringNumber = "2024-001",
                name = "小白",
                loftId = "loft-1",
                photoUri = photoUri
            )
        )

        coVerify { photoStorage.savePigeonPhoto(any(), photoUri) }
    }

    @Test
    fun `save pigeon does not call family update when save fails`() = runTest {
        coEvery { pigeonRepository.savePigeon(any()) } returns Result.failure(Exception("DB error"))

        useCase(
            SavePigeonUseCase.Params(
                ringNumber = "2024-001",
                name = "小白"
            )
        )

        coVerify(exactly = 0) { updateFamilyRelationUseCase(any(), any(), any(), any()) }
    }
}
