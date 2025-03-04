package com.nayya.myktor.domain

import kotlinx.serialization.Serializable

@Serializable
data class ProductImageEntity(
    val id: Int,
    val productId: Int,
    val imageBase64: String, // Изображение в base64
)
