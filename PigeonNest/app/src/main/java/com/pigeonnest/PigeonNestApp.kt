package com.pigeonnest

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class PigeonNestApp : Application() {

    override fun onCreate() {
        super.onCreate()
        deleteUnencryptedDatabaseIfExists()
    }

    /**
     * 删除旧的未加密数据库文件（如果存在）。
     * 首次启用 SQLCipher 加密时，旧数据库未加密会导致
     * "file is not a database" 崩溃。
     */
    private fun deleteUnencryptedDatabaseIfExists() {
        val dbFile = getDatabasePath("pigeon_nest.db")
        if (dbFile.exists()) {
            // 尝试检测是否为有效的 SQLCipher 加密数据库
            // SQLCipher 加密数据库的前 16 字节是 SQLite 格式 3 魔数，
            // 但文件头会被加密，所以如果文件头以 "SQLite format 3" 开头，
            // 说明是未加密的普通数据库，需要删除
            try {
                dbFile.inputStream().use { input ->
                    val header = ByteArray(16)
                    val read = input.read(header)
                    if (read >= 16) {
                        val headerStr = String(header, Charsets.UTF_8)
                        if (headerStr.startsWith("SQLite format 3")) {
                            Log.w("PigeonNestApp", "检测到未加密数据库，正在删除以启用 SQLCipher 加密...")
                            dbFile.delete()
                            // 同时删除 journal/wal/shm 等辅助文件
                            File(dbFile.parent, "${dbFile.name}-journal").delete()
                            File(dbFile.parent, "${dbFile.name}-wal").delete()
                            File(dbFile.parent, "${dbFile.name}-shm").delete()
                            Log.i("PigeonNestApp", "旧数据库已删除，SQLCipher 将在下次访问时创建加密数据库")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PigeonNestApp", "检测数据库类型失败，尝试强制删除", e)
                dbFile.delete()
            }
        }
    }
}
