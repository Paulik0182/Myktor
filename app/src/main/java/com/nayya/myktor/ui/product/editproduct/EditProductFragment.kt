package com.nayya.myktor.ui.product.editproduct

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentEditProductBinding
import com.nayya.myktor.domain.ProductEntity
import com.nayya.myktor.ui.product.products.ProductViewModel
import com.nayya.myktor.utils.viewBinding
import java.math.BigDecimal

class EditProductFragment : Fragment(R.layout.fragment_edit_product) {

    private val binding by viewBinding<FragmentEditProductBinding>()

    private val productViewModel: ProductViewModel by activityViewModels()

    private val viewModel: EditProductViewModel by lazy {
        EditProductViewModel(productViewModel)
    }

    private var productId: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments?.containsKey(ARG_PRODUCT_ID) == true) {
            productId = arguments?.getInt(ARG_PRODUCT_ID)
        }

        val productName = arguments?.getString(ARG_PRODUCT_NAME)
        val productDescription = arguments?.getString(ARG_PRODUCT_DESCRIPTION)
        val productPrice = arguments?.getString(ARG_PRODUCT_PRICE)?.let { BigDecimal(it) }

        binding.productNameEditText.setText(productName ?: "")
        binding.descriptionEditText.setText(productDescription ?: "")
        binding.priceEditText.setText(productPrice?.toPlainString() ?: "")

        binding.saveButton.setOnClickListener {
            val name = binding.productNameEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()
            val priceText = binding.priceEditText.text.toString()

            if (name.isNotEmpty() && description.isNotEmpty() && priceText.isNotEmpty()) {
                val price = try {
                    BigDecimal(priceText)
                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Не верный формат цены", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                viewModel.saveProduct(
                    productId,
                    name,
                    description,
                    price,
                    stockQuantity = 0, // добавить выбор количества на складе
                    minStockQuantity = 0, // добавить ввод минимального количества
                    productCodes = emptyList(), // Добавить ввод штрих-кодов
                    isDemanded = true, // Добавить чекбокс
                    productLinks = emptyList(), // Пока заглушка, добавить UI
                    locations = emptyList(), // Пока заглушка, добавить UI
                    images = emptyList(), // Пока заглушка, добавить UI
                    onSuccess = {
                        setFragmentResult("product_updated", Bundle())
                        parentFragmentManager.popBackStack()
                        Toast.makeText(requireContext(), "Продукт сохранен", Toast.LENGTH_SHORT)
                            .show()
                    },
                    onError = {
                        Toast.makeText(requireContext(), "Ошибка сохранения", Toast.LENGTH_SHORT)
                            .show()
                    }
                )
            } else {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getController(): Controller = activity as Controller

    interface Controller

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getController()
    }

    companion object {

        private const val ARG_PRODUCT_ID = "product_id"
        private const val ARG_PRODUCT_NAME = "product_name"
        private const val ARG_PRODUCT_DESCRIPTION = "product_description"
        private const val ARG_PRODUCT_PRICE = "product_price"

        fun newInstance(productEntity: ProductEntity? = null): EditProductFragment {
            val fragment = EditProductFragment()
            val args = Bundle()
            productEntity?.let {
                it.id?.let { it1 -> args.putInt(ARG_PRODUCT_ID, it1) }
                args.putString(ARG_PRODUCT_NAME, it.name)
                args.putString(ARG_PRODUCT_DESCRIPTION, it.description)
                args.putString(ARG_PRODUCT_PRICE, it.price.toPlainString())
            }
            fragment.arguments = args
            return fragment
        }
    }
}