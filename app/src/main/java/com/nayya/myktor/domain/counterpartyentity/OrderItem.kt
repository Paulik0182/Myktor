package com.nayya.myktor.domain.counterpartyentity

import com.nayya.myktor.domain.productentity.MeasurementUnitList
import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    val id: Long? = null,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val measurementUnitId: Long,
    val measurementUnitList: MeasurementUnitList?,
    val measurementUnit: String?,
    val measurementUnitAbbreviation: String?,
) : java.io.Serializable
