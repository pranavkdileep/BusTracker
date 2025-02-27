package com.pranavkd.bustracker.ChatLogic

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {
    private val _messageList = mutableStateListOf<Message>()
    val messageList: List<Message> get() = _messageList

    fun sendMessage(text: String) {
        _messageList.add(Message(text, "send", "12:00"))
        _messageList.add(Message("Hello! How can I help you?", "receive", "12:01"))
    }

    fun receiveMessage() {
        // Stub for receiving messages
    }
}