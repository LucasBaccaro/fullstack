package com.baccaro.lucas

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform