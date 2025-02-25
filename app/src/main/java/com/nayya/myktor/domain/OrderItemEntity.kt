package com.nayya.myktor.domain

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemEntity(
    val id: Int? = null,
    val orderId: Int,
    val productId: Int,
    val supplierId: Int,
    val quantity: Int,
)
