package com.example.mobappprototype.model

import com.google.firebase.Timestamp
import java.util.Date

data class MeetingData (
    var id: String = "",
    val subject: String = "",
    val branch: String = "",
    val day: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val slots: Int = 0,
    val slotsRemaining: Int = 0,
    val participants: List<String> = emptyList(),
    val tutorId: String = "",
    val startTimeTimestamp: Timestamp = Timestamp.now(),
    val endTimeTimestamp: Timestamp = Timestamp.now(),
    val date: Timestamp = Timestamp.now(),
    val tutorFullName: String = "",
    val tutorProfilePic: String = "",
    val meetingSite: String = "",
    var upcomingDate: Date = Date()
    )