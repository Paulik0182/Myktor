package com.nayya.myktor.domain.counterpartyentity

import kotlinx.serialization.Serializable

@Serializable
data class CountryTranslation(
    val id: Long? = null,
    val countryId: Long,
    val languageCode: String,
    val name: String,
) : java.io.Serializable
