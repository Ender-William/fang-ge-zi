package com.pigeonnest.data.repository

import android.net.Uri
import com.pigeonnest.data.file.BackupManager
import com.pigeonnest.domain.repository.BackupInfo
import com.pigeonnest.domain.repository.BackupRepository
import com.pigeonnest.domain.repository.ImportMode
import com.pigeonnest.domain.repository.ImportResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val backupManager: BackupManager
) : BackupRepository {

    override suspend fun exportBackup(destinationUri: Uri): Result<String> {
        return backupManager.exportBackup(destinationUri)
    }

    override suspend fun importBackup(backupUri: Uri, mode: ImportMode): Result<ImportResult> {
        if (mode == ImportMode.MERGE) {
            return Result.failure(UnsupportedOperationException("MERGE mode is not supported yet"))
        }
        val result = backupManager.importBackup(backupUri)
        return result.map { message ->
            parseImportResult(message)
        }
    }

    override fun getBackupHistory(): Flow<List<BackupInfo>> {
        return emptyFlow()
    }

    private fun parseImportResult(message: String): ImportResult {
        val regex = Regex("导入成功：(\\d+) 鸽棚, (\\d+) 鸽子, (\\d+) 关系")
        val match = regex.find(message)
        return if (match != null) {
            ImportResult(
                importedLofts = match.groupValues[1].toIntOrNull() ?: 0,
                importedPigeons = match.groupValues[2].toIntOrNull() ?: 0,
                importedRelations = match.groupValues[3].toIntOrNull() ?: 0
            )
        } else {
            ImportResult(0, 0, 0)
        }
    }
}
