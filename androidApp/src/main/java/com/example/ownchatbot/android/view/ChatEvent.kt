package com.example.ownchatbot.android.view

import android.graphics.Bitmap
import com.google.ai.client.generativeai.Chat

sealed class ChatEvent{
    data class UpdatePrompt(val newPrompt: String) : ChatEvent()
    data object ClearPrompt : ChatEvent()
    data class SendPrompt(
        val prompt: String,
        val bitmap: Bitmap?
    ) : ChatEvent()
}
