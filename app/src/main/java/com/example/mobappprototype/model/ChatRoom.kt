package com.example.mobappprototype.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChatRoom(
    @DocumentId
    val documentId: String = "",
    val meetingID: String = "",
    val participants: List<String> = listOf(),
    var lastMessage: LastMessage? = null,
    var unreadCount: Long = 0 // I just added this recently to prepare for the unreadCount feature
)

data class LastMessage(
    val content: String = "",
    val senderUID: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)