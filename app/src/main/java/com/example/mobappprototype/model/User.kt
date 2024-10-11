package com.example.mobappprototype.model

data class User(
    val firstName: String = "",
    val lastName: String = "",
    val program: String = "",
    val role: String = "",
    val bio: String = "",
    val subjects: List<String> = emptyList(),
    val userUID: String = "",
    val profilePic: String = ""
)