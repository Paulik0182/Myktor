package com.nayya.myktor.ui.product.products

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.nayya.myktor.R
import com.nayya.myktor.databinding.ItemProductBinding
import com.nayya.myktor.domain.ProductEntity
import com.nayya.myktor.domain.productentity.Product

class ProductsAdapter(
    private var items: List<Product>,
    private val onItemClick: (Product) -> Unit,
    private val onLongClick: (Product) -> Unit,
) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            product: Product,
            onItemClick: (Product) -> Unit,
            onLongClick: (Product) -> Unit,
        ) {
            binding.tvProductName.text = product.name

            binding.root.setOnClickListener {
                onItemClick(product)
            }

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
