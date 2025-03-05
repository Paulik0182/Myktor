package com.nayya.myktor.domain

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class ProductEntity(
    val id: Int? = null,
    val name: String,
    val description: String,

    /**
     * Kotlin kotlinx.serialization не поддерживает BigDecimal по умолчанию, поэтому добавлен кастомный сериализатор
     * Это предотвратит проблемы с округлением.
     * BigDecimalSerializer - корректно сериализует/десериализует.
     */
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal, // Используем BigDecimal для работы с деньгами
    val hasSuppliers: Boolean,
    val supplierCount: Int,
    val stockQuantity: Int, // Количество товара на складе
    val minStockQuantity: Int, // Неснижаемый остаток
    val productCodes: List<ProductCodeEntity>, // Список кодов товара (штрих-коды, QR-коды)
    val isDemanded: Boolean, // Флаг востребованности
    val productLinks: List<ProductLinkEntity>, // Список интернет-ссылок
    val locations: List<WarehouseLocationEntity>, // Код места хранения товара
    val images: List<ProductImageEntity>, // Для картинок
    val categories: List<String>, // категория
    val subcategories: List<String>, // подкатегория
)
