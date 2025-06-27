package com.nayya.myktor.ui.profile.address.addressedit

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.nayya.myktor.domain.counterpartyentity.City
import com.nayya.myktor.domain.counterpartyentity.Country

class AddressAutoCompleteAdapter(
    context: Context,
    private val items: List<Any>
) : ArrayAdapter<Any>(context, android.R.layout.simple_dropdown_item_1line, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)

        when (val item = getItem(position)) {
            is Country -> textView.text = item.name
            is City -> textView.text = item.name
        }

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filteredList = if (constraint.isNullOrEmpty()) {
                    items
                } else {
                    items.filter {
                        when (it) {
                            is Country -> it.name.contains(constraint, true)
                            is City -> it.name.contains(constraint, true)
                            else -> false
                        }
                    }
                }
                results.values = filteredList
                results.count = filteredList.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                // Обрабатываем случай, когда results null или values null
                if (results == null || results.values == null) {
                    clear()
                    notifyDataSetChanged()
                    return
                }

                try {
                    clear()
                    @Suppress("UNCHECKED_CAST")
                    val filteredItems = results.values as? List<Any>
                    if (filteredItems != null) {
                        addAll(filteredItems)
                    }
                } catch (e: Exception) {
                    Log.e("Filter", "Error updating filtered results", e)
                } finally {
                    notifyDataSetChanged()
                }
            }
        }
    }
}