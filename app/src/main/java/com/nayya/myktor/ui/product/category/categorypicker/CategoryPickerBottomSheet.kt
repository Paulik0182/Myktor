package com.nayya.myktor.ui.product.category.categorypicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentCategoryPickerBinding
import com.nayya.myktor.domain.productentity.CategoryEntity

class CategoryPickerBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentCategoryPickerBinding
    private lateinit var adapter: CategoryTreeAdapter

    private var allCategories: List<CategoryEntity> = emptyList()
    private var selectedCategoryIds: MutableSet<Long> = mutableSetOf()
    private var selectedSubcategoryIds: MutableSet<Long> = mutableSetOf()

    private var listener: ((Set<Long>, Set<Long>) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme_Compat)
    }

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
            allCategories = (it as? List<CategoryEntity>)?.map { cat ->
                cat.copy(
                    subcategories = cat.subcategories ?: emptyList(),
                    translations = cat.translations ?: emptyList()
                )
            } ?: emptyList()
        } ?: run {
            allCategories = emptyList()
        }

        selectedCategoryIds.addAll(arguments?.getLongArray(ARG_SELECTED_CATEGORY_IDS)?.toList() ?: emptyList())
        selectedSubcategoryIds.addAll(arguments?.getLongArray(ARG_SELECTED_SUBCATEGORY_IDS)?.toList() ?: emptyList())

        adapter = CategoryTreeAdapter(
            categories = allCategories,
            selectedCategories = selectedCategoryIds,
            selectedSubcategories = selectedSubcategoryIds,
            onCategoryChecked = { id, isChecked ->
                if (isChecked) {
                    selectedCategoryIds.add(id)
                    val index = findCategoryIndex(id)
                    if (index != -1) adapter.notifyItemChanged(index)
                } else {
                    selectedCategoryIds.remove(id)

                    // Удалим все связанные подкатегории
                    val category = allCategories.find { it.id == id }
                    category?.subcategories?.forEach { sub ->
                        selectedSubcategoryIds.remove(sub.id)
                    }

                    // Обновим визуально категорию и подкатегории
                    val startIndex = findCategoryIndex(id)
                    if (startIndex != -1) {
                        binding.recyclerView.post {
                            adapter.notifyItemChanged(startIndex) // категория
                            category?.subcategories?.forEachIndexed { i, _ ->
                                adapter.notifyItemChanged(startIndex + 1 + i)
                            }
                        }
                    }
                }
            },
            onSubcategoryChecked = { subId, isChecked ->
                if (isChecked) {
                    selectedSubcategoryIds.add(subId)

                    // ищем родительскую категорию и добавляем её
                    val parentCategory = allCategories.find { cat ->
                        cat.subcategories.any { it.id == subId }
                    }
                    parentCategory?.id?.let { parentId ->
                        if (selectedCategoryIds.add(parentId)) {
                            val categoryIndex = findCategoryIndex(parentId)
                            if (categoryIndex != -1) {
                                binding.recyclerView.post {
                                    adapter.notifyItemChanged(categoryIndex)
                                }
                            }
                        }
                    }
                } else {
                    selectedSubcategoryIds.remove(subId)
                }
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Включаем анимацию появления/исчезновения
        (binding.recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = true
        binding.recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fade_in)

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

    private fun findCategoryIndex(categoryId: Long): Int {
        var index = 0
        for (category in allCategories) {
            if (category.id == categoryId) return index
            index += 1 + category.subcategories.size
        }
        return -1
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
