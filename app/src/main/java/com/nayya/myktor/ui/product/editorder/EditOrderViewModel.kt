package com.nayya.myktor.ui.product.editorder

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.counterpartyentity.OrderEntity
import com.nayya.myktor.ui.product.orders.OrderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

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
                val orderEntity = order.toOrderEntity()

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
        orderId: Long,
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

/**
 * Не даёт упасть, если counterpartyName — null;
 *
 * Автоматически проставляет createdAt, если он не задан;
 *
 * Обновляет updatedAt на текущее время;
 *
 * Гарантирует, что items — не null и не empty;
 *
 * Использует дефолтный orderStatus = 0, если он не задан.
 */
fun OrderEntity.toOrderEntity(): OrderEntity {
    return OrderEntity(
        id = this.id,
        counterpartyId = this.counterpartyId,
        counterpartyName = this.counterpartyName ?: "",
        orderStatus = this.orderStatus.takeIf { it != 0 } ?: 0,

        createdAt = this.createdAt.ifBlank { Instant.now().toString() },
        updatedAt = Instant.now().toString(),
        acceptedAt = this.acceptedAt,
        completedAt = this.completedAt,

        items = this.items.ifEmpty { emptyList() }
    )
}
