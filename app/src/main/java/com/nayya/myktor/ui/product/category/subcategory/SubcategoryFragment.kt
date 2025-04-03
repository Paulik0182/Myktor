package com.nayya.myktor.ui.product.category.subcategory

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentSubcategoryBinding
import com.nayya.myktor.domain.productentity.CategoryEntity
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.domain.productentity.Subcategory
import com.nayya.myktor.ui.product.products.ProductsAdapter
import com.nayya.myktor.utils.viewBinding

class SubcategoryFragment : Fragment(R.layout.fragment_subcategory) {

    private lateinit var category: CategoryEntity
    private lateinit var allProducts: List<Product>

    //    private lateinit var viewModel: SubcategoryViewModel
    private val binding by viewBinding<FragmentSubcategoryBinding>()
    private lateinit var combinedAdapter: CombinedAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getSerializable(ARG_CATEGORY) as CategoryEntity
            allProducts = it.getSerializable(ARG_PRODUCTS) as List<Product>
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val subcategories = category.subcategories.orEmpty()
        val productsWithoutSub = allProducts.filter { product ->
            val categoryId = category.id ?: return@filter false
            product.categoryIds.orEmpty().contains(categoryId) &&
                    product.subcategoryIds.orEmpty().isEmpty()
        }

        // Создаём единый список: подкатегории → разделитель → продукты
        val combinedItems = mutableListOf<CombinedItem>().apply {
            if (subcategories.isNotEmpty()) {
                addAll(subcategories.map { CombinedItem.SubcategoryItem(it) })
            }

            if (subcategories.isNotEmpty() && productsWithoutSub.isNotEmpty()) {
                add(CombinedItem.Divider) // добавляем разделитель
            }

            addAll(productsWithoutSub.map { CombinedItem.ProductItem(it) })
        }

        // Настраиваем GridLayoutManager с разной шириной колонок
        val layoutManager = GridLayoutManager(requireContext(), 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (combinedItems[position]) {
                        is CombinedItem.ProductItem -> 1 // продукт — одна колонка
                        else -> 2 // подкатегория и разделитель — на всю ширину
                    }
                }
            }
        }

        // Создаём адаптер
        combinedAdapter = CombinedAdapter(
            combinedItems,
            onSubcategoryClick = { sub ->
                getController().openProductsBySubcategory(sub.id ?: return@CombinedAdapter, allProducts)
            },
            onProductClick = { product ->
                // TODO: редактирование
            },
            onProductLongClick = { product ->
                // TODO: удаление
            }
        )

        // Назначаем всё в RecyclerView
        binding.subcategoryRecyclerView.visibility = View.GONE
        binding.productRecyclerView.apply {
            this.layoutManager = layoutManager
            this.adapter = combinedAdapter
        }

//        val subcategories = category.subcategories.orEmpty()
//        val productsWithoutSub = allProducts.filter { product ->
//            val categoryId = category.id ?: return@filter false
//            product.categoryIds.orEmpty().contains(categoryId) &&
//                    product.subcategoryIds.orEmpty().isEmpty()
//        }
//
//        val combinedItems = mutableListOf<CombinedItem>()
//
//        if (subcategories.isNotEmpty()) {
//            combinedItems.addAll(subcategories.map { CombinedItem.SubcategoryItem(it) })
//        }
//
//        if (subcategories.isNotEmpty() && productsWithoutSub.isNotEmpty()) {
//            combinedItems.add(CombinedItem.Divider) // разделитель
//        }
//
//        combinedItems.addAll(productsWithoutSub.map { CombinedItem.ProductItem(it) })
//
//        val adapter = CombinedAdapter(
//            combinedItems,
//            onSubcategoryClick = { sub -> getController().openProductsBySubcategory(sub.id ?: return@CombinedAdapter, allProducts) },
//            onProductClick = { product -> /* редактирование */ },
//            onProductLongClick = { product -> /* удаление */ }
//        )
//
//        binding.subcategoryRecyclerView.visibility = View.GONE
//        val layoutManager = GridLayoutManager(requireContext(), 2)
//        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
//            override fun getSpanSize(position: Int): Int {
//                return when (combinedItems[position]) {
//                    is CombinedItem.ProductItem -> 1
//                    else -> 2
//                }
//            }
//        }
//        binding.productRecyclerView.layoutManager = layoutManager
//        binding.productRecyclerView.adapter = adapter
    }

    private fun getController(): Controller = activity as Controller

    interface Controller {
        fun openProductsBySubcategory(subcategoryId: Long, products: List<Product>)
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

sealed class CombinedItem {
    data class SubcategoryItem(val subcategory: Subcategory) : CombinedItem()
    data class ProductItem(val product: Product) : CombinedItem()
    object Divider : CombinedItem()
}
