package com.nayya.myktor.domain.loginentity

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val deviceInfo: String? = null
)
