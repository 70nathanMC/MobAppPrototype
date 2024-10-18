package com.example.mobappprototype

data class ChatState (
    val isEnteringToken: Boolean = true,
    val remoteToken: String = "",
    val messageText: String = "",

)