package com.nayya.myktor.domain

import kotlinx.serialization.Serializable

@Serializable
data class ProductCodeEntity(
    val productId: Int,
    val code: String,
)
