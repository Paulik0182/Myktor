package com.nayya.myktor.domain

import kotlinx.serialization.Serializable

@Serializable
data class WarehouseLocationEntity(
    val id: Int,
    val counterpartyId: Int,
    val locationCode: String,
)
