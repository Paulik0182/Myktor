package com.nayya.myktor.ui.product.products

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentProductsBinding
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.utils.viewBinding
import kotlinx.coroutines.launch

class ProductsFragment : Fragment(R.layout.fragment_products) {

    private lateinit var viewModel: ProductViewModel
    private val binding by viewBinding<FragmentProductsBinding>()
    private lateinit var adapter: ProductsAdapter
    private var subcategoryId: Long? = null
    private var passedProducts: List<Product>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            passedProducts = it.getSerializable(ARG_PRODUCTS) as List<Product>
            subcategoryId = it.getLong(ARG_SUBCATEGORY_ID, -1).takeIf { id -> id != -1L }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        viewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        adapter = ProductsAdapter(
            emptyList(),
            onItemClick = { product ->
                getController().openProductFragment(product)
            }
        ) { productDel ->
            productDel.id?.let { viewModel.deleteProduct(it) }
        }

//        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter

        val animation =
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_fall_down)
        binding.recyclerView.layoutAnimation = animation

        // Наблюдение за отфильтрованным списком
        viewModel.filteredProducts.observe(viewLifecycleOwner) { products ->
            adapter.updateList(products)
            // Здесь вручную запускаем анимацию
            binding.recyclerView.scheduleLayoutAnimation()
        }

        if (passedProducts != null && passedProducts!!.isNotEmpty()) {
            // Если продукты были переданы (например, из подкатегории)
            viewModel.setSubcategoryFilter(subcategoryId)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.fetchProducts()
            }
        } else {
            // Загружаем с сервера
            // Запускаем функцию в корутине (первая загрузка данных)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.fetchProducts()
            }
        }

        setFragmentResultListener("product_updated") { _, _ ->
            viewModel.fetchProducts()

            // Здесь вручную запускаем анимацию
            binding.recyclerView.scheduleLayoutAnimation()
        }

        binding.addProductButton.setOnClickListener {
//            getController().openEditProductFragment(null)
        }
    }

    private fun getController(): Controller = activity as Controller

    interface Controller {
        fun openProductFragment(product: Product)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getController()
    }

    companion object {
        private const val ARG_PRODUCTS = "arg_products"
        private const val ARG_SUBCATEGORY_ID = "arg_subcategory_id"

        @JvmStatic
        fun newInstance(
            products: List<Product> = emptyList(),
            subcategoryId: Long? = null,
        ) = ProductsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_PRODUCTS, ArrayList(products))
                subcategoryId?.let { putLong(ARG_SUBCATEGORY_ID, it) }
            }
        }
    }
}