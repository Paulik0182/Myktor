package com.nayya.myktor.ui.product.category.categorypicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nayya.myktor.databinding.FragmentCategoryPickerBinding
import com.nayya.myktor.domain.productentity.CategoryEntity

class CategoryPickerBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentCategoryPickerBinding
    private lateinit var adapter: CategoryTreeAdapter

    private var allCategories: List<CategoryEntity> = emptyList()
    private var selectedCategoryIds: MutableSet<Long> = mutableSetOf()
    private var selectedSubcategoryIds: MutableSet<Long> = mutableSetOf()

    private var listener: ((Set<Long>, Set<Long>) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoryPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получение аргументов
        arguments?.getSerializable(ARG_CATEGORIES)?.let {
            allCategories = it as List<CategoryEntity>
        }

        selectedCategoryIds.addAll(arguments?.getLongArray(ARG_SELECTED_CATEGORY_IDS)?.toList() ?: emptyList())
        selectedSubcategoryIds.addAll(arguments?.getLongArray(ARG_SELECTED_SUBCATEGORY_IDS)?.toList() ?: emptyList())

        adapter = CategoryTreeAdapter(
            categories = allCategories,
            selectedCategories = selectedCategoryIds,
            selectedSubcategories = selectedSubcategoryIds,
            onCategoryChecked = { id, isChecked ->
                if (isChecked) selectedCategoryIds.add(id) else selectedCategoryIds.remove(id)
            },
            onSubcategoryChecked = { id, isChecked ->
                if (isChecked) selectedSubcategoryIds.add(id) else selectedSubcategoryIds.remove(id)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.btnApply.setOnClickListener {
            listener?.invoke(selectedCategoryIds, selectedSubcategoryIds)
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    fun setOnApplyListener(callback: (Set<Long>, Set<Long>) -> Unit) {
        listener = callback
    }

    companion object {
        private const val ARG_CATEGORIES = "arg_categories"
        private const val ARG_SELECTED_CATEGORY_IDS = "arg_sel_cat"
        private const val ARG_SELECTED_SUBCATEGORY_IDS = "arg_sel_subcat"

        fun newInstance(
            categories: List<CategoryEntity>,
            selectedCategoryIds: List<Long>,
            selectedSubcategoryIds: List<Long>
        ): CategoryPickerBottomSheet {
            return CategoryPickerBottomSheet().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CATEGORIES, ArrayList(categories))
                    putLongArray(ARG_SELECTED_CATEGORY_IDS, selectedCategoryIds.toLongArray())
                    putLongArray(ARG_SELECTED_SUBCATEGORY_IDS, selectedSubcategoryIds.toLongArray())
                }
            }
        }
    }
}
