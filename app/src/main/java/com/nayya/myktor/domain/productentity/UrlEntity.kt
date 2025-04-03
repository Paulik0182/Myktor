package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class UrlEntity(
    val id: Long? = null,
    val url: String
) : java.io.Serializable