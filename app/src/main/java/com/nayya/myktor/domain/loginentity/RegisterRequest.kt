package com.nayya.myktor.domain.loginentity

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String = "CUSTOMER"
)
