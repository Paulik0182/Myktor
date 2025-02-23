package com.nayya.myktor.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitInstance {
    val api: ApiService by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080") // Используем localhost для эмулятора
            .addConverterFactory(GsonConverterFactory.create()) // JSON-конвертер
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}
