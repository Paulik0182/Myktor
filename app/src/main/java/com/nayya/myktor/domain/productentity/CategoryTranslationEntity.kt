package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class CategoryTranslationEntity(
    val id: Long? = null,
    val categoryId: Long,
    val languageCode: String, // код перевода. Например: ru, pl, en
    val name: String // сам перевод категории
)