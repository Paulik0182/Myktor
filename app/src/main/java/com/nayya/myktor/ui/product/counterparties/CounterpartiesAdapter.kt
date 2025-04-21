package com.nayya.myktor.ui.product.counterparties

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.nayya.myktor.R
import com.nayya.myktor.databinding.ItemSupplierBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity

class CounterpartiesAdapter(
    private var items: List<CounterpartyEntity>,
    private val onItemClick: (CounterpartyEntity) -> Unit,
    private val onLongClick: (CounterpartyEntity) -> Unit,
) : RecyclerView.Adapter<CounterpartiesAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemSupplierBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            counterpartyEntity: CounterpartyEntity,
            onItemClick: (CounterpartyEntity) -> Unit,
            onLongClick: (CounterpartyEntity) -> Unit,
        ) {
            Log.d("SUPPLIERS", "✅ ЛОГ 5 Отображается контрагент: ${counterpartyEntity.shortName}")

            try {
                val firstName = counterpartyEntity.firstName
                val lastName = counterpartyEntity.lastName

                binding.tvSupplierName.text =
                    counterpartyEntity.companyName ?: "$firstName $lastName"
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvSupplierName.text = "Неизвестный контрагент"
            }

            binding.root.setOnClickListener {
                onItemClick(counterpartyEntity)
            }

            // Длинное нажатие → показать меню
            binding.root.setOnLongClickListener {
                val popup = PopupMenu(binding.root.context, binding.root)
                popup.menuInflater.inflate(R.menu.menu_supplier, popup.menu)
                popup.setOnMenuItemClickListener {
                    if (it.itemId == R.id.action_delete) {
                        onLongClick(counterpartyEntity) // Вызываем функцию удаления
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
            onItemClick,
            onLongClick
        )
    }

    override fun getItemCount(): Int {
        val count = items.size
        Log.d("SUPPLIERS", "✅ ЛОГ 4 Adapter getItemCount: $count")
        return count
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newItems: List<CounterpartyEntity>) {
        Log.d("SUPPLIERS", "✅ ЛОГ 3 Adapter получил ${newItems.size} элементов")

        items = newItems
        notifyDataSetChanged()
    }
}
