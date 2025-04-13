package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyResponse(
    val id: Long,
    val code: String,
    val symbol: String,
    val name: String
) : java.io.Serializable
