package com.rose.clubs.data

enum class Role(val value: Int) {
    CAPTAIN(1), SUB_CAPTAIN(2), MEMBER(3);

    companion object {
        fun fromValue(value: Int): Role = when (value) {
            1 -> CAPTAIN
            2 -> SUB_CAPTAIN
            3 -> MEMBER
            else -> throw IllegalArgumentException("Invalid value $value")
        }
    }
}