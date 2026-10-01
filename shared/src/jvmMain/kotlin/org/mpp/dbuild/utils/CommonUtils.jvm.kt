package org.mpp.dbuild.utils
import java.io.File

actual fun readFile(filename: String): List<String> {
    return File(filename).readLines()
}

actual fun formatString(format: String, args: Array<String>): String {
    return String.format(format, *args)
}