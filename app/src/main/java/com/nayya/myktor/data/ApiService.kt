package com.nayya.myktor.data

import com.nayya.myktor.domain.Counterparty
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Определяем API-интерфейс с Retrofit
 */
interface ApiService {
    /**
     * suspend fun – Используем Kotlin Coroutines для асинхронных запросов.
     */

    // Получение списка товаров
    @GET("/products")
    suspend fun getProducts(): List<String>

    // Получение списка поставщиков
    @GET("/counterparties")
    suspend fun getSuppliers(): List<Counterparty>

    // Удаление поставщика
    @DELETE("/counterparties/{id}")
    suspend fun deleteSupplier(@Path("id") id: Int)

    // Добавление нового поставщика
    @POST("/counterparties")
    suspend fun addSupplier(@Body counterparty: Counterparty)

    // Обновление данных поставщика
    @PUT("/counterparties/{id}")
    suspend fun updateSupplier(@Path("id") id: Int, @Body counterparty: Counterparty)

    // Получение списка заказов
    @GET("/orders")
    suspend fun getOrders(): List<Int>
}
