package com.nayya.myktor.ui.product.category

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentCategoriesBinding
import com.nayya.myktor.domain.productentity.CategoryEntity
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.utils.viewBinding
import kotlinx.coroutines.launch

class CategoriesFragment : Fragment(R.layout.fragment_categories) {

    private lateinit var viewModel: CategoryViewModel
    private val binding by viewBinding<FragmentCategoriesBinding>()
    private lateinit var adapter: CategoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)

        adapter = CategoryAdapter(
            emptyList(),
            onItemClick = {categoryEntity ->
                // Передаём category и ВСЕ продукты — во фрагмент подкатегорий
                val products = viewModel.allProducts.value ?: emptyList()
                getController().openSubcategoryFragment(categoryEntity, products)
            }
        )

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter

        viewModel.categories.observe(viewLifecycleOwner) {
            adapter.updateList(it)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fetchProducts()
        }
    }

    private fun getController(): Controller = activity as Controller

    interface Controller {
        fun openSubcategoryFragment(category: CategoryEntity, products: List<Product>)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getController()
    }

    companion object {

        @JvmStatic
        fun newInstance() = CategoriesFragment()
    }
}