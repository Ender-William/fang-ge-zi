package com.pigeonnest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "family_relations",
    indices = [
        Index(value = ["pigeon_id"]),
        Index(value = ["father_id"]),
        Index(value = ["mother_id"]),
        Index(value = ["mate_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = PigeonEntity::class,
            parentColumns = ["id"],
            childColumns = ["pigeon_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PigeonEntity::class,
            parentColumns = ["id"],
            childColumns = ["father_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = PigeonEntity::class,
            parentColumns = ["id"],
            childColumns = ["mother_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = PigeonEntity::class,
            parentColumns = ["id"],
            childColumns = ["mate_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class FamilyRelationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "pigeon_id")
    val pigeonId: String,

    @ColumnInfo(name = "father_id")
    val fatherId: String? = null,

    @ColumnInfo(name = "mother_id")
    val motherId: String? = null,

    @ColumnInfo(name = "mate_id")
    val mateId: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
