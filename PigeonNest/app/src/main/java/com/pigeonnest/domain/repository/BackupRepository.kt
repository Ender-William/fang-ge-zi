package com.pigeonnest.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow

data class BackupInfo(
    val uri: Uri,
    val fileName: String,
    val createdAt: Long,
    val fileSizeBytes: Long
)

data class ImportResult(
    val importedLofts: Int,
    val importedPigeons: Int,
    val importedRelations: Int
)

enum class ImportMode { REPLACE, MERGE }

interface BackupRepository {
    suspend fun exportBackup(destinationUri: Uri): Result<String>
    suspend fun importBackup(backupUri: Uri, mode: ImportMode): Result<ImportResult>
    fun getBackupHistory(): Flow<List<BackupInfo>>
}
