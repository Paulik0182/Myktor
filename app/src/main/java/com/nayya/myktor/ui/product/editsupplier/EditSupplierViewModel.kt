package com.nayya.myktor.ui.product.editsupplier

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.Counterparty
import com.nayya.myktor.ui.product.suppliers.SupplierViewModel
import kotlinx.coroutines.launch

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
                val counterparty = Counterparty(
                    id = id ?: 0, // ID = 0, так как сервер его игнорирует при создании
                    name = name,
                    type = type,
                    isSupplier = type.lowercase() == "поставщик",
                    productCount = 0
                )

                Log.d("API", "Отправка запроса: $counterparty") // Логируем данные перед отправкой

                if (id == null) {
                    RetrofitInstance.api.addSupplier(counterparty) // Создаем новый объект
                    Log.d("API", "Поставщик добавлен")
                } else {
                    RetrofitInstance.api.updateSupplier(id, counterparty) // Обновляем существующий
                    Log.d("API", "Поставщик обновлен")
                }

                supplierViewModel.fetchSuppliers() // Перезагружаем список после сохранения
                onSuccess()
            } catch (e: Exception) {
                Log.e("API", "Ошибка при сохранении поставщика", e)
                onError(e)
            }
        }
    }
}
