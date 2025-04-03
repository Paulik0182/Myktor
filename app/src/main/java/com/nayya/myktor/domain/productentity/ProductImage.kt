package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class ProductImage(
    val id: Long? = null,
    val productId: Long,
    val imageBase64: String // Изображение в base64
) : java.io.Serializable