package com.nayya.myktor.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//TODO В продакшене обязательно перейти на https!!!!!

const val BASE_URL = "http://10.0.2.2:8080"
object RetrofitInstance {
    private lateinit var apiService: ApiService

    fun init() {
        buildApi()
    }

    private fun buildApi() {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(logging)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }

    val api: ApiService
        get() = apiService
}
