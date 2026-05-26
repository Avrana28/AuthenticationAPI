package com.auth.utils

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val token: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String
)

@Serializable
data class MessageResponse(
    val message: String,
    val userId: String? = null
)