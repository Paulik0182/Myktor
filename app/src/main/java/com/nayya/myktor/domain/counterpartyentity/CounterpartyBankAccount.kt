package com.nayya.myktor.domain.counterpartyentity

import com.nayya.myktor.domain.productentity.CurrencyResponse
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
    val currency: CurrencyResponse,
    val currencyId: Long,
) : java.io.Serializable