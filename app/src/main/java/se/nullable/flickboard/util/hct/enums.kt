inline fun <reified T : Enum<T>> tryEnumValueOf(name: String): T? = try {
    enumValueOf<T>(name)
} catch (_: IllegalArgumentException) {
    // thrown by enumValueOf when a key is not found
    null
}