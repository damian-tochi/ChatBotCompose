package com.example.ownchatbot.android.view


import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ownchatbot.android.data.Chat
import com.example.ownchatbot.android.data.ChatData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date


class ChatViewModel : ViewModel() {

    private val _chatState = MutableStateFlow(ChatState())
    val chatState = _chatState.asStateFlow()

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.SendPrompt -> {
                if (event.prompt.isNotEmpty()) {

                    addPrompt(event.prompt, event.bitmap, Date())

                    if (event.bitmap != null) {
                        getResponseWithImage(event.prompt, event.bitmap)
                    } else {
                        getResponse(event.prompt)
                    }
                }
            }

            is ChatEvent.UpdatePrompt -> {
                _chatState.update {
                    it.copy(prompt = event.newPrompt)
                }

            }

            is ChatEvent.ClearPrompt -> {
                val mods = chatState.value
                mods.chatList.clear()
            }
        }
    }

    private fun addPrompt(prompt: String, bitmap: Bitmap?, time: Date) {
        _chatState.update {
            it.copy(
                chatList = it.chatList.toMutableList().apply {
                    add(0, Chat(prompt, bitmap, time, true))
                },
                prompt = "",
                bitmap = null
            )
        }

    }

    private fun getResponse(prompt: String) {
        viewModelScope.launch {
            val chat = ChatData.getResponse(prompt)
            _chatState.update {
                it.copy(
                    chatList = it.chatList.toMutableList().apply {
                        add(0, chat)
                    }
                )
            }
        }
    }

    private fun getResponseWithImage(prompt: String, bitmap: Bitmap) {
        viewModelScope.launch {
            val chat = ChatData.getResponseWithImage(prompt, bitmap)
            _chatState.update {
                it.copy(
                    chatList = it.chatList.toMutableList().apply {
                        add(0, chat)
                    }
                )
            }
        }
    }

}
