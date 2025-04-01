package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class ProductLink(
    val id: Long? = null,
    val productId: Long?,
    val counterpartyId: Long?,
    val urlId: Long?,
    val urlName: String?,
    val url: List<UrlEntity> = emptyList()
)