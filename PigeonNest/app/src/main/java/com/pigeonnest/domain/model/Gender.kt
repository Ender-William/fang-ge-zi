package com.pigeonnest.domain.model

enum class Gender(val code: Int, val displayName: String) {
    UNKNOWN(0, "未知"),
    MALE(1, "雄"),
    FEMALE(2, "雌");

    companion object {
        fun fromCode(code: Int): Gender = entries.find { it.code == code } ?: UNKNOWN
    }
}
