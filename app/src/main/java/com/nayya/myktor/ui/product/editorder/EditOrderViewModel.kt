package com.nayya.myktor.ui.product.editorder

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.OrderEntity
import com.nayya.myktor.ui.product.orders.OrderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditOrderViewModel(
    private val orderViewModel: OrderViewModel,
) : ViewModel() {

    fun saveOrder(
        order: OrderEntity,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val orderEntity = OrderEntity(
                    id = order.id,
                    orderDate = order.orderDate,
                    counterpartyId = order.counterpartyId,
                    items = order.items
                )

                Log.d("API", "Отправка запроса: ${order.id}, $order")

                if (order.id == null) {
                    RetrofitInstance.api.createOrder(orderEntity)
                } else {
                    RetrofitInstance.api.updateOrder(order.id, order)
                    Log.d("API", "Заказ обновлен")
                }

                withContext(Dispatchers.Main) {
                    orderViewModel.fetchOrders() // Обновляем список заказов после сохранения
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("API", "Ошибка при сохранении заказа", e)
                onError(e)
            }
        }
    }

    fun fetchOrderDetails(
        orderId: Int,
        onSuccess: (OrderEntity) -> Unit,
        onError: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val order = RetrofitInstance.api.getOrderDetails(orderId)
                onSuccess(order)
            } catch (e: Exception) {
                onError()
            }
        }
    }
}