package com.nayya.myktor.domain

import kotlinx.serialization.Serializable

@Serializable
data class OrderEntity(
    val id: Int? = null,
    val orderDate: String, // Дата в виде строки
    val counterpartyId: Int,
    val items: List<OrderItemEntity> = emptyList(),
)
