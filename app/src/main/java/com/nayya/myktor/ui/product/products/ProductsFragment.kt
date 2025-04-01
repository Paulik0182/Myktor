package com.nayya.myktor.ui.product.products

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentProductsBinding
import com.nayya.myktor.domain.ProductEntity
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.utils.viewBinding
import kotlinx.coroutines.launch

class ProductsFragment : Fragment(R.layout.fragment_products) {

    private lateinit var viewModel: ProductViewModel
    private val binding by viewBinding<FragmentProductsBinding>()
    private lateinit var adapter: ProductsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ProductViewModel::class.java)

        adapter = ProductsAdapter(
            emptyList(),
            onItemClick = { product ->
                getController().openEditProductFragment(product)
            }
        ) { productDel ->
            productDel.id?.let { viewModel.deleteProduct(it) }

        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.updateList(products)
        }

        // Запускаем функцию в корутине (первая загрузка данных)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fetchProducts()
        }

        setFragmentResultListener("product_updated") { _, _ ->
            viewModel.fetchProducts()
        }

        binding.addProductButton.setOnClickListener {
            getController().openEditProductFragment(null)
        }
    }

    private fun getController(): Controller = activity as Controller

    interface Controller {
        fun openEditProductFragment(product: Product?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getController()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProductsFragment()
    }
}