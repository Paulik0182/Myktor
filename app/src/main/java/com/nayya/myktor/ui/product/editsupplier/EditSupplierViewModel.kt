package com.nayya.myktor.ui.product.editsupplier

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.CounterpartyEntity
import com.nayya.myktor.ui.product.suppliers.SupplierViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditSupplierViewModel(
    private val supplierViewModel: SupplierViewModel
) : ViewModel() {

    fun saveSupplier(
        id: Int?,
        name: String,
        type: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val counterpartyEntity = CounterpartyEntity(
                    id = id, // ID = 0, так как сервер его игнорирует при создании
                    name = name,
                    type = type,
                    isSupplier = type.lowercase() == "поставщик",
                    productCount = 0
                )

                Log.d(
                    "API",
                    "Отправка запроса: $counterpartyEntity"
                ) // Логируем данные перед отправкой

                if (id == null) {
                    RetrofitInstance.api.addSupplier(counterpartyEntity) // Создаем новый объект
                    Log.d("API", "Поставщик добавлен")
                } else {
                    RetrofitInstance.api.updateSupplier(
                        id,
                        counterpartyEntity
                    ) // Обновляем существующий
                    Log.d("API", "Поставщик обновлен")
                }

                withContext(Dispatchers.Main) {
                    supplierViewModel.fetchSuppliers() // Перезагружаем список после сохранения
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("API", "Ошибка при сохранении поставщика", e)
                onError(e)
            }
        }
    }
}
