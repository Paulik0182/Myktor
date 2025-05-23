package com.nayya.myktor.domain.loginentity

import com.nayya.myktor.ui.login.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val role: UserRole = UserRole.CUSTOMER
)
