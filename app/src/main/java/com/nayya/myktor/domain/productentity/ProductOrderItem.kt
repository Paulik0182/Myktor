package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class ProductOrderItem(
    val id: Long? = null,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val measurementUnitId: Long,
    val measurementUnitList: MeasurementUnitList?,
    val measurementUnit: String?,
    val measurementUnitAbbreviation: String?
) : java.io.Serializable