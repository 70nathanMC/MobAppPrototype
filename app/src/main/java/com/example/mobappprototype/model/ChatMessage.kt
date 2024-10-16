package com.example.mobappprototype.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChatMessage(
    val senderUID: String = "",
    val content: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)
