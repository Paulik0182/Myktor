package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class ProductImage(
    val id: Long? = null,
    val productId: Long,
    val imageUrl: String, // путь к файлу (локальный или полный URL)
    val position: Int?
) : java.io.Serializable