package com.example.mobappprototype.model

data class SearchHistoryData(
    val query: String = "",
    val timestamp: Long = 0L // Store timestamp as a Long
)