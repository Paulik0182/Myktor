package com.nayya.myktor.ui.product.productview

import androidx.lifecycle.ViewModel
import com.nayya.myktor.domain.productentity.Product
import android.util.Base64
import androidx.core.text.bold
import androidx.core.text.buildSpannedString

class ViewProductViewModel : ViewModel() {

    fun getStockInfo(product: Product): String {
        val unit = product.measurementUnitAbbreviation.orEmpty()
        return "На складе: ${product.totalStockQuantity} $unit"
    }

    fun getCodesText(product: Product): CharSequence {
        val codesList = product.productCodes.orEmpty()
            .mapNotNull { it.codeName?.takeIf { name -> name.isNotBlank() } }

        if (codesList.isEmpty()) return ""

        return buildSpannedString {
            bold { append("Коды: ") }
            append(codesList.joinToString(", "))
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
