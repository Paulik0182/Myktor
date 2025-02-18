package com.nayya.myktor.ui.product.suppliers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.Counterparty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupplierViewModel : ViewModel() {
    val suppliers = MutableLiveData<List<Counterparty>>()

    suspend fun fetchSuppliers(): List<Counterparty> {
        return withContext(Dispatchers.IO) { // Переключаем поток на фоновый
            try {
                val response = RetrofitInstance.api.getSuppliers()
                suppliers.postValue(response)
                response
            } catch (e: Exception) {
                suppliers.postValue(emptyList()) // В случае ошибки отправляем пустой список
                emptyList()
            }
        }
    }

    fun deleteSupplier(id: Int) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.deleteSupplier(id)
                fetchSuppliers() // Перезагружаем список после удаления
            } catch (e: Exception) {
                // Лог ошибки
            }
        }
    }
}
