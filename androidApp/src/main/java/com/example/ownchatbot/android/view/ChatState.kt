package com.example.ownchatbot.android.view

import android.graphics.Bitmap
import com.example.ownchatbot.android.data.Chat

data class ChatState(
    val chatList: MutableList<Chat> = mutableListOf(),
    val prompt: String = "",
    val bitmap: Bitmap? = null) {

    companion object {
        fun fromString(value: String?): ChatState {

            return ChatState()
        }
    }
}
