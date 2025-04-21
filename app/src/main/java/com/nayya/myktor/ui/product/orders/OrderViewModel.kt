package com.nayya.myktor.ui.product.orders

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.counterpartyentity.OrderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderViewModel : ViewModel() {
    val orders = MutableLiveData<List<OrderEntity>>()

    fun fetchOrders() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getOrders()
                }
                orders.value = emptyList()
                orders.postValue(response)
            } catch (e: Exception) {
                orders.postValue(emptyList()) // В случае ошибки отправляем пустой список
            }
        }
    }

    fun orderDetails(
        orderId: Long,
    ) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.deleteOrder(orderId)
                fetchOrders()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Order", "Ошибка: ${e.localizedMessage}")
            }
        }
    }
}
