package com.nayya.myktor.ui.product.category.subcategory

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentSubcategoryBinding
import com.nayya.myktor.domain.productentity.CategoryEntity
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.domain.productentity.Subcategory
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
        val categoryId = category.id ?: return
        val subcategoryIdsOfThisCategory = category.subcategories.orEmpty()
            .mapNotNull { it.id }
            .toSet()

        val productsWithoutSub = allProducts.filter { product ->
            val productCategoryIds = product.categoryIds.orEmpty()
            val productSubcategoryIds = product.subcategoryIds.orEmpty()

            // Собираем связи categoryId → subcategoryId (или null)
            val links = productCategoryIds.associateWith { catId ->
                product.subcategories.orEmpty()
                    .firstOrNull { it.categoryId == catId }?.id
            }.toList()

            val currentCategoryLinks = links.filter { it.first == categoryId }

            Log.d("FilterDebug", "📦 ${product.name}")
            Log.d("FilterDebug", " - categories: $productCategoryIds")
            Log.d("FilterDebug", " - subcategories: $productSubcategoryIds")
            Log.d("FilterDebug", " - links: $links")
            Log.d("FilterDebug", " - currentCategoryLinks: $currentCategoryLinks")
            Log.d("FilterDebug", " - subIds of current category: $subcategoryIdsOfThisCategory")

            if (productCategoryIds.size != productSubcategoryIds.size) {
                Log.d(
                    "FilterDebug",
                    "💥 [${product.name}] categoryIds.size != subcategoryIds.size → ${productCategoryIds.size} != ${productSubcategoryIds.size}"
                )
            }

            if (currentCategoryLinks.isEmpty()) {
                Log.d("FilterDebug", "❌ ${product.name} — нет связи с категорией ${category.name}")
                return@filter false
            }

            val hasSubInThisCategory = currentCategoryLinks.any { (_, subId) ->
                val issue = when {
                    subId == null -> {
                        // это ок — это то, что мы и ищем
                        false
                    }

                    !subcategoryIdsOfThisCategory.contains(subId) -> {
                        Log.d(
                            "FilterDebug",
                            "💥 ${product.name} — subId $subId НЕ входит в подкатегории категории ${category.name}"
                        )
                        true
                    }

                    else -> true
                }
                issue
            }

            return@filter if (hasSubInThisCategory) {
                Log.d(
                    "FilterDebug",
                    "❌ ${product.name} — привязан к подкатегории текущей категории ${category.name}"
                )
                false
            } else {
                Log.d(
                    "FilterDebug",
                    "✅ ${product.name} — привязан к категории ${category.name} БЕЗ подкатегорий"
                )
                true
            }
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
                getController().openProductsBySubcategory(
                    sub.id ?: return@CombinedAdapter,
                    allProducts
                )
            },
            onProductClick = { product ->
                getController().openProductFragment(product)
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

        // Анимация появления элементов списка
        val animation =
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_fall_down)
        binding.productRecyclerView.layoutAnimation = animation
        binding.productRecyclerView.scheduleLayoutAnimation()
    }

    private fun getController(): Controller = activity as Controller

    interface Controller {
        fun openProductsBySubcategory(subcategoryId: Long, products: List<Product>)
        fun openProductFragment(product: Product)
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
