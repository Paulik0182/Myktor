package com.nayya.myktor.domain.counterpartyentity

import kotlinx.serialization.Serializable

@Serializable
data class CounterpartyContact(
    val id: Long? = null,
    val counterpartyId: Long?,
    val counterpartyName: String?,
    val contactType: String,
    val contactValue: String,
    val countryCodeId: Long?,
    val countryName: String? = null,
    val countryPhoneCode: String? = null,
    val countryIsoCode: String? = null,
    val representativeId: Long?,
    val representativeName: String?,
) : java.io.Serializable