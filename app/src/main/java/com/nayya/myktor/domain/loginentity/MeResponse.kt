package com.nayya.myktor.domain.loginentity

import com.nayya.myktor.ui.login.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class MeResponse(
    val userId: Long,
    val role: UserRole,
    val counterpartyId: Long? = null
)
