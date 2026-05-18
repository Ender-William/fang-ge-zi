package com.pigeonnest.domain.model

data class FamilyGroup(
    val rootPigeonId: String,
    val rootPigeonName: String,
    val matePigeonName: String? = null,
    val rootColor: String? = null,
    val mateColor: String? = null,
    val totalCount: Int = 0,
    val maleCount: Int = 0,
    val femaleCount: Int = 0
) {
    val displayName: String
        get() = if (matePigeonName != null) {
            "${rootPigeonName} & ${matePigeonName}"
        } else {
            rootPigeonName
        }

    val colorDisplay: String
        get() = when {
            rootColor != null && mateColor != null -> "$rootColor / $mateColor"
            rootColor != null -> rootColor
            mateColor != null -> mateColor
            else -> "未记录"
        }
}
