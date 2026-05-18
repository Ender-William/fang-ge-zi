package com.pigeonnest.data.file

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.gson.GsonBuilder
import com.pigeonnest.data.local.dao.FamilyRelationDao
import com.pigeonnest.data.local.dao.LoftDao
import com.pigeonnest.data.local.dao.PigeonDao
import com.pigeonnest.data.local.dao.PigeonPhotoDao
import com.pigeonnest.data.local.entity.FamilyRelationEntity
import com.pigeonnest.data.local.entity.LoftEntity
import com.pigeonnest.data.local.entity.PigeonEntity
import com.pigeonnest.data.local.entity.PigeonPhotoEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class BackupData(
    val backup_version: Int,
    val backup_date: Long,
    val app_version: String,
    val data: BackupContent
)

data class BackupContent(
    val lofts: List<LoftEntity>,
    val pigeons: List<PigeonEntity>,
    val family_relations: List<FamilyRelationEntity> = emptyList(),
    val pigeon_photos: List<PigeonPhotoEntity> = emptyList()
)

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val loftDao: LoftDao,
    private val pigeonDao: PigeonDao,
    private val familyRelationDao: FamilyRelationDao,
    private val pigeonPhotoDao: PigeonPhotoDao,
    private val photoStorage: PhotoStorageManager
) {
    companion object {
        const val BACKUP_VERSION = 1
        const val BACKUP_FILE_PREFIX = "pigeonnest_backup_"
        const val BACKUP_FILE_EXTENSION = ".zip"
    }

    suspend fun exportBackup(): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
            val backupDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "PigeonNest/backups"
            ).apply { mkdirs() }

            val backupFile = File(backupDir, "${BACKUP_FILE_PREFIX}${timestamp}${BACKUP_FILE_EXTENSION}")

            val lofts = loftDao.getAll()
            val pigeons = pigeonDao.getAll()
            val familyRelations = familyRelationDao.getAll()
            val pigeonPhotos = pigeonPhotoDao.getAll()

            val backupData = BackupData(
                backup_version = BACKUP_VERSION,
                backup_date = System.currentTimeMillis(),
                app_version = "1.0.0",
                data = BackupContent(
                    lofts = lofts,
                    pigeons = pigeons,
                    family_relations = familyRelations,
                    pigeon_photos = pigeonPhotos
                )
            )

            val jsonString = GsonBuilder().setPrettyPrinting().create().toJson(backupData)

            ZipOutputStream(backupFile.outputStream()).use { zos ->
                zos.putNextEntry(ZipEntry("data.json"))
                zos.write(jsonString.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                photoStorage.getAllPhotoFiles().forEach { photoFile ->
                    if (photoFile.exists()) {
                        val entryName = "photos/${photoFile.name}"
                        zos.putNextEntry(ZipEntry(entryName))
                        photoFile.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackup(backupUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val tempDir = File(context.cacheDir, "backup_temp").apply {
                deleteRecursively()
                mkdirs()
            }

            context.contentResolver.openInputStream(backupUri)?.use { input ->
                ZipInputStream(input).use { zis ->
                    var entry: ZipEntry?
                    while (zis.nextEntry.also { entry = it } != null) {
                        val entryFile = File(tempDir, entry!!.name)
                        entryFile.parentFile?.mkdirs()
                        entryFile.outputStream().use { output ->
                            zis.copyTo(output)
                        }
                    }
                }
            } ?: return@withContext Result.failure(IllegalArgumentException("无法读取备份文件"))

            val jsonFile = File(tempDir, "data.json")
            if (!jsonFile.exists()) {
                return@withContext Result.failure(IllegalArgumentException("备份文件格式错误"))
            }

            val gson = GsonBuilder().create()
            val backupData = gson.fromJson(jsonFile.readText(), BackupData::class.java)

            if (backupData.backup_version > BACKUP_VERSION) {
                return@withContext Result.failure(
                    UnsupportedOperationException("备份版本 ${backupData.backup_version} 不兼容，请升级应用")
                )
            }

            // REPLACE mode: clear existing data and insert backup data
            loftDao.deleteAll()
            pigeonDao.deleteAll()
            familyRelationDao.deleteAll()
            pigeonPhotoDao.deleteAll()

            backupData.data.lofts.forEach { loftDao.insert(it) }
            backupData.data.pigeons.forEach { pigeonDao.insert(it) }
            backupData.data.family_relations.forEach { familyRelationDao.insert(it) }
            backupData.data.pigeon_photos.forEach { pigeonPhotoDao.insert(it) }

            // Copy photos
            val photosDir = File(tempDir, "photos")
            if (photosDir.exists()) {
                photoStorage.importPhotosFromDirectory(photosDir)
            }

            tempDir.deleteRecursively()

            Result.success("导入成功：${backupData.data.lofts.size} 鸽棚, ${backupData.data.pigeons.size} 鸽子, ${backupData.data.family_relations.size} 关系, ${backupData.data.pigeon_photos.size} 照片")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
