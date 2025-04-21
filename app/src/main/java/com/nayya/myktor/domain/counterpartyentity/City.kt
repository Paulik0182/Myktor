package com.nayya.myktor.domain.counterpartyentity

import kotlinx.serialization.Serializable

@Serializable
data class City(
    val id: Long? = null,
    val name: String,
    val translations: List<CityTranslation> = emptyList(),
    val country: List<Country>? = emptyList(),
    val countryId: Long?,
) : java.io.Serializable