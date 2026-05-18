package com.pigeonnest.domain.repository

import com.pigeonnest.domain.model.FamilyRelation
import com.pigeonnest.domain.model.GraphData
import com.pigeonnest.domain.model.PigeonBrief

interface FamilyRepository {
    suspend fun getFamilyRelation(pigeonId: String): FamilyRelation?
    suspend fun getLineage(pigeonId: String, generations: Int = 3): LineageResult
    suspend fun getChildren(pigeonId: String): List<PigeonBrief>
    suspend fun getSiblings(pigeonId: String): List<PigeonBrief>
    suspend fun updateParents(pigeonId: String, fatherId: String?, motherId: String?): Result<Unit>
    suspend fun updateMate(pigeonId: String, mateId: String?): Result<Unit>
    suspend fun getGraphData(centerPigeonId: String, depth: Int = 3): GraphData
}

data class LineageResult(
    val pigeon: PigeonBrief,
    val father: LineageResult? = null,
    val mother: LineageResult? = null,
    val generation: Int = 0
)
