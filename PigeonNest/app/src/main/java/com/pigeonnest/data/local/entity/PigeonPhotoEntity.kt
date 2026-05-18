package com.pigeonnest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "pigeon_photos",
    indices = [Index(value = ["pigeon_id"])],
    foreignKeys = [
        ForeignKey(
            entity = PigeonEntity::class,
            parentColumns = ["id"],
            childColumns = ["pigeon_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PigeonPhotoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "pigeon_id")
    val pigeonId: String,

    @ColumnInfo(name = "photo_path")
    val photoPath: String,

    @ColumnInfo(name = "caption")
    val caption: String? = null,

    @ColumnInfo(name = "taken_date")
    val takenDate: Long? = null,

    @ColumnInfo(name = "is_primary")
    val isPrimary: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
