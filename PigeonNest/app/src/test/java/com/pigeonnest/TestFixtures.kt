package com.pigeonnest

import com.pigeonnest.domain.model.FamilyRelation
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.model.PigeonBrief
import com.pigeonnest.domain.model.PigeonStatus

object TestFixtures {

    fun createLoft(
        id: String = "loft-1",
        name: String = "测试鸽舍",
        location: String? = "屋顶",
        pigeonCount: Int = 0
    ) = Loft(
        id = id,
        name = name,
        location = location,
        pigeonCount = pigeonCount
    )

    fun createPigeon(
        id: String = "pigeon-1",
        ringNumber: String = "2024-001",
        name: String = "小白",
        color: String? = "白",
        gender: Gender = Gender.MALE,
        loft: Loft? = null,
        status: PigeonStatus = PigeonStatus.ACTIVE,
        familyRelation: FamilyRelation? = null
    ) = Pigeon(
        id = id,
        ringNumber = ringNumber,
        name = name,
        color = color,
        gender = gender,
        loft = loft,
        status = status,
        familyRelation = familyRelation,
        createdAt = 1700000000000L,
        updatedAt = 1700000000000L
    )

    fun createPigeonBrief(
        id: String = "pigeon-brief-1",
        ringNumber: String = "2024-002",
        name: String = "小红",
        gender: Gender = Gender.FEMALE,
        photoPath: String? = null,
        color: String? = "红",
        achievement: String? = null
    ) = PigeonBrief(
        id = id,
        ringNumber = ringNumber,
        name = name,
        gender = gender,
        photoPath = photoPath,
        color = color,
        achievement = achievement
    )

    fun createFamilyRelation(
        id: String = "fr-1",
        pigeonId: String = "pigeon-1",
        father: PigeonBrief? = null,
        mother: PigeonBrief? = null,
        mate: PigeonBrief? = null,
        children: List<PigeonBrief> = emptyList()
    ) = FamilyRelation(
        id = id,
        pigeonId = pigeonId,
        father = father,
        mother = mother,
        mate = mate,
        children = children
    )
}
