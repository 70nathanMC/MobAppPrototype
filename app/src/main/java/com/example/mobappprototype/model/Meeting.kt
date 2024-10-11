package com.example.mobappprototype.model

import com.google.firebase.firestore.PropertyName

data class Meeting(
    var day: String,
    var subject: String,
    @get:PropertyName("time_end") @set:PropertyName("time_end") var timeEnd: String,
    @get:PropertyName("time_start") @set:PropertyName("time_start") var timeStart: String,
    var topic: String,
    var slots: Int
)
