package com.nayya.myktor.data

import com.nayya.myktor.domain.CounterpartyEntity
import com.nayya.myktor.domain.OrderEntity
import com.nayya.myktor.domain.ProductEntity
import com.nayya.myktor.domain.productentity.CategoryEntity
import com.nayya.myktor.domain.productentity.Product
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Определяем API-интерфейс с Retrofit
 */
interface ApiService {
    /**
     * suspend fun – Используем Kotlin Coroutines для асинхронных запросов.
     */

    @GET("/categories")
    suspend fun getCategories(@Query("lang") lang: String): List<CategoryEntity>

    // Получение списка товаров
    @GET("/products")
    suspend fun getProducts(): List<Product>

    @GET("/products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Product

    // Удаление продукта
    @DELETE("/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Long)

    // Добавление нового продукта
    @POST("/products")
    suspend fun addProduct(@Body product: Product)

    // Обновление данных о продукте
    @PUT("/products/{id}")
    suspend fun updateProduct(@Path("id") id: Long, @Body product: Product)

    // Получение списка поставщиков
    @GET("/counterparties")
    suspend fun getSuppliers(): List<CounterpartyEntity>

    @GET("/counterparties/{id}")
    suspend fun getCounterpartyById(@Path("id") id: Int): CounterpartyEntity

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
