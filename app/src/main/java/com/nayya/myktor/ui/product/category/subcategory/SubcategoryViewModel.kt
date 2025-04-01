package com.nayya.myktor.ui.product.category.subcategory

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.domain.productentity.Subcategory
import kotlinx.coroutines.launch

class SubcategoryViewModel : ViewModel() {

    val allProducts = MutableLiveData<List<Product>>()

    fun fetchAllProducts() {
        viewModelScope.launch {
            try {
                val products = RetrofitInstance.api.getProducts()
                allProducts.postValue(products)
            } catch (e: Exception) {
                allProducts.postValue(emptyList())
            }
        }
    }
}