package se.nullable.flickboard.util

import android.util.JsonReader
import android.util.JsonToken

inline fun <T> JsonReader.orNull(next: JsonReader.() -> T): T? = when (peek()) {
    JsonToken.NULL -> null.also { nextNull() }
    else -> next()
}
