package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class ProductCode(
    val productId: Long,
    val codId: Long?,
    val codeName: String,
    val code: List<Code> = emptyList()
) : java.io.Serializable