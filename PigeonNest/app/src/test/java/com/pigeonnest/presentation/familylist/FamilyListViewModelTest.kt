package com.pigeonnest.presentation.familylist

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.pigeonnest.CoroutineTestRule
import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.usecase.pigeon.GetPigeonListUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FamilyListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val familyRepository: FamilyRepository = mockk()
    private val getPigeonListUseCase: GetPigeonListUseCase = mockk()
    private val context: Context = mockk(relaxed = true)
    private val sharedPrefs: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)

    private fun createViewModel(): FamilyListViewModel {
        every { context.getSharedPreferences("family_names", Context.MODE_PRIVATE) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.apply() } returns Unit
        return FamilyListViewModel(familyRepository, getPigeonListUseCase, context)
    }

    @Test
    fun `loadFamilies with empty pigeons returns empty list`() = runTest {
        every { getPigeonListUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        viewModel.loadFamilies()
        advanceUntilIdle()

        assertTrue(viewModel.families.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `setCustomFamilyName stores in preferences and reloads`() = runTest {
        every { getPigeonListUseCase() } returns flowOf(emptyList())
        every { sharedPrefs.getString("family_name_p1", null) } returns "Custom Name"

        val viewModel = createViewModel()
        viewModel.setCustomFamilyName("p1", "Custom Name")

        advanceUntilIdle()
        coVerify { editor.putString("family_name_p1", "Custom Name") }
    }

    @Test
    fun `setCustomFamilyName removes preference when blank`() = runTest {
        every { getPigeonListUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        viewModel.setCustomFamilyName("p1", "")

        advanceUntilIdle()
        coVerify { editor.remove("family_name_p1") }
    }
}
