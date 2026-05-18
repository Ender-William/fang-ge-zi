package com.pigeonnest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "location_history",
    indices = [
        Index(value = ["pigeon_id"]),
        Index(value = ["from_loft_id"]),
        Index(value = ["to_loft_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = PigeonEntity::class,
            parentColumns = ["id"],
            childColumns = ["pigeon_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LoftEntity::class,
            parentColumns = ["id"],
            childColumns = ["from_loft_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = LoftEntity::class,
            parentColumns = ["id"],
            childColumns = ["to_loft_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class LocationHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "pigeon_id")
    val pigeonId: String,

    @ColumnInfo(name = "from_loft_id")
    val fromLoftId: String? = null,

    @ColumnInfo(name = "to_loft_id")
    val toLoftId: String? = null,

    @ColumnInfo(name = "move_date")
    val moveDate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "note")
    val note: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
