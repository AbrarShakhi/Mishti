package com.abrarshakhi.mishti

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
