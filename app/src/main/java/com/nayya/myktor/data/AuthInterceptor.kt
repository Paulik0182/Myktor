package com.nayya.myktor.data

import android.util.Log
import com.nayya.myktor.data.prefs.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenStorage.getToken()
        Log.d("AuthInterceptor", "Токен в хедере: ${token?.take(10)}...")

        val request = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            request.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(request.build())
    }
}
