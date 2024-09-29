package com.example.mobappprototype.model

data class Tutor(
    val name: String,
    val profilePic: Int, // Resource ID of the drawable image
    val strengths: List<Subject>,
    val rating: Float
)