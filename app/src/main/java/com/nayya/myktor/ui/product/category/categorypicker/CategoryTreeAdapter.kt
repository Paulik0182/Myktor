package com.nayya.myktor.ui.product.category.categorypicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.nayya.myktor.R
import com.nayya.myktor.domain.productentity.CategoryEntity
import com.nayya.myktor.domain.productentity.Subcategory

class CategoryTreeAdapter(
    private val categories: List<CategoryEntity>,
    private val selectedCategories: Set<Long>,
    private val selectedSubcategories: Set<Long>,
    private val onCategoryChecked: (Long, Boolean) -> Unit,
    private val onSubcategoryChecked: (Long, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return categories.sumOf { 1 + it.subcategories.size }
    }

    override fun getItemViewType(position: Int): Int {
        var index = 0
        for (category in categories) {
            if (position == index) return TYPE_CATEGORY
            index++
            if (position < index + category.subcategories.size) return TYPE_SUBCATEGORY
            index += category.subcategories.size
        }
        throw IllegalStateException("Invalid position: $position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_CATEGORY -> {
                val view = inflater.inflate(R.layout.item_category_checkbox, parent, false)
                CategoryViewHolder(view)
            }
            TYPE_SUBCATEGORY -> {
                val view = inflater.inflate(R.layout.item_subcategory_checkbox, parent, false)
                SubcategoryViewHolder(view)
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var index = 0
        for (category in categories) {
            if (position == index) {
                (holder as CategoryViewHolder).bind(category)
                return
            }
            index++
            for (subcategory in category.subcategories) {
                if (position == index) {
                    (holder as SubcategoryViewHolder).bind(subcategory)
                    return
                }
                index++
            }
        }
    }

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val checkbox: CheckBox = view.findViewById(R.id.checkbox)

        fun bind(category: CategoryEntity) {
            checkbox.text = category.name
            checkbox.isChecked = selectedCategories.contains(category.id)
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                category.id?.let { onCategoryChecked(it, isChecked) }
            }
        }
    }

    inner class SubcategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val checkbox: CheckBox = view.findViewById(R.id.checkbox)

        fun bind(subcategory: Subcategory) {
            checkbox.text = subcategory.name
            checkbox.isChecked = selectedSubcategories.contains(subcategory.id)
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                subcategory.id?.let { onSubcategoryChecked(it, isChecked) }
            }
        }
    }

    companion object {
        private const val TYPE_CATEGORY = 0
        private const val TYPE_SUBCATEGORY = 1
    }
}
