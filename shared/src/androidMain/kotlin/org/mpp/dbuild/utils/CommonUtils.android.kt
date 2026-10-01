package org.mpp.dbuild.utils

actual fun readFile(filename: String): List<String> {
    return listOf<String>() // not yet implemented: todo
}

actual fun formatString(format: String, args: Array<String>): String {
    return String.format(format, *args)
}
