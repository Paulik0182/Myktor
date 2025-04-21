package com.nayya.myktor.domain.counterpartyentity

import com.nayya.myktor.domain.productentity.LinkEntity
import kotlinx.serialization.Serializable

@Serializable
data class CounterpartyEntity(
    val id: Long? = null,
    val shortName: String, // Краткое название или псевдоним

    val companyName: String?,
    val type: String,
    val isSupplierOld: Boolean,
    val productCountOld: Int,

    val isSupplier: Boolean,
    val isWarehouse: Boolean,
    val isCustomer: Boolean,
    val isLegalEntity: Boolean,  // Юридическое лицо (true) или физическое (false)

    val imagePath: String?, // Путь к логотипу/аватарке

    val nip: String?,
    val krs: String?,

    val firstName: String?, // Только для физ. лиц
    val lastName: String?, // Только для физ. лиц

    val counterpartyRepresentatives: List<Representative> = emptyList(),
    val representativesIds: List<Long>? = emptyList(),
    val representativesName: String?,
    val representativesContact: List<String>? = emptyList(), // Должна собираться строчка контакта (пример: Тел. +48 000-000-000, E-mail: pop@hot.pl)

    val counterpartyContacts: List<CounterpartyContact> = emptyList(),
    val contactIds: List<Long>? = emptyList(),
    val counterpartyContact: List<String>? = emptyList(), // Должна собираться строчка контакта (пример: Тел. +48 000-000-000, E-mail: pop@hot.pl)

    val counterpartyBankAccounts: List<CounterpartyBankAccount> = emptyList(),
    val bankAccountIds: List<Long>? = emptyList(),
    val bankAccountInformation: List<String>? = emptyList(), // должна быть строка из названия банка, номера счета и обозначение валюты счета

    val counterpartyAddresses: List<CounterpartyAddresse> = emptyList(),
    val addressesIds: List<Long> = emptyList(),
    val addressesInformation: List<String>? = emptyList(), // должна быть строка из Адреса (например: Страна, город, улица, дом, если есть кв. или локация)

    val orders: List<OrderEntity>? = emptyList(),
    val orderIds: List<Long>? = emptyList(),

    val productCounterparties: List<ProductCounterparty> = emptyList(),

    val counterpartyLinks: List<LinkEntity>? = emptyList(),
    val counterpartyLinkIds: List<Long>? = emptyList(),

    val productSuppliers: List<ProductSupplier>? = emptyList(),
    val productSupplierIds: List<Long>? = emptyList(),

    ) : java.io.Serializable