package com.nayya.myktor.ui.product.products

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductViewModel : ViewModel() {

    val products = MutableLiveData<List<ProductEntity>>()

    fun fetchProducts() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getProducts()
                }
                products.value = emptyList()
                products.postValue(response)
            } catch (e: Exception) {
                products.postValue(emptyList()) // В случае ошибки отправляем пустой список
            }
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.deleteProduct(id)
                fetchProducts()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Products", "Ошибка: ${e.localizedMessage}")
            }
        }
    }
}