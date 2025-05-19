package com.nayya.myktor.domain.loginentity

import kotlinx.serialization.Serializable

@Serializable
data class ResetRequest(val email: String)
