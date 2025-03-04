package com.nayya.myktor.domain

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemEntity(
    val id: Int? = null,
    val orderId: Int,
    val productId: Int,
    val productName: String = "",  // <-- Добавляем название продукта
    val supplierId: Int,
    val supplierName: String? = null,  // <-- Если нужно название поставщика
    val quantity: Int,
)
