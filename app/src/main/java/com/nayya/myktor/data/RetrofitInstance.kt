package com.nayya.myktor.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080") // Используем localhost для эмулятора
            .addConverterFactory(GsonConverterFactory.create()) // JSON-конвертер
            .build()
            .create(ApiService::class.java)
    }
}
