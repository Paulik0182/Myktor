package com.nayya.myktor.ui.product.editsupplier

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
import com.nayya.myktor.ui.product.counterparties.CounterpartyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditSupplierViewModel(
    private val counterpartyViewModel: CounterpartyViewModel
) : ViewModel() {

    fun saveSupplier(
        id: Long?,
        name: String,
        type: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val counterpartyEntity = CounterpartyEntity(
                    id = id, // ID может быть null для создания нового
                    shortName = name,
                    companyName = name, // Можно приравнять к name, если нет отдельного поля
                    type = type,
                    isSupplierOld = false,
                    productCountOld = 0,

                    isSupplier = type.lowercase() == "поставщик",
                    isWarehouse = false,
                    isCustomer = false,
                    isLegalEntity = true, // Можно выставить true по умолчанию

                    imagePath = null,
                    nip = null,
                    krs = null,
                    firstName = null,
                    lastName = null,

                    counterpartyRepresentatives = emptyList(),
                    representativesIds = emptyList(),
                    representativesName = null,
                    representativesContact = emptyList(),

                    counterpartyContacts = emptyList(),
                    contactIds = emptyList(),
                    counterpartyContact = emptyList(),

                    counterpartyBankAccounts = emptyList(),
                    bankAccountIds = emptyList(),
                    bankAccountInformation = emptyList(),

                    counterpartyAddresses = emptyList(),
                    addressesIds = emptyList(),
                    addressesInformation = emptyList(),

                    orders = emptyList(),
                    orderIds = emptyList(),

                    productCounterparties = emptyList(),

                    counterpartyLinks = emptyList(),
                    counterpartyLinkIds = emptyList(),

                    productSuppliers = emptyList(),
                    productSupplierIds = emptyList()
                )

                Log.d(
                    "API",
                    "Отправка запроса: $counterpartyEntity"
                ) // Логируем данные перед отправкой

                if (id == null) {
                    RetrofitInstance.api.addCounterparty(counterpartyEntity) // Создаем новый объект
                    Log.d("API", "Поставщик добавлен")
                } else {
                    RetrofitInstance.api.updateCounterparty(
                        id,
                        counterpartyEntity
                    ) // Обновляем существующий
                    Log.d("API", "Поставщик обновлен")
                }

                withContext(Dispatchers.Main) {
                    counterpartyViewModel.fetchCounterparties() // Перезагружаем список после сохранения
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("API", "Ошибка при сохранении поставщика", e)
                onError(e)
            }
        }
    }
}
