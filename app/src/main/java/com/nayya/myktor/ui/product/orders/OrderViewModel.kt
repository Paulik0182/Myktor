package com.nayya.myktor.ui.product.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import com.nayya.myktor.data.RetrofitInstance

class OrderViewModel : ViewModel() {
    val orders = MutableLiveData<List<Int>>()

    fun fetchOrders() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getOrders()
                orders.postValue(response)
            } catch (e: Exception) {
                orders.postValue(emptyList()) // В случае ошибки отправляем пустой список
            }
        }
    }
}
