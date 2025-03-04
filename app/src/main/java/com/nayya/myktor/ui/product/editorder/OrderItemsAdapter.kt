package com.nayya.myktor.ui.product.editorder

import android.annotation.SuppressLint
import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.nayya.myktor.databinding.ItemOrderItemBinding
import com.nayya.myktor.domain.OrderItemEntity

class OrderItemsAdapter(
    private var items: List<OrderItemEntity>,
) : RecyclerView.Adapter<OrderItemsAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: OrderItemEntity,
            onQuantityChange: (Int) -> Unit,
            onDelete: () -> Unit,
        ) {
            binding.productNameTextView.text = item.productName
            binding.quantityEditText.setText(item.quantity.toString())

            // Используем doAfterTextChanged
            binding.quantityEditText.addTextChangedListener { editable: Editable? ->
                onQuantityChange(editable?.toString()?.toIntOrNull() ?: 0)
            }

            binding.deleteButton.setOnClickListener {
                onDelete()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            items[position],
            onQuantityChange = { newQuantity ->
                items = items.toMutableList().apply {
                    this[position] = this[position].copy(
                        quantity = newQuantity,
                        supplierName = this[position].supplierName ?: "" // Защита от null
                    )
                }
            },
            onDelete = {
                items = items.toMutableList().apply {
                    removeAt(position)
                }
                notifyDataSetChanged()
            }
        )
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newItems: List<OrderItemEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun getItems(): List<OrderItemEntity> = items

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(item: OrderItemEntity) {
        items = items + item
        notifyDataSetChanged()
    }
}
