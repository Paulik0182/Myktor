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
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.domain.productentity.ProductCode
import com.nayya.myktor.domain.productentity.ProductLink
import com.nayya.myktor.ui.product.products.ProductViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class EditProductViewModel(
    private val productViewModel: ProductViewModel,
) : ViewModel() {

    fun saveProduct(
        product: Product,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val productRequest = Product(
                    id = product.id,
                    name = product.name,
                    description = product.description,
                    price = product.price,
                    hasSuppliers = false,
                    supplierCount = 0,
                    totalStockQuantity = product.totalStockQuantity,
                    minStockQuantity = product.minStockQuantity,

                    isDemanded = product.isDemanded,
                    measurementUnitId = product.measurementUnitId,
                    measurementUnitList = product.measurementUnitList,
                    measurementUnit = product.measurementUnit,
                    measurementUnitAbbreviation = product.measurementUnitAbbreviation,

                    productCodes = product.productCodes,
                    productLinks = product.productLinks,
                    productImages = product.productImages,
                    productCounterparties = product.productCounterparties,
                    productSuppliers = product.productSuppliers,
                    measurementUnits = product.measurementUnits,
                    productOrderItem = product.productOrderItem,
                    categories = product.categories,
                    subcategoryIds = product.subcategoryIds,
                    categoryIds = product.categoryIds,
                    subcategories = product.subcategories
                )

                Log.d("API", "Отправка запроса: $productRequest")

                if (product.id == null) {
                    RetrofitInstance.api.addProduct(productRequest)
                    Log.d("API", "Продукт добавлен")
                } else {
                    RetrofitInstance.api.updateProduct(
                        product.id,
                        productRequest
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