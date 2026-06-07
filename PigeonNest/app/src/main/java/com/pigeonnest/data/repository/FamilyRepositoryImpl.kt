package com.pigeonnest.data.repository

import com.pigeonnest.data.local.dao.FamilyRelationDao
import com.pigeonnest.data.local.dao.PigeonDao
import com.pigeonnest.data.local.entity.PigeonEntity
import com.pigeonnest.data.local.mapper.PigeonMapper
import com.pigeonnest.domain.model.EdgeType
import com.pigeonnest.domain.model.FamilyRelation
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.model.GraphData
import com.pigeonnest.domain.model.GraphEdge
import com.pigeonnest.domain.model.GraphNode
import com.pigeonnest.domain.model.PigeonBrief
import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.repository.LineageResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyRepositoryImpl @Inject constructor(
    private val pigeonDao: PigeonDao,
    private val familyRelationDao: FamilyRelationDao,
    private val pigeonMapper: PigeonMapper
) : FamilyRepository {

    override suspend fun getFamilyRelation(pigeonId: String): FamilyRelation? {
        val relation = familyRelationDao.getByPigeonId(pigeonId)
        val father = relation?.fatherId?.let { getBrief(it) }
        val mother = relation?.motherId?.let { getBrief(it) }
        val mate = relation?.mateId?.let { getBrief(it) }
        val children = familyRelationDao.getChildrenRelations(pigeonId).mapNotNull { getBrief(it.pigeonId) }

        // 即使没有直接关系记录，只要有后代/配偶，也应该返回
        if (relation == null && children.isEmpty() && mate == null && father == null && mother == null) {
            return null
        }

        return FamilyRelation(
            id = relation?.id ?: java.util.UUID.randomUUID().toString(),
            pigeonId = pigeonId,
            father = father,
            mother = mother,
            mate = mate,
            children = children
        )
    }

    override suspend fun getLineage(pigeonId: String, generations: Int): LineageResult {
        return buildLineage(pigeonId, 0, generations)
    }

    private suspend fun buildLineage(pigeonId: String, generation: Int, maxGeneration: Int): LineageResult {
        val brief = getBrief(pigeonId) ?: PigeonBrief(pigeonId, "", "", Gender.UNKNOWN, null)
        if (generation >= maxGeneration) {
            return LineageResult(pigeon = brief, generation = generation)
        }
        val relation = familyRelationDao.getByPigeonId(pigeonId)
        var father = relation?.fatherId?.let { buildLineage(it, generation + 1, maxGeneration) }
        var mother = relation?.motherId?.let { buildLineage(it, generation + 1, maxGeneration) }

        // 推断母亲：如果母亲缺失但父亲存在，尝试从父亲的配偶中推断
        val fatherNode = father
        if (mother == null && fatherNode != null) {
            val fatherRelation = familyRelationDao.getByPigeonId(fatherNode.pigeon.id)
            fatherRelation?.mateId?.let { mateId ->
                if (mateId != pigeonId) {
                    mother = buildLineage(mateId, generation + 1, maxGeneration)
                }
            }
        }

        // 推断父亲：如果父亲缺失但母亲存在，尝试从母亲的配偶中推断
        val motherNode = mother
        if (father == null && motherNode != null) {
            val motherRelation = familyRelationDao.getByPigeonId(motherNode.pigeon.id)
            motherRelation?.mateId?.let { mateId ->
                if (mateId != pigeonId) {
                    father = buildLineage(mateId, generation + 1, maxGeneration)
                }
            }
        }

        return LineageResult(
            pigeon = brief,
            father = father,
            mother = mother,
            generation = generation
        )
    }

    override suspend fun getChildren(pigeonId: String): List<PigeonBrief> {
        return familyRelationDao.getChildrenRelations(pigeonId).mapNotNull { getBrief(it.pigeonId) }
    }

    override suspend fun getSiblings(pigeonId: String): List<PigeonBrief> {
        val relation = familyRelationDao.getByPigeonId(pigeonId) ?: return emptyList()
        val parentIds = listOfNotNull(relation.fatherId, relation.motherId)
        if (parentIds.isEmpty()) return emptyList()
        val siblings = mutableSetOf<PigeonBrief>()
        parentIds.forEach { parentId ->
            familyRelationDao.getChildrenRelations(parentId).forEach { childRel ->
                if (childRel.pigeonId != pigeonId) {
                    getBrief(childRel.pigeonId)?.let { siblings.add(it) }
                }
            }
        }
        return siblings.toList()
    }

    override suspend fun updateParents(
        pigeonId: String,
        fatherId: String?,
        motherId: String?
    ): Result<Unit> {
        return try {
            val existing = familyRelationDao.getByPigeonId(pigeonId)
            if (existing == null) {
                familyRelationDao.insert(
                    com.pigeonnest.data.local.entity.FamilyRelationEntity(
                        pigeonId = pigeonId,
                        fatherId = fatherId,
                        motherId = motherId
                    )
                )
            } else {
                familyRelationDao.update(
                    existing.copy(fatherId = fatherId, motherId = motherId)
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMate(pigeonId: String, mateId: String?): Result<Unit> {
        return try {
            // 1. 清除该鸽子之前的配偶关系（双向）
            val existing = familyRelationDao.getByPigeonId(pigeonId)
            existing?.mateId?.let { oldMateId ->
                val oldMateRelation = familyRelationDao.getByPigeonId(oldMateId)
                if (oldMateRelation != null) {
                    familyRelationDao.update(oldMateRelation.copy(mateId = null))
                }
            }

            // 2. 更新当前鸽子的配偶
            if (existing == null) {
                familyRelationDao.insert(
                    com.pigeonnest.data.local.entity.FamilyRelationEntity(
                        pigeonId = pigeonId,
                        mateId = mateId
                    )
                )
            } else {
                familyRelationDao.update(existing.copy(mateId = mateId))
            }

            // 3. 双向更新：设置对方配偶为当前鸽子
            mateId?.let { newMateId ->
                val mateRelation = familyRelationDao.getByPigeonId(newMateId)
                if (mateRelation == null) {
                    familyRelationDao.insert(
                        com.pigeonnest.data.local.entity.FamilyRelationEntity(
                            pigeonId = newMateId,
                            mateId = pigeonId
                        )
                    )
                } else {
                    familyRelationDao.update(mateRelation.copy(mateId = pigeonId))
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGraphData(centerPigeonId: String, depth: Int): GraphData {
        val visited = mutableSetOf<String>()
        val rootNode = buildNode(centerPigeonId, 0, depth, visited) ?: GraphNode(
            pigeonId = centerPigeonId,
            pigeonBrief = getBrief(centerPigeonId)
                ?: PigeonBrief(centerPigeonId, "", "", Gender.UNKNOWN, null)
        )
        val allNodes = mutableListOf<GraphNode>()
        val edges = mutableListOf<GraphEdge>()
        collectNodesAndEdges(rootNode, allNodes, edges)
        return GraphData(rootNode = rootNode, allNodes = allNodes, edges = edges)
    }

    private suspend fun buildNode(
        pigeonId: String,
        currentDepth: Int,
        maxDepth: Int,
        visited: MutableSet<String>
    ): GraphNode? {
        if (!visited.add(pigeonId)) return null
        val entity = pigeonDao.getById(pigeonId) ?: return null
        android.util.Log.d("FamilyRepo", "buildNode: pigeonId=$pigeonId depth=$currentDepth name=${entity.name}")
        val node = GraphNode(
            pigeonId = pigeonId,
            pigeonBrief = entity.toBrief()
        )
        if (maxDepth > 0 && currentDepth >= maxDepth) return node

        val relation = familyRelationDao.getByPigeonId(pigeonId)
        android.util.Log.d("FamilyRepo", "  Node relation: fatherId=${relation?.fatherId} motherId=${relation?.motherId} mateId=${relation?.mateId}")
        relation?.fatherId?.let { fid ->
            buildNode(fid, currentDepth + 1, maxDepth, visited)?.let { fatherNode ->
                node.father = fatherNode
                fatherNode.children.add(node)
            }
        }
        relation?.motherId?.let { mid ->
            buildNode(mid, currentDepth + 1, maxDepth, visited)?.let { motherNode ->
                node.mother = motherNode
                motherNode.children.add(node)
            }
        }
        relation?.mateId?.let { mateId ->
            buildNode(mateId, currentDepth, maxDepth, visited)?.let { mateNode ->
                node.mate = mateNode
                mateNode.mate = node
            }
        }
        val childRelations = familyRelationDao.getChildrenRelations(pigeonId)
        android.util.Log.d("FamilyRepo", "  childRelations count=${childRelations.size} ids=${childRelations.map { it.pigeonId }}")
        childRelations.forEach { childRel ->
            buildNode(childRel.pigeonId, currentDepth + 1, maxDepth, visited)?.let { childNode ->
                node.children.add(childNode)
                if (pigeonId == childRel.fatherId) childNode.father = node
                if (pigeonId == childRel.motherId) childNode.mother = node
            }
        }
        return node
    }

    private fun collectNodesAndEdges(
        node: GraphNode,
        allNodes: MutableList<GraphNode>,
        edges: MutableList<GraphEdge>
    ) {
        if (node in allNodes) return
        allNodes.add(node)
        node.children.forEach { child ->
            edges.add(GraphEdge(node, child, EdgeType.PARENT_CHILD))
            collectNodesAndEdges(child, allNodes, edges)
        }
        node.mate?.let { mate ->
            if (mate !in allNodes) {
                edges.add(GraphEdge(node, mate, EdgeType.MATE))
                collectNodesAndEdges(mate, allNodes, edges)
            }
        }
        node.father?.let { collectNodesAndEdges(it, allNodes, edges) }
        node.mother?.let { collectNodesAndEdges(it, allNodes, edges) }
    }

    private suspend fun getBrief(pigeonId: String): PigeonBrief? {
        return pigeonDao.getById(pigeonId)?.toBrief()
    }

    private fun PigeonEntity.toBrief(): PigeonBrief {
        return PigeonBrief(
            id = id,
            ringNumber = ringNumber,
            name = name,
            gender = Gender.fromCode(gender),
            photoPath = photoPath
        )
    }
}
