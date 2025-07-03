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
import com.nayya.myktor.domain.counterpartyentity.City

class CityFilterAdapter(
    context: Context,
    private val cities: List<City>
) : ArrayAdapter<City>(context, android.R.layout.simple_dropdown_item_1line, cities), Filterable {

    private var filteredCities: List<City> = cities

    override fun getCount(): Int = filteredCities.size
    override fun getItem(position: Int): City = filteredCities[position]

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
                    cities
                } else {
                    cities.filter {
                        it.name.contains(constraint, ignoreCase = true)
                    }
                }
                results.count = (results.values as List<*>).size
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredCities = results?.values as? List<City> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
}
