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

                // Получаем контрагента
                val counterparty = RetrofitInstance.api.getCounterpartyById(order.counterpartyId)

                // Получаем названия продуктов
                val itemsWithNames = order.items.map { item ->
                    val product = RetrofitInstance.api.getProductById(item.productId)
                    item.copy(
                        productName = product.name,   // Добавляем имя продукта
                        supplierName = product.name   // Если нужно имя поставщика, меняем тут
                    )
                }

                // Формируем объект с именами вместо ID
                val updatedOrder = order.copy(
                    counterpartyName = counterparty.name,  // Добавляем имя контрагента
                    items = itemsWithNames
                )

                onSuccess(updatedOrder)
            } catch (e: Exception) {
                onError()
            }
        }
    }

    fun fetchOrderDetails2(
        orderId: Int,
        onSuccess: (OrderEntity) -> Unit,
        onError: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getOrderDetails(orderId)
                onSuccess(response)
            } catch (e: Exception) {
                onError()
            }
        }
    }
}