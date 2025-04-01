package com.nayya.myktor.domain.productentity

import kotlinx.serialization.Serializable

@Serializable
data class CategoryEntity(
    val id: Long? = null,
    val name: String,
    val imageUrl: String?,
    val subcategories: List<Subcategory>? = null,
    val translations: List<CategoryTranslationEntity> = emptyList()
): java.io.Serializable