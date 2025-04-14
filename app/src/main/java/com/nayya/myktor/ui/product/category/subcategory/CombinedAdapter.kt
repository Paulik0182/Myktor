package com.nayya.myktor.ui.product.category.subcategory

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nayya.myktor.R
import com.nayya.myktor.databinding.ItemCategorySubcategoryBinding
import com.nayya.myktor.databinding.ItemProductBinding
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.domain.productentity.Subcategory
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import com.nayya.myktor.data.BASE_URL

class CombinedAdapter(
    private val items: List<CombinedItem>,
    private val onSubcategoryClick: (Subcategory) -> Unit,
    private val onProductClick: (Product) -> Unit,
    private val onProductLongClick: (Product) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SUBCATEGORY = 0
        private const val TYPE_DIVIDER = 1
        private const val TYPE_PRODUCT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is CombinedItem.SubcategoryItem -> TYPE_SUBCATEGORY
            is CombinedItem.Divider -> TYPE_DIVIDER
            is CombinedItem.ProductItem -> TYPE_PRODUCT
        }
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SUBCATEGORY -> {
                val binding = ItemCategorySubcategoryBinding.inflate(inflater, parent, false)
                SubcategoryViewHolder(binding)
            }

            TYPE_DIVIDER -> {
                val divider = View(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        (3 * resources.displayMetrics.density).toInt()
                    )
                    setBackgroundColor(Color.LTGRAY)
                }
                DividerViewHolder(divider)
            }

            else -> {
                val binding = ItemProductBinding.inflate(inflater, parent, false)
                ProductViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CombinedItem.SubcategoryItem -> (holder as SubcategoryViewHolder).bind(item.subcategory)
            is CombinedItem.ProductItem -> (holder as ProductViewHolder).bind(item.product)
            is CombinedItem.Divider -> Unit // ничего не биндим
        }
    }

    inner class SubcategoryViewHolder(private val binding: ItemCategorySubcategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(subcategory: Subcategory) {
            binding.titleTextView.text = subcategory.name
            Glide.with(binding.imageView.context)
                .load(BASE_URL + subcategory.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(binding.imageView)

            binding.root.setOnClickListener { onSubcategoryClick(subcategory) }
        }
    }

    inner class DividerViewHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var count = 1

        fun bind(product: Product) {
            binding.tvProductName.text = product.name

            val productImages = product.productImages ?: emptyList()
            if (productImages.isNotEmpty()) {
                val imageUrl = productImages.first().imageUrl
                Glide.with(binding.ivProductImage.context)
                    .asBitmap()
                    .load(BASE_URL + imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(binding.ivProductImage)
            } else {
                binding.ivProductImage.setImageResource(R.drawable.ic_placeholder)
            }

            binding.ivFavorite.setOnClickListener {
                val isSelected = it.tag as? Boolean ?: false
                binding.ivFavorite.setImageResource(
                    if (isSelected) R.drawable.ic_favorite_border else R.drawable.ic_favorite_filled
                )
                it.tag = !isSelected

                // Анимация
                val animation = AnimationUtils.loadAnimation(it.context, R.anim.heart_pop)
                it.startAnimation(animation)
            }

            binding.cardProduct.setOnClickListener {
                onProductClick(product)
            }

            binding.root.setOnLongClickListener {
                val popup = PopupMenu(binding.root.context, binding.root)
                popup.menuInflater.inflate(R.menu.menu_supplier, popup.menu)
                popup.setOnMenuItemClickListener {
                    if (it.itemId == R.id.action_delete) {
                        onProductLongClick(product)
                        true
                    } else false
                }
                popup.show()
                true
            }

            binding.btnOrder.setOnClickListener {
                count = 1
                updateCounterUI()
            }

            binding.btnPlus.setOnClickListener {
                count++
                updateCounterUI()
            }

            binding.btnMinus.setOnClickListener {
                count--
                if (count <= 0) {
                    count = 0
                    showOrderButton()
                } else {
                    updateCounterUI()
                }
            }
        }

        private fun updateCounterUI() {
            binding.btnOrder.visibility = View.GONE
            binding.llCounter.visibility = View.VISIBLE
            binding.tvCounter.text = count.toString()
        }

        private fun showOrderButton() {
            binding.llCounter.visibility = View.GONE
            binding.btnOrder.visibility = View.VISIBLE
        }
    }
}
