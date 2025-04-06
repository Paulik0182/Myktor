package com.nayya.myktor.ui.product.editproduct

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.ApiService
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.productentity.CategoryEntity
import com.nayya.myktor.domain.productentity.MeasurementUnitList
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.domain.productentity.ProductCreateRequest
import com.nayya.myktor.domain.productentity.ProductLinkRequest
import com.nayya.myktor.ui.product.products.ProductViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal

class EditProductViewModel(
    private val api: ApiService,
    private val productViewModel: ProductViewModel,
) : ViewModel() {

    private var editingProduct: Product? = null

    val units = MutableLiveData<List<MeasurementUnitList>>()
    val selectedUnitId = MutableLiveData<Long>()

    val links = MutableLiveData<MutableList<String>>(mutableListOf())

    val product = MutableLiveData<Product>()

    var selectedCategoryIds: List<Long> = emptyList()
        private set
    var selectedSubcategoryIds: List<Long> = emptyList()
        private set

    private val _categories = MutableLiveData<List<CategoryEntity>>()
    val categories: LiveData<List<CategoryEntity>> get() = _categories

    fun loadProduct(productId: Long) {
        viewModelScope.launch {
            try {
                val loaded = RetrofitInstance.api.getProductById(productId)
                editingProduct = loaded
                product.postValue(loaded)

                selectedUnitId.postValue(loaded.measurementUnitId)
                links.postValue(
                    loaded.productLinks?.mapNotNull { it.urlName }?.toMutableList()
                        ?: mutableListOf()
                )
                selectedCategoryIds = loaded.categoryIds ?: emptyList()
                selectedSubcategoryIds = loaded.subcategoryIds ?: emptyList()
            } catch (e: Exception) {
                // обработка ошибки
            }
        }
    }

    fun setUnits(unitsList: List<MeasurementUnitList>) {
        //Здесть получаем по запросу данные меры измерения
        units.value = unitsList
    }

    fun updateLink(index: Int, value: String) {
        val list = links.value ?: mutableListOf()
        if (index in list.indices) {
            list[index] = value
            links.value = list
        }
    }

    fun addLink() {
        if ((links.value?.size ?: 0) < 4) {
            val updated = links.value ?: mutableListOf()
            updated.add("")
            links.value = updated
        }
    }

    fun removeLink(index: Int) {
        val updated = links.value ?: return
        if (index in updated.indices) {
            updated.removeAt(index)
            links.value = updated
        }
    }

    fun setCategorySelection(categoryIds: List<Long>, subcategoryIds: List<Long>) {
        selectedCategoryIds = categoryIds
        selectedSubcategoryIds = subcategoryIds
    }

    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getAllCategories()
                _categories.postValue(response)
            } catch (e: Exception) {
                _categories.postValue(emptyList())
            }
        }
    }

    private fun buildCreateRequest(
        name: String,
        description: String,
        price: BigDecimal,
    ): ProductCreateRequest {
        return ProductCreateRequest(
            name = name,
            description = description,
            price = price,
            hasSuppliers = false,
            supplierCount = 0,
            totalStockQuantity = 0,
            minStockQuantity = 0,
            isDemanded = false,
            measurementUnitId = selectedUnitId.value ?: 1L,
            productCodes = emptyList(),
            productImages = emptyList(),
            productLinks = links.value?.map {
                ProductLinkRequest(url = it)
            } ?: emptyList(),
            productCounterparties = emptyList(),
            productSuppliers = emptyList(),
            categories = selectedCategoryIds,
            subcategories = selectedSubcategoryIds
        )
    }

    fun saveProduct(
        name: String,
        description: String,
        price: BigDecimal,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val request = buildCreateRequest(name, description, price)

                editingProduct?.id?.let {
                    api.updateProduct(it, request)
                } ?: api.addProduct(request)

                productViewModel.fetchProducts()
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}
