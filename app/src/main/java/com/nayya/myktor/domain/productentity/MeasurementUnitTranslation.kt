package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class MeasurementUnitTranslation(
    val id: Long? = null,
    val measurementUnitId: Long,
    val languageCode: String,
    val name: String,
    val abbreviation: String?,
) : java.io.Serializable