package com.nayya.myktor.domain

import kotlinx.serialization.Serializable

@Serializable
data class ProductLinkEntity(
    val id: Int,
    val productId: Int,
    val counterpartyId: Int,
    val url: String,
)
