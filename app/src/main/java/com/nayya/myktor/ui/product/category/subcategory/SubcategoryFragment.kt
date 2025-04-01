package com.nayya.myktor.ui.product.category.subcategory

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentCategoriesBinding
import com.nayya.myktor.databinding.FragmentSubcategoryBinding
import com.nayya.myktor.domain.CounterpartyEntity
import com.nayya.myktor.domain.productentity.CategoryEntity
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.ui.product.category.CategoryViewModel
import com.nayya.myktor.ui.product.products.ProductsAdapter
import com.nayya.myktor.utils.viewBinding

class SubcategoryFragment : Fragment(R.layout.fragment_subcategory) {

    private lateinit var category: CategoryEntity
    private lateinit var allProducts: List<Product>

    //    private lateinit var viewModel: SubcategoryViewModel
    private val binding by viewBinding<FragmentSubcategoryBinding>()
    private lateinit var subcategoryAdapter: SubcategoryAdapter
    private lateinit var productsAdapter: ProductsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getSerializable(ARG_CATEGORY) as CategoryEntity
            allProducts = it.getSerializable(ARG_PRODUCTS) as List<Product>
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Подкатегории
        subcategoryAdapter = SubcategoryAdapter(
            category.subcategories,
            onItemClick = { /* переход по подкатегории */ }
        )
        binding.subcategoryRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.subcategoryRecyclerView.adapter = subcategoryAdapter

        // Продукты без подкатегорий
        val productsWithoutSub = allProducts.filter { product ->
            val categoryId = category.id ?: return@filter false
            (product.categoryIds ?: emptyList()).contains(categoryId) &&
                    (product.subcategoryIds ?: emptyList()).isEmpty()
        }


        productsAdapter = ProductsAdapter(
            productsWithoutSub,
            onItemClick = {},
            onLongClick = {}
        )
        binding.productRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.productRecyclerView.adapter = productsAdapter

//        viewModel = ViewModelProvider(this).get(SubcategoryViewModel::class.java)

//        // Адаптеры
//        subcategoryAdapter = SubcategoryAdapter(
//            category.subcategories,
//            onItemClick = { subcategory ->
////                val filtered = viewModel.allProducts.value
////                    ?.filter { it.subcategoryIds.contains(subcategory.id) }
////                    .orEmpty()
////                productsAdapter.updateList(filtered)
//            }
//        )
//
//        productsAdapter = ProductsAdapter(
//            emptyList(),
//            onItemClick = { /* ... */ },
//            onLongClick = { /* ... */ }
//        )
//
//        // RecyclerView
//        binding.subcategoryRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
//        binding.subcategoryRecyclerView.adapter = subcategoryAdapter
//
//        binding.productRecyclerView.layoutManager = LinearLayoutManager(requireContext())
//        binding.productRecyclerView.adapter = productsAdapter
//
//        // Наблюдение за товарами
//        viewModel.allProducts.observe(viewLifecycleOwner) { allProducts ->
//            // Показать сначала все без подкатегории
////            val productsWithoutSub = allProducts.filter { it.subcategoryIds.isEmpty() }
////            productsAdapter.updateList(productsWithoutSub)
//        }
//
//        // Запрос на загрузку
//        viewModel.fetchAllProducts()
    }

    private fun getController(): Controller = activity as Controller

    interface Controller {
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getController()
    }

    companion object {
        private const val ARG_CATEGORY = "arg_category"
        private const val ARG_PRODUCTS = "arg_products"

        @JvmStatic
        fun newInstance(category: CategoryEntity, products: List<Product>) =
            SubcategoryFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CATEGORY, category)
                    putSerializable(ARG_PRODUCTS, ArrayList(products)) // сериализуем список
                }
            }
    }
}