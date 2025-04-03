package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class Code(
    val id: Long? = null,
    val code: String
) : java.io.Serializable