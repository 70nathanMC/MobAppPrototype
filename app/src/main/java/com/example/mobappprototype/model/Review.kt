package com.example.mobappprototype.model

data class Review(
    val reviewerName: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val reviewerProfilePic: String = ""
)