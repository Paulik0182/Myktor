package com.nayya.myktor.ui.product

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentAccountingProductBinding
import com.nayya.myktor.utils.viewBinding

class AccountingProductsFragment : Fragment(R.layout.fragment_accounting_product) {

    private val binding by viewBinding<FragmentAccountingProductBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ordersButton.setOnClickListener {
            getController().openOrders()
        }

        binding.productsButton.setOnClickListener {
            getController().openProduct()
        }

        binding.suppliersButton.setOnClickListener {
            getController().openSuppliers()
        }

        binding.productsCategoryButton.setOnClickListener {
            getController().openCategory()
        }
    }

    private fun getController(): Controller = activity as Controller

    interface Controller {
        fun openOrders()
        fun openProduct()
        fun openCategory()
        fun openSuppliers()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getController()
    }

    companion object {
        @JvmStatic
        fun newInstance() = AccountingProductsFragment()
    }
}