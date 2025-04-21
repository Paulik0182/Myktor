package com.nayya.myktor.domain.counterpartyentity

import kotlinx.serialization.Serializable

@Serializable
data class ProductSupplier(
    val id: Long? = null,

    val productId: Long,
    val productName: String,

    val counterpartyId: Long,
    val counterpartyName: String?
) : java.io.Serializable