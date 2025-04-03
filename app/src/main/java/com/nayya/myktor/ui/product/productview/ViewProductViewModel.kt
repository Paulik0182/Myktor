package com.nayya.myktor.ui.product.productview

import androidx.lifecycle.ViewModel
import com.nayya.myktor.domain.productentity.Product
import android.util.Base64

class ViewProductViewModel : ViewModel() {

    fun getCategoryText(product: Product): String {
        val categories = product.categories.orEmpty().joinToString(", ") { it.name }
        val subcategories = product.subcategories.orEmpty().joinToString(", ") { it.name }
        return buildString {
            append("Категории: $categories")
            if (subcategories.isNotBlank()) append("\nПодкатегории: $subcategories")
        }
    }

    fun getStockInfo(product: Product): String {
        val unit = product.measurementUnitAbbreviation.orEmpty()
        return "На складе: ${product.totalStockQuantity} $unit"
    }

    fun getCodesText(product: Product): String {
        return product.productCodes.orEmpty().joinToString(", ") { it.codeName }.let {
            if (it.isNotBlank()) "Коды: $it" else ""
        }
    }

    fun getLinksText(product: Product): String {
        return product.productLinks.orEmpty().joinToString("\n") { it.urlName.orEmpty() }
    }

    fun getDecodedImages(product: Product): List<ByteArray> {
        return product.productImages.orEmpty().mapNotNull { image ->
            image.imageBase64?.takeIf { it.isNotBlank() }?.let { base64 ->
                try {
                    Base64.decode(base64, Base64.DEFAULT)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}
