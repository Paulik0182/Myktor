package com.nayya.myktor.ui.product.suppliers

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.CounterpartyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SupplierViewModel : ViewModel() {
    val suppliers = MutableLiveData<List<CounterpartyEntity>>()

    fun fetchSuppliers() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getSuppliers()
                }
                suppliers.value = emptyList() // Принудительно очищаем, чтобы LiveData обновилась
                suppliers.postValue(response) // Перезаписываем данные, вызывая `observe`
            } catch (e: Exception) {
                suppliers.postValue(emptyList())
            }
        }
    }

    fun deleteSupplier(id: Int) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.deleteSupplier(id)
                fetchSuppliers() // Перезагружаем список после удаления
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Supplier", "Ошибка: ${e.localizedMessage}")
            }
        }
    }
}
