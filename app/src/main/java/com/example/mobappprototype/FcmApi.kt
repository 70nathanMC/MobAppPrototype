package com.example.mobappprototype

import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApi {

    @POST("fcm/send")
    suspend fun sendMessage(
        @Body body: SendMessageDto

    )

    @POST("/broadcast")
    suspend fun broadcast(
        @Body body: SendMessageDto
    )
}