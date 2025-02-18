package com.nayya.myktor.ui.product.suppliers

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.nayya.myktor.R
import com.nayya.myktor.databinding.ItemSupplierBinding
import com.nayya.myktor.domain.Counterparty

class SuppliersAdapter(
    private var items: List<Counterparty>,
    private val onLongClick: (Counterparty) -> Unit,
) : RecyclerView.Adapter<SuppliersAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemSupplierBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(counterparty: Counterparty, onLongClick: (Counterparty) -> Unit) {
            binding.tvSupplierName.text = counterparty.name

            // Длинное нажатие → показать меню
            binding.root.setOnLongClickListener {
                val popup = PopupMenu(binding.root.context, binding.root)
                popup.menuInflater.inflate(R.menu.menu_supplier, popup.menu)
                popup.setOnMenuItemClickListener {
                    if (it.itemId == R.id.action_delete) {
                        onLongClick(counterparty) // Вызываем функцию удаления
                        true
                    } else false
                }
                popup.show()
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSupplierBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            items[position],
            onLongClick
        )
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newItems: List<Counterparty>) {
        items = newItems
        notifyDataSetChanged()
    }
}
