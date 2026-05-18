package com.pigeonnest.domain.model

data class Loft(
    val id: String,
    val name: String,
    val location: String? = null,
    val description: String? = null,
    val sortOrder: Int = 0,
    val capacity: Int? = null,
    val colorTag: String? = null,
    val pigeonCount: Int = 0
)
