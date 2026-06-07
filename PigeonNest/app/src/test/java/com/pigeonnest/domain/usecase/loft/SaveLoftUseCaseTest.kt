package com.pigeonnest.domain.usecase.loft

import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.repository.LoftRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveLoftUseCaseTest {

    private val repository: LoftRepository = mockk()
    private val useCase = SaveLoftUseCase(repository)

    @Test
    fun `save loft with valid name succeeds`() = runTest {
        coEvery { repository.saveLoft(any()) } returns Result.success("loft-1")

        val result = useCase(name = "测试鸽舍")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `save loft trims name and fields`() = runTest {
        val loftSlot = slot<Loft>()
        coEvery { repository.saveLoft(capture(loftSlot)) } returns Result.success("loft-1")

        useCase(
            name = "  测试鸽舍  ",
            location = "  屋顶  ",
            description = "  主鸽舍  "
        )

        assertEquals("测试鸽舍", loftSlot.captured.name)
        assertEquals("屋顶", loftSlot.captured.location)
        assertEquals("主鸽舍", loftSlot.captured.description)
    }

    @Test
    fun `save loft with blank name fails`() = runTest {
        val result = useCase(name = "")

        assertTrue(result.isFailure)
        assertEquals("鸽舍名称不能为空", result.exceptionOrNull()?.message)
    }

    @Test
    fun `save loft with whitespace-only name fails`() = runTest {
        val result = useCase(name = "   ")

        assertTrue(result.isFailure)
        assertEquals("鸽舍名称不能为空", result.exceptionOrNull()?.message)
    }

    @Test
    fun `save loft preserves existing id when updating`() = runTest {
        val loftSlot = slot<Loft>()
        coEvery { repository.saveLoft(capture(loftSlot)) } returns Result.success("existing-id")

        useCase(id = "existing-id", name = "更新鸽舍")

        assertEquals("existing-id", loftSlot.captured.id)
    }

    @Test
    fun `save loft generates new id when creating`() = runTest {
        val loftSlot = slot<Loft>()
        coEvery { repository.saveLoft(capture(loftSlot)) } returns Result.success("new-id")

        useCase(name = "新鸽舍")

        assertTrue(loftSlot.captured.id.isNotBlank())
        assertEquals("新鸽舍", loftSlot.captured.name)
    }
}
