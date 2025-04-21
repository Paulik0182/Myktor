package com.nayya.myktor.ui.product.orders

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.nayya.myktor.R
import com.nayya.myktor.databinding.ItemOrderBinding
import com.nayya.myktor.domain.counterpartyentity.OrderEntity

class OrdersAdapter(
    private var items: List<OrderEntity>,
    private val onItemClick: (OrderEntity) -> Unit,
    private val onLongClick: (OrderEntity) -> Unit,
) :
    RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            orderEntity: OrderEntity,
            onItemClick: (OrderEntity) -> Unit,
            onLongClick: (OrderEntity) -> Unit,
        ) {
            binding.tvOrderId.text = "Заказ №${orderEntity.id}"

            binding.root.setOnClickListener {
                onItemClick(orderEntity)
            }

            binding.root.setOnLongClickListener {
                val popup = PopupMenu(binding.root.context, binding.root)
                popup.menuInflater.inflate(R.menu.menu_supplier, popup.menu)
                popup.setOnMenuItemClickListener {
                    if (it.itemId == R.id.action_delete) {
                        onLongClick(orderEntity)
                        true
                    } else false
                }
                popup.show()
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderBinding.inflate(
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
    fun updateList(newItems: List<OrderEntity>) {
        items = newItems
        notifyDataSetChanged()
    }
}
