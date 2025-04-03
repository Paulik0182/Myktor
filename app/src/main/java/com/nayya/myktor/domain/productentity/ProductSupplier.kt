package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class ProductSupplier(
    val id: Long? = null,

    val productId: Long,
    val productName: String,

    val supplierId: Long,
    val supplierName: String
) : java.io.Serializable