package com.nayya.myktor.ui.product.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import com.nayya.myktor.data.RetrofitInstance

class ProductViewModel : ViewModel() {

    val products = MutableLiveData<List<String>>()

    fun fetchProducts() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getProducts()
                products.postValue(response)
            } catch (e: Exception) {
                products.postValue(emptyList()) // В случае ошибки отправляем пустой список
            }
        }
    }
}