package com.example.ownchatbot

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform