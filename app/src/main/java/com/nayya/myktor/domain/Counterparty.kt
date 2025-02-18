package com.nayya.myktor.domain

import kotlinx.serialization.Serializable

@Serializable
data class Counterparty(
    val id: Int,
    val name: String,
    val type: String,
    val isSupplier: Boolean,
    val productCount: Int
)
