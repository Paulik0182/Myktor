package com.nayya.myktor.ui.product.products

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.nayya.myktor.R
import com.nayya.myktor.databinding.ItemProductBinding
import com.nayya.myktor.domain.ProductEntity

class ProductsAdapter(
    private var items: List<ProductEntity>,
    private val onItemClick: (ProductEntity) -> Unit,
    private val onLongClick: (ProductEntity) -> Unit,
) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            productEntity: ProductEntity,
            onItemClick: (ProductEntity) -> Unit,
            onLongClick: (ProductEntity) -> Unit,
        ) {
            binding.tvProductName.text = productEntity.name

            binding.root.setOnClickListener {
                onItemClick(productEntity)
            }

            binding.root.setOnLongClickListener {
                val popup = PopupMenu(binding.root.context, binding.root)
                popup.menuInflater.inflate(R.menu.menu_supplier, popup.menu)
                popup.setOnMenuItemClickListener {
                    if (it.itemId == R.id.action_delete) {
                        onLongClick(productEntity)
                        true
                    } else false
                }
                popup.show()
                true
            }
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
    fun updateList(newItems: List<ProductEntity>) {
        items = newItems
        notifyDataSetChanged()
    }
}
