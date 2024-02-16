package se.nullable.flickboard.util

import android.icu.lang.UCharacter

fun String.singleCodePointOrNull(): Int? =
    when {
        UCharacter.codePointCount(this, 0, this.length) == 1 -> UCharacter.codePointAt(this, 0)
        else -> null
    }

fun String.dropInitialSpace(): String? =
    when {
        this.getOrNull(0) == ' ' -> this.drop(1)
        else -> null
    }