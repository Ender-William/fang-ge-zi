package com.pigeonnest.domain.model

data class Pigeon(
    val id: String,
    val ringNumber: String,
    val name: String,
    val color: String? = null,
    val gender: Gender = Gender.UNKNOWN,
    val birthDate: Long? = null,
    val entryDate: Long? = null,
    val photoPath: String? = null,
    val eyePhotoPath: String? = null,
    val loft: Loft? = null,
    val cageNumber: String? = null,
    val status: PigeonStatus = PigeonStatus.ACTIVE,
    val notes: String? = null,
    val familyRelation: FamilyRelation? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
