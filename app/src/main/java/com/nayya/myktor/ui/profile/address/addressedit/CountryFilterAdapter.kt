package com.nayya.myktor.ui.profile.address.addressedit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.nayya.myktor.R
import com.nayya.myktor.domain.counterpartyentity.Country

class CountryFilterAdapter(
    context: Context,
    private val countries: List<Country>
) : ArrayAdapter<Country>(context, android.R.layout.simple_dropdown_item_1line, countries), Filterable {

    private var filteredCountries: List<Country> = countries

    override fun getCount(): Int = filteredCountries.size
    override fun getItem(position: Int): Country = filteredCountries[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        (view as TextView).text = getItem(position).name
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                results.values = if (constraint.isNullOrBlank()) {
                    countries
                } else {
                    countries.filter {
                        it.name.contains(constraint, ignoreCase = true)
                    }
                }
                results.count = (results.values as List<*>).size
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredCountries = results?.values as? List<Country> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
}