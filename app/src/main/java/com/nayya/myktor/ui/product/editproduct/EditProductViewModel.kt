package com.nayya.myktor.ui.product.editproduct

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.ProductCodeEntity
import com.nayya.myktor.domain.ProductEntity
import com.nayya.myktor.domain.ProductImageEntity
import com.nayya.myktor.domain.ProductLinkEntity
import com.nayya.myktor.domain.WarehouseLocationEntity
import com.nayya.myktor.ui.product.products.ProductViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class EditProductViewModel(
    private val productViewModel: ProductViewModel,
) : ViewModel() {

    fun saveProduct(
        productId: Int?,
        name: String,
        description: String,
        price: BigDecimal,
        stockQuantity: Int,
        minStockQuantity: Int,
        productCodes: List<ProductCodeEntity>,
        isDemanded: Boolean = true,
        productLinks: List<ProductLinkEntity>,
        locations: List<WarehouseLocationEntity>,
        images: List<ProductImageEntity>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val productEntity = ProductEntity(
                    id = productId,
                    name = name,
                    description = description,
                    price = price,
                    hasSuppliers = false,
                    supplierCount = 0,
                    stockQuantity = stockQuantity,
                    minStockQuantity = minStockQuantity,
                    productCodes = productCodes,
                    isDemanded = isDemanded,
                    productLinks = productLinks,
                    locations = locations,
                    images = images
                )

                Log.d("API", "Отправка запроса: $productEntity")

                if (productId == null) {
                    RetrofitInstance.api.addProduct(productEntity)
                    Log.d("API", "Продукт добавлен")
                } else {
                    RetrofitInstance.api.updateProduct(
                        productId,
                        productEntity
                    )
                    Log.d("API", "Продукт обновлен")
                }

                withContext(Dispatchers.Main) {
                    productViewModel.fetchProducts()
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("API", "Ошибка при сохранении продукта", e)
                onError(e)
            }
        }
    }
}