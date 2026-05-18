package com.pigeonnest.domain.model

data class PigeonPhoto(
    val id: String,
    val pigeonId: String,
    val photoPath: String,
    val caption: String?,
    val takenDate: Long?,
    val isPrimary: Boolean
)
