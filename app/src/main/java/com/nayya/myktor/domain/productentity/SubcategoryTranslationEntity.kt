package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class SubcategoryTranslationEntity(
    val id: Long? = null,
    val subcategoryId: Long,
    val languageCode: String, // код перевода. Например: ru, pl, en
    val name: String // сам перевод категории
)