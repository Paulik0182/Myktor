package com.nayya.myktor.domain.counterpartyentity

import com.nayya.myktor.domain.productentity.Currency
import kotlinx.serialization.Serializable

@Serializable
data class CounterpartyBankAccount(
    val id: Long? = null,
    val accountNumber: String?,
    val bankName: String,
    val swiftCode: String?,
    val code: String,
    val symbol: String?,
    val currencyName: String,
    val currency: Currency,
    val currencyId: Long,
) : java.io.Serializable