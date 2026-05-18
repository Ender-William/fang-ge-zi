package com.pigeonnest.data.file

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.google.gson.GsonBuilder
import com.pigeonnest.data.local.dao.FamilyRelationDao
import com.pigeonnest.data.local.dao.LocationHistoryDao
import com.pigeonnest.data.local.dao.LoftDao
import com.pigeonnest.data.local.dao.PigeonDao
import com.pigeonnest.data.local.dao.PigeonPhotoDao
import com.pigeonnest.data.local.entity.FamilyRelationEntity
import com.pigeonnest.data.local.entity.LocationHistoryEntity
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
    val data: BackupContent,
    val preferences: Map<String, Map<String, Any?>> = emptyMap()
)

data class BackupContent(
    val lofts: List<LoftEntity>,
    val pigeons: List<PigeonEntity>,
    val family_relations: List<FamilyRelationEntity> = emptyList(),
    val pigeon_photos: List<PigeonPhotoEntity> = emptyList(),
    val location_history: List<LocationHistoryEntity> = emptyList()
)

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val loftDao: LoftDao,
    private val pigeonDao: PigeonDao,
    private val familyRelationDao: FamilyRelationDao,
    private val pigeonPhotoDao: PigeonPhotoDao,
    private val locationHistoryDao: LocationHistoryDao,
    private val photoStorage: PhotoStorageManager
) {
    companion object {
        const val BACKUP_VERSION = 2
        const val BACKUP_FILE_PREFIX = "pigeonnest_backup_"
        const val BACKUP_FILE_EXTENSION = ".zip"
        private val PREFS_NAMES = listOf("settings", "family_names")
        const val TAG = "BackupManager"
    }

    suspend fun exportBackup(targetUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val lofts = loftDao.getAll()
            val pigeons = pigeonDao.getAll()
            val familyRelations = familyRelationDao.getAll()
            val pigeonPhotos = pigeonPhotoDao.getAll()
            val locationHistory = locationHistoryDao.getAll()

            // 导出所有 SharedPreferences
            val prefsMap = mutableMapOf<String, Map<String, Any?>>()
            PREFS_NAMES.forEach { name ->
                val sp = context.getSharedPreferences(name, Context.MODE_PRIVATE)
                prefsMap[name] = sp.all
            }

            val backupData = BackupData(
                backup_version = BACKUP_VERSION,
                backup_date = System.currentTimeMillis(),
                app_version = "1.0.0",
                data = BackupContent(
                    lofts = lofts,
                    pigeons = pigeons,
                    family_relations = familyRelations,
                    pigeon_photos = pigeonPhotos,
                    location_history = locationHistory
                ),
                preferences = prefsMap
            )

            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonString = gson.toJson(backupData)

            val photosDir = File(context.filesDir, "photos")
            val photoFiles = photoStorage.getAllPhotoFiles()
            Log.d(TAG, "找到 ${photoFiles.size} 个照片文件待导出")

            context.contentResolver.openOutputStream(targetUri)?.use { output ->
                ZipOutputStream(output).use { zos ->
                    zos.putNextEntry(ZipEntry("data.json"))
                    zos.write(jsonString.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()

                    // 照片保留完整目录结构导出，ZIP 路径统一使用正斜杠
                    photoFiles.forEach { photoFile ->
                        if (photoFile.exists()) {
                            val relativePath = photoFile.relativeTo(photosDir).path
                                .replace("\\", "/")
                            val entryName = "photos/$relativePath"
                            Log.d(TAG, "导出照片: $entryName")
                            zos.putNextEntry(ZipEntry(entryName))
                            photoFile.inputStream().use { it.copyTo(zos) }
                            zos.closeEntry()
                        }
                    }
                }
            } ?: return@withContext Result.failure(IllegalStateException("无法写入目标文件"))

            Result.success(
                "备份成功：${lofts.size} 鸽棚, ${pigeons.size} 鸽子, " +
                "${familyRelations.size} 关系, ${photoFiles.size} 照片文件"
            )
        } catch (e: Exception) {
            Log.e(TAG, "导出失败", e)
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
                        // ZIP 条目名统一将反斜杠转为正斜杠，确保跨平台兼容
                        val safeName = entry!!.name.replace("\\", "/")
                        val entryFile = File(tempDir, safeName)
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

            // 使用事务模式：先清空再插入
            // 注意：由于外键约束，需要按正确顺序操作
            // 先清空从表，再清空主表
            locationHistoryDao.deleteAll()
            pigeonPhotoDao.deleteAll()
            familyRelationDao.deleteAll()
            pigeonDao.deleteAll()
            loftDao.deleteAll()

            // 插入主表
            loftDao.insertAll(backupData.data.lofts)
            pigeonDao.insertAll(backupData.data.pigeons)
            // 插入从表
            familyRelationDao.insertAll(backupData.data.family_relations)
            pigeonPhotoDao.insertAll(backupData.data.pigeon_photos)
            locationHistoryDao.insertAll(backupData.data.location_history)

            // 恢复照片（保留目录结构）
            val photosDir = File(tempDir, "photos")
            if (photosDir.exists()) {
                photoStorage.importPhotosFromDirectory(photosDir)
            }

            // 恢复 SharedPreferences
            backupData.preferences.forEach { (name, values) ->
                val sp = context.getSharedPreferences(name, Context.MODE_PRIVATE)
                val editor = sp.edit()
                editor.clear()
                values.forEach { (key, value) ->
                    when (value) {
                        is String -> editor.putString(key, value)
                        is Int -> editor.putInt(key, value)
                        is Long -> editor.putLong(key, value)
                        is Float -> editor.putFloat(key, value)
                        is Boolean -> editor.putBoolean(key, value)
                        is Set<*> -> @Suppress("UNCHECKED_CAST") editor.putStringSet(key, value as Set<String>)
                        null -> editor.remove(key)
                    }
                }
                editor.apply()
            }

            tempDir.deleteRecursively()

            Result.success(
                "导入成功：${backupData.data.lofts.size} 鸽棚, ${backupData.data.pigeons.size} 鸽子, " +
                "${backupData.data.family_relations.size} 关系, ${backupData.data.pigeon_photos.size} 照片记录, " +
                "${backupData.data.location_history.size} 位置记录"
            )
        } catch (e: Exception) {
            Log.e(TAG, "导入失败", e)
            Result.failure(e)
        }
    }
}
