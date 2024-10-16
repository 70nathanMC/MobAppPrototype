package com.example.mobappprototype.model

data class MeetingData (
    val id: String = "", // Meeting ID
    val subject: String = "",
    val branch: String = "",
    val day: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val slots: Int = 0,
    val slotsRemaining: Int = 0,
    val participants: List<String> = emptyList(),
    val tutorId: String = "",
    )