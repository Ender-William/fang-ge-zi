package com.pigeonnest.presentation.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.pigeonnest.CoroutineTestRule
import com.pigeonnest.data.file.BackupManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val context: Context = mockk(relaxed = true)
    private val sharedPrefs: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)
    private val backupManager: BackupManager = mockk()

    private fun createViewModel(): SettingsViewModel {
        every { context.getSharedPreferences("settings", Context.MODE_PRIVATE) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        return SettingsViewModel(context, backupManager)
    }

    @Test
    fun `init loads settings from preferences`() {
        every { sharedPrefs.getInt("font_size", 0) } returns 1
        every { sharedPrefs.getBoolean("high_contrast", false) } returns true

        val viewModel = createViewModel()

        assertEquals(1, viewModel.settings.value.fontSize)
        assertEquals("大", viewModel.settings.value.fontSizeLabel)
        assertTrue(viewModel.settings.value.highContrast)
    }

    @Test
    fun `setFontSize updates settings and triggers recreate`() {
        every { sharedPrefs.getInt("font_size", 0) } returns 0
        every { sharedPrefs.getBoolean("high_contrast", false) } returns false

        val viewModel = createViewModel()
        viewModel.setFontSize(2)

        assertEquals(2, viewModel.settings.value.fontSize)
        assertEquals("超大", viewModel.settings.value.fontSizeLabel)
        assertTrue(viewModel.recreateNeeded.value)
        coVerify { editor.putInt("font_size", 2) }
    }

    @Test
    fun `setHighContrast updates settings and triggers recreate`() {
        every { sharedPrefs.getInt("font_size", 0) } returns 0
        every { sharedPrefs.getBoolean("high_contrast", false) } returns false

        val viewModel = createViewModel()
        viewModel.setHighContrast(true)

        assertTrue(viewModel.settings.value.highContrast)
        assertTrue(viewModel.recreateNeeded.value)
        coVerify { editor.putBoolean("high_contrast", true) }
    }

    @Test
    fun `setHighContrast does nothing when value unchanged`() {
        every { sharedPrefs.getInt("font_size", 0) } returns 0
        every { sharedPrefs.getBoolean("high_contrast", false) } returns false

        val viewModel = createViewModel()
        viewModel.setHighContrast(false)

        assertFalse(viewModel.recreateNeeded.value)
    }

    @Test
    fun `clearRecreateFlag resets state`() {
        every { sharedPrefs.getInt("font_size", 0) } returns 0
        every { sharedPrefs.getBoolean("high_contrast", false) } returns false

        val viewModel = createViewModel()
        viewModel.setFontSize(1)
        assertTrue(viewModel.recreateNeeded.value)

        viewModel.clearRecreateFlag()
        assertFalse(viewModel.recreateNeeded.value)
    }

    @Test
    fun `exportBackup sets toast message on success`() = runTest {
        every { sharedPrefs.getInt("font_size", 0) } returns 0
        every { sharedPrefs.getBoolean("high_contrast", false) } returns false
        val uri = mockk<android.net.Uri>()
        coEvery { backupManager.exportBackup(uri) } returns Result.success("备份成功")

        val viewModel = createViewModel()
        viewModel.exportBackup(uri)

        advanceUntilIdle()
        assertEquals("备份成功", viewModel.toastMessage.value)
    }

    @Test
    fun `exportBackup sets error toast on failure`() = runTest {
        every { sharedPrefs.getInt("font_size", 0) } returns 0
        every { sharedPrefs.getBoolean("high_contrast", false) } returns false
        val uri = mockk<android.net.Uri>()
        coEvery { backupManager.exportBackup(uri) } returns Result.failure(Exception("磁盘已满"))

        val viewModel = createViewModel()
        viewModel.exportBackup(uri)

        advanceUntilIdle()
        assertEquals("备份失败: 磁盘已满", viewModel.toastMessage.value)
    }

    @Test
    fun `importBackup sets success state`() = runTest {
        every { sharedPrefs.getInt("font_size", 0) } returns 0
        every { sharedPrefs.getBoolean("high_contrast", false) } returns false
        val uri = mockk<android.net.Uri>()
        coEvery { backupManager.importBackup(uri) } returns Result.success("导入成功")

        val viewModel = createViewModel()
        viewModel.importBackup(uri)

        advanceUntilIdle()
        assertTrue(viewModel.importSuccess.value)
        assertEquals("导入成功\n请重启应用以完成恢复", viewModel.toastMessage.value)
    }

    @Test
    fun `clearImportSuccess resets state`() = runTest {
        every { sharedPrefs.getInt("font_size", 0) } returns 0
        every { sharedPrefs.getBoolean("high_contrast", false) } returns false
        val uri = mockk<android.net.Uri>()
        coEvery { backupManager.importBackup(uri) } returns Result.success("导入成功")

        val viewModel = createViewModel()
        viewModel.importBackup(uri)
        advanceUntilIdle()
        assertTrue(viewModel.importSuccess.value)

        viewModel.clearImportSuccess()
        assertFalse(viewModel.importSuccess.value)
    }

    @Test
    fun `clearToast resets message`() = runTest {
        every { sharedPrefs.getInt("font_size", 0) } returns 0
        every { sharedPrefs.getBoolean("high_contrast", false) } returns false

        val viewModel = createViewModel()
        viewModel.clearToast()
        assertNull(viewModel.toastMessage.value)
    }
}
