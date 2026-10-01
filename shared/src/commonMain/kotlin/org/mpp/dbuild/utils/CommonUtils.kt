package org.mpp.dbuild.utils

expect fun readFile(filename: String): List<String>

expect fun formatString(format: String, args: Array<String>): String
