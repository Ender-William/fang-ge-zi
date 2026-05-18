package com.pigeonnest.domain.model

enum class PigeonStatus(val code: Int, val displayName: String) {
    ACTIVE(0, "在养"),
    SOLD(1, "已售"),
    DECEASED(2, "已故"),
    GIFTED(3, "赠送");

    companion object {
        fun fromCode(code: Int): PigeonStatus = entries.find { it.code == code } ?: ACTIVE
    }
}
