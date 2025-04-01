package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class MeasurementUnitList(
    val id: Long? = null,
    val name: String,
    val abbreviation: String,
    val translations: List<MeasurementUnitTranslation> = emptyList()
)