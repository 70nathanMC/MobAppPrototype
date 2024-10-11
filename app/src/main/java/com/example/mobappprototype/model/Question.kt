package com.example.mobappprototype.model

data class Question(
    val questionText: String = "",
    val choices: List<String> = listOf(),
    val correctAnswer: String = ""
)
