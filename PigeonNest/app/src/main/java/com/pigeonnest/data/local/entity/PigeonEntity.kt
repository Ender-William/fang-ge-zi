package com.pigeonnest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "pigeons",
    indices = [
        Index(value = ["ring_number"], unique = true),
        Index(value = ["loft_id"]),
        Index(value = ["status"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = LoftEntity::class,
            parentColumns = ["id"],
            childColumns = ["loft_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class PigeonEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "ring_number")
    val ringNumber: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color")
    val color: String? = null,

    @ColumnInfo(name = "gender")
    val gender: Int = 0,

    @ColumnInfo(name = "birth_date")
    val birthDate: Long? = null,

    @ColumnInfo(name = "entry_date")
    val entryDate: Long? = null,

    @ColumnInfo(name = "photo_path")
    val photoPath: String? = null,

    @ColumnInfo(name = "eye_photo_path")
    val eyePhotoPath: String? = null,

    @ColumnInfo(name = "loft_id")
    val loftId: String? = null,

    @ColumnInfo(name = "cage_number")
    val cageNumber: String? = null,

    @ColumnInfo(name = "status")
    val status: Int = 0,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "achievement")
    val achievement: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Int = 0
)
