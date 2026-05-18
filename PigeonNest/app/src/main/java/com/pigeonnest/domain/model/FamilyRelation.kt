package com.pigeonnest.domain.model

data class FamilyRelation(
    val id: String,
    val pigeonId: String,
    val father: PigeonBrief? = null,
    val mother: PigeonBrief? = null,
    val mate: PigeonBrief? = null,
    val children: List<PigeonBrief> = emptyList()
)
