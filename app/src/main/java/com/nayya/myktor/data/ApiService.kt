package com.nayya.myktor.data

import com.nayya.myktor.domain.CounterpartyEntity
import com.nayya.myktor.domain.OrderEntity
import com.nayya.myktor.domain.ProductEntity
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
    suspend fun getProducts(): List<ProductEntity>

    // Удаление продукта
    @DELETE("/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int)

    // Добавление нового продукта
    @POST("/products")
    suspend fun addProduct(@Body productEntity: ProductEntity)

    // Обновление данных о продукте
    @PUT("/products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body productEntity: ProductEntity)

    // Получение списка поставщиков
    @GET("/counterparties")
    suspend fun getSuppliers(): List<CounterpartyEntity>

    // Удаление поставщика
    @DELETE("/counterparties/{id}")
    suspend fun deleteSupplier(@Path("id") id: Int)

    // Добавление нового поставщика
    @POST("/counterparties")
    suspend fun addSupplier(@Body counterpartyEntity: CounterpartyEntity)

    // Обновление данных поставщика
    @PUT("/counterparties/{id}")
    suspend fun updateSupplier(@Path("id") id: Int, @Body counterpartyEntity: CounterpartyEntity)

    // Получение списка заказов
    @GET("/orders")
    suspend fun getOrders(): List<OrderEntity>

    @GET("/orders/{id}")
    suspend fun getOrderDetails(@Path("id") id: Int): OrderEntity

    @POST("/orders")
    suspend fun createOrder(@Body order: OrderEntity)

    @PUT("/orders/{id}")
    suspend fun updateOrder(@Path("id") id: Int, @Body order: OrderEntity)

    @DELETE("/orders/{id}")
    suspend fun deleteOrder(@Path("id") id: Int)
}
