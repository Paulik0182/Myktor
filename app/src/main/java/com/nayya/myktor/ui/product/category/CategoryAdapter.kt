package com.nayya.myktor.ui.product.category

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.nayya.myktor.domain.productentity.CategoryEntity
import com.nayya.myktor.R
import com.nayya.myktor.data.BASE_URL
import com.nayya.myktor.databinding.ItemCategorySubcategoryBinding

class CategoryAdapter(
    private var items: List<CategoryEntity>,
    private val onItemClick: (CategoryEntity) -> Unit,
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemCategorySubcategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            categoryEntity: CategoryEntity,
            onItemClick: (CategoryEntity) -> Unit,
        ) {
            binding.titleTextView.text = categoryEntity.name

            Glide.with(binding.imageView.context)
                .load(BASE_URL + categoryEntity.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imageView)

            binding.root.setOnClickListener {
                onItemClick(categoryEntity)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategorySubcategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newItems: List<CategoryEntity>) {
        items = newItems
        notifyDataSetChanged()
    }
}