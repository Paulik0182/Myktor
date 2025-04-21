package com.nayya.myktor.ui.product.products

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.productentity.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductViewModel : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _filteredProducts = MediatorLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> get() = _filteredProducts

    private var subcategoryId: Long? = null

    init {
        _filteredProducts.addSource(_products) { updateFilteredProducts() }
    }

    fun setSubcategoryFilter(subId: Long?) {
        subcategoryId = subId
        updateFilteredProducts()
    }

    private fun updateFilteredProducts() {
        val currentProducts = _products.value.orEmpty()
        val filtered = subcategoryId?.let { subId ->
            currentProducts.filter { it.subcategoryIds.orEmpty().contains(subId) }
        } ?: currentProducts
        _filteredProducts.postValue(filtered)
    }

    fun fetchProducts() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getProducts()
                }
                _products.value = emptyList()
                _products.postValue(response)
            } catch (e: Exception) {
                _products.postValue(emptyList()) // В случае ошибки отправляем пустой список
            }
        }
    }

    fun deleteProduct(id: Long) {
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