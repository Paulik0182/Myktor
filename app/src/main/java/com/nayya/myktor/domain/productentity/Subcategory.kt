package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class Subcategory(
    val id: Long? = null,
    val name: String, // локализованное имя (имя по умолчанию)
    val imageUrl: String?,
    val categoryId: Long,
    val subcategoryTranslationEntities: List<SubcategoryTranslationEntity> = emptyList()
) : java.io.Serializable