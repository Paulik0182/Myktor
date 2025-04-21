package com.nayya.myktor.domain.counterpartyentity

import kotlinx.serialization.Serializable

@Serializable
data class CityTranslation(
    val id: Long? = null,
    val cityId: Long,
    val languageCode: String,
    val name: String
) : java.io.Serializable
