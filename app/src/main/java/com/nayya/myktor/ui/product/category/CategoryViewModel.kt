package com.nayya.myktor.ui.product.category

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.productentity.CategoryEntity
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.utils.LocaleUtils
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {

    val allProducts = MutableLiveData<List<Product>>()
    val categories = MutableLiveData<List<CategoryEntity>>() // Извлекаем из продуктов

    fun fetchProducts() {
        viewModelScope.launch {
            try {
                val result = RetrofitInstance.api.getProducts()
                Log.d("DEBUG", "Loaded products: ${result.size}")
                result.forEach { Log.d("DEBUG", it.name) }

                allProducts.postValue(result)

                // Извлечь категории из всех продуктов
                val allCategories = result.flatMap { it.categories.orEmpty() }.distinctBy { it.id }
                categories.postValue(allCategories)
            } catch (e: Exception) {
                Log.e("DEBUG", "Error loading products", e)
                allProducts.postValue(emptyList())
                categories.postValue(emptyList())
            }
        }
    }
}

