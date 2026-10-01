package org.mpp.dbuild

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform