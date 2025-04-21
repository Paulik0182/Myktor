package com.nayya.myktor.domain.counterpartyentity

import com.nayya.myktor.utils.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class OrderEntity(
    val id: Long? = null,
    val counterpartyId: Long,
    val counterpartyName: String?,
    val orderStatus: Int,

    val createdAt: String, // <--- временно вместо Instant
    val updatedAt: String? = null,
    val acceptedAt: String? = null,
    val completedAt: String? = null,

//    @Serializable(with = InstantSerializer::class)
//    val createdAt: Instant,
//
//    @Serializable(with = InstantSerializer::class)
//    val updatedAt: Instant? = null,
//
//    @Serializable(with = InstantSerializer::class)
//    val acceptedAt: Instant? = null,
//
//    @Serializable(with = InstantSerializer::class)
//    val completedAt: Instant? = null,

    val items: List<OrderItem> = emptyList(),
) : java.io.Serializable