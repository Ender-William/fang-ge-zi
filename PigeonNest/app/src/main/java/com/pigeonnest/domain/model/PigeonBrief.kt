package com.pigeonnest.domain.model

data class PigeonBrief(
    val id: String,
    val ringNumber: String,
    val name: String,
    val gender: Gender,
    val photoPath: String?,
    val color: String? = null
)
