package com.nayya.myktor.ui.product.products

import android.annotation.SuppressLint
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nayya.myktor.R
import com.nayya.myktor.databinding.ItemProductBinding
import com.nayya.myktor.domain.productentity.Product

class ProductsAdapter(
    private var items: List<Product>,
    private val onItemClick: (Product) -> Unit,
    private val onLongClick: (Product) -> Unit,
) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var count = 1

        fun bind(
            product: Product,
            onItemClick: (Product) -> Unit,
            onLongClick: (Product) -> Unit,
        ) {
            binding.tvProductName.text = product.name

            val imageBase64 = product.productImages ?: emptyList()

            // Заглушка (временно)
            if (!imageBase64.isNullOrEmpty()) {
                val base64 = imageBase64.first().imageBase64
                val decoded = Base64.decode(base64, Base64.DEFAULT)
                Glide.with(binding.ivProductImage.context)
                    .asBitmap()
                    .load(decoded)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(binding.ivProductImage)
            } else {
                binding.ivProductImage.setImageResource(R.drawable.ic_placeholder)
            }

            binding.ivFavorite.setOnClickListener {
                // просто переключаем цвет для визуального эффекта
                val isSelected = it.tag as? Boolean ?: false
                binding.ivFavorite.setImageResource(
                    if (isSelected) R.drawable.ic_favorite_border else R.drawable.ic_favorite_filled
                )
                it.tag = !isSelected
            }

            // Нажатие на сам item (пока редактирование)
            binding.cardProduct.setOnClickListener {
                onItemClick(product)
            }

//            binding.root.setOnClickListener {
//                onItemClick(product)
//            }

            // Удержание — удалить
//            binding.cardProduct.setOnLongClickListener {
//                onLongClick(product)
//                true
//            }

            binding.root.setOnLongClickListener {
                val popup = PopupMenu(binding.root.context, binding.root)
                popup.menuInflater.inflate(R.menu.menu_supplier, popup.menu)
                popup.setOnMenuItemClickListener {
                    if (it.itemId == R.id.action_delete) {
                        onLongClick(product)
                        true
                    } else false
                }
                popup.show()
                true
            }

            // Кнопка "Заказать"
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            items[position],
            onItemClick,
            onLongClick
        )
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
}
