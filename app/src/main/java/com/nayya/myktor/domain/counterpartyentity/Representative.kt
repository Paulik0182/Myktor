package com.nayya.myktor.domain.counterpartyentity

import kotlinx.serialization.Serializable

@Serializable
data class Representative(
    val id: Long? = null,
    val counterpartyId: Long,
    val fullName: String,
    val position: Int,
    val contacts: List<CounterpartyContact> = emptyList(),
    val contactsIds: List<Long>? = emptyList(),
) : java.io.Serializable