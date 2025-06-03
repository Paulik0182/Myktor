package com.nayya.myktor.data

import com.nayya.myktor.data.network.BankAccountRequest
import com.nayya.myktor.data.network.CounterpartyAddressRequest
import com.nayya.myktor.data.network.CounterpartyContactRequest
import com.nayya.myktor.data.network.CounterpartyImageRequest
import com.nayya.myktor.data.network.CounterpartyPatchRequest
import com.nayya.myktor.data.network.CounterpartyRequest
import com.nayya.myktor.data.network.ProductCreateRequest
import com.nayya.myktor.data.network.RepresentativeRequest
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
import com.nayya.myktor.domain.counterpartyentity.Country
import com.nayya.myktor.domain.counterpartyentity.OrderEntity
import com.nayya.myktor.domain.loginentity.LoginRequest
import com.nayya.myktor.domain.loginentity.MeResponse
import com.nayya.myktor.domain.loginentity.RegisterRequest
import com.nayya.myktor.domain.loginentity.ResetPasswordRequest
import com.nayya.myktor.domain.loginentity.ResetRequest
import com.nayya.myktor.domain.productentity.CategoryEntity
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.domain.productentity.ProductImage
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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
    suspend fun addProduct(@Body product: ProductCreateRequest)

    @GET("categories/all")
    suspend fun getAllCategories(): List<CategoryEntity>

    // Обновление данных о продукте
    @PUT("/products/{id}")
    suspend fun updateProduct(@Path("id") id: Long, @Body product: ProductCreateRequest)

    @Multipart
    @POST("/products/{id}/images")
    suspend fun uploadProductImage(
        @Path("id") productId: Long,
        @Part image: MultipartBody.Part
    ): ResponseBody

    @GET("/products/{id}/images")
    suspend fun getProductImages(@Path("id") productId: Long): List<ProductImage>

    @GET("/products/{id}/images/{imageId}")
    suspend fun getProductImage(
        @Path("id") productId: Long,
        @Path("imageId") imageId: Long
    ): ResponseBody

    @DELETE("/products/{id}/images/{imageId}")
    suspend fun deleteProductImage(
        @Path("id") productId: Long,
        @Path("imageId") imageId: Long
    ): ResponseBody

    // изменение порядка изображений
    @POST("/products/{id}/images/reorder")
    suspend fun reorderProductImagesPost(
        @Path("id") productId: Long,
        @Body imageIds: List<Long>
    ): Response<ResponseBody>

    // PATCH и POST делают одно и то же — просто поддержка двух методов. Можно использовать любой.
    @PATCH("/products/{id}/images")
    suspend fun reorderProductImagesPatch(
        @Path("id") productId: Long,
        @Body imageIds: List<Long>
    ): Response<ResponseBody>



    // Получение списка поставщиков
    @GET("/counterparties/all")
    suspend fun getCounterparties(): List<CounterpartyEntity>

    @GET("/counterparties/{id}")
    suspend fun getCounterpartyById(@Path("id") id: Long): CounterpartyEntity

    // Удаление поставщика
    @DELETE("/counterparties/{id}")
    suspend fun deleteCounterparty(@Path("id") id: Long)

    // Добавление нового поставщика
    @POST("/counterparties")
    suspend fun addCounterparty(@Body counterpartyRequest: CounterpartyRequest)

    // Обновление данных поставщика
    @PUT("/counterparties/{id}")
    suspend fun updateCounterparty(
        @Path("id") id: Long,
        @Body counterpartyRequest: CounterpartyRequest
    ): Response<Unit>

    // Получение списка заказов
    @GET("/orders")
    suspend fun getOrders(): List<OrderEntity>

    @GET("/orders/{id}")
    suspend fun getOrderDetails(@Path("id") id: Long): OrderEntity

    @POST("/orders")
    suspend fun createOrder(@Body order: OrderEntity)

    @PUT("/orders/{id}")
    suspend fun updateOrder(@Path("id") id: Long, @Body order: OrderEntity)

    @DELETE("/orders/{id}")
    suspend fun deleteOrder(@Path("id") id: Long)

    @PATCH("counterparties/{id}/contacts")
    suspend fun patchContacts(
        @Path("id") counterpartyId: Long,
        @Body contacts: List<CounterpartyContactRequest>
    ): Response<Unit>

    @GET("/countries")
    suspend fun getCountries(): List<Country>

    @PATCH("counterparties/{id}/basic")
    suspend fun patchBasicFields(
        @Path("id") id: Long,
        @Body body: CounterpartyPatchRequest
    ): Response<Unit>

    @PATCH("counterparties/{id}/addresses")
    suspend fun patchAddresses(
        @Path("id") id: Long,
        @Body addresses: List<CounterpartyAddressRequest>
    ): Response<Unit>

    @PATCH("counterparties/{id}/representatives")
    suspend fun patchRepresentatives(
        @Path("id") id: Long,
        @Body rep: RepresentativeRequest
    ): Response<Unit>

    @PATCH("counterparties/{id}/bankAccounts")
    suspend fun patchBankAccounts(
        @Path("id") id: Long,
        @Body acc: BankAccountRequest
    ): Response<Unit>

    @PATCH("counterparties/{id}/image")
    suspend fun patchImagePath(
        @Path("id") id: Long,
        @Body image: CounterpartyImageRequest
    ): Response<Unit>

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): Map<String, String>

    @POST("/auth/request_password_reset")
    suspend fun requestResetPassword(@Body request: ResetRequest)

    @POST("/auth/reset_password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest)

    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequest)

    @GET("/auth/me")
    suspend fun getMe(): MeResponse

    @POST("/auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("/auth/logout_all")
    suspend fun logoutAll(): Response<Unit>

    @POST("/auth/delete_account")
    suspend fun deleteAccount(): Response<Unit>

    @POST("counterparties/{counterpartyId}/addresses")
    suspend fun createCounterpartyAddress(
        @Path("counterpartyId") counterpartyId: Long,
        @Body request: CounterpartyAddressRequest
    ): Response<Unit>

    @PUT("addresses/{addressId}")
    suspend fun updateCounterpartyAddress(
        @Path("addressId") addressId: Long,
        @Body request: CounterpartyAddressRequest
    ): Response<Unit>

    @PATCH("counterparties/{id}/addresses")
    suspend fun updateAllAddresses(
        @Path("id") id: Long,
        @Body addresses: List<CounterpartyAddressRequest>
    ): Response<Unit>

}
