package com.nayya.myktor.ui.product.counterparties

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CounterpartyViewModel : ViewModel() {
    val counterparties = MutableLiveData<List<CounterpartyEntity>>()

    fun fetchCounterparties() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getCounterparties()
                }

                Log.d("SUPPLIERS", "✅ ЛОГ 1 Получено ${response.size} поставщиков")

                counterparties.value = emptyList()
                counterparties.postValue(response)
            } catch (e: Exception) {
                Log.e("SUPPLIERS", "Ошибка при получении: ${e.localizedMessage}", e)
                counterparties.postValue(emptyList())
            }
        }
    }

    fun deleteCounterparty(id: Long) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.deleteCounterparty(id)
                fetchCounterparties() // Перезагружаем список после удаления
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Supplier", "Ошибка: ${e.localizedMessage}")
            }
        }
    }
}
