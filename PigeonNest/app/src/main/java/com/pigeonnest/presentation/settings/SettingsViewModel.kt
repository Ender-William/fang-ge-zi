package com.pigeonnest.presentation.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pigeonnest.data.file.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppSettings(
    val fontSize: Int = 0,
    val fontSizeLabel: String = "标准",
    val highContrast: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupManager: BackupManager
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    private val _recreateNeeded = MutableStateFlow(false)
    val recreateNeeded: StateFlow<Boolean> = _recreateNeeded

    private fun loadSettings(): AppSettings {
        val fontSize = prefs.getInt("font_size", 0)
        val label = when (fontSize) {
            1 -> "大"
            2 -> "超大"
            else -> "标准"
        }
        return AppSettings(
            fontSize = fontSize,
            fontSizeLabel = label,
            highContrast = prefs.getBoolean("high_contrast", false)
        )
    }

    fun setFontSize(size: Int) {
        prefs.edit().putInt("font_size", size).apply()
        val label = when (size) {
            1 -> "大"
            2 -> "超大"
            else -> "标准"
        }
        _settings.value = _settings.value.copy(fontSize = size, fontSizeLabel = label)
        _recreateNeeded.value = true
    }

    fun setHighContrast(enabled: Boolean) {
        if (_settings.value.highContrast == enabled) return
        prefs.edit().putBoolean("high_contrast", enabled).apply()
        _settings.value = _settings.value.copy(highContrast = enabled)
        _recreateNeeded.value = true
    }

    fun clearRecreateFlag() {
        _recreateNeeded.value = false
    }

    fun exportBackup() {
        viewModelScope.launch {
            val result = backupManager.exportBackup()
            result.fold(
                onSuccess = { _toastMessage.value = "备份已导出" },
                onFailure = { _toastMessage.value = "备份失败: ${it.message}" }
            )
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
