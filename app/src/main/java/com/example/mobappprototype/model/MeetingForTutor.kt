package com.example.mobappprototype.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MeetingForTutor(
    val id: String = "", // Add this line
    val subject: String = "",
    val branch: String = "",
    val day: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val slots: Int = 0,
    val slotsRemaining: Int = 0
) : Parcelable
