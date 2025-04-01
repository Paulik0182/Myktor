package com.nayya.myktor.domain.productentity

import com.nayya.myktor.domain.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Product(
    val id: Long? = null,
    val name: String,
    val description: String,
    /**
     * Kotlin kotlinx.serialization не поддерживает BigDecimal по умолчанию, поэтому добавлен кастомный сериализатор
     * Это предотвратит проблемы с округлением.
     * BigDecimalSerializer - корректно сериализует/десериализует.
     */
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal, // Используем BigDecimal для работы с деньгами
    val hasSuppliers: Boolean, // Признак наличия поставщика

    val supplierCount: Int, // Количество поставщиков
    val totalStockQuantity: Int, // количество товара на всех складах
    val minStockQuantity: Int, // неснижаемый остаток

    val isDemanded: Boolean, // флаг востребованности
    val measurementUnitId: Long, // ID единицы измерения
    val measurementUnitList: MeasurementUnitList?, // Связь с единицей измерения
    val measurementUnit: String?,
    val measurementUnitAbbreviation: String?,

    val productCodes: List<ProductCode> = emptyList(), // список кодов товара (штрих-коды, QR-коды).
    val productLinks: List<ProductLink> = emptyList(), // список интернет-ссылок.
    val productImages: List<ProductImage> = emptyList(), // для картинок
    val productCounterparties: List<ProductCounterparty> = emptyList(), // Информация о складах, контрагентах
    val productSuppliers: List<ProductSupplier> = emptyList(), // Информация о Поставщике, контрагентах
    val measurementUnits: List<MeasurementUnitList> = emptyList(), // Еденицы измерения
    val productOrderItem: List<ProductOrderItem> = emptyList(), // Информация о Заказах товара
    val categories: List<CategoryEntity> = emptyList(), // Категории - многоязычность
    val subcategoryIds: List<Long> = emptyList(), // список подкатегорий (для фильтра)
    val categoryIds: List<Long> = emptyList(), // список категорий (для фильтра)
) : java.io.Serializable