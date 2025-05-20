package com.nayya.myktor.domain.loginentity

import kotlinx.serialization.Serializable

@Serializable
data class LoginErrorResponse(
    val code: String,
    val message: String
)
