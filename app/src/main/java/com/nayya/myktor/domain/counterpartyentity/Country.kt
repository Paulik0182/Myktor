package com.nayya.myktor.domain.counterpartyentity

import kotlinx.serialization.Serializable

@Serializable
data class Country(
    val id: Long? = null,
    val name: String,
    val phoneCode: String,
    val isoCode: String,
    val translations: List<CountryTranslation> = emptyList(),
    val city: List<City>? = emptyList(),
    val cityIds: List<Long>? = emptyList(),
) : java.io.Serializable