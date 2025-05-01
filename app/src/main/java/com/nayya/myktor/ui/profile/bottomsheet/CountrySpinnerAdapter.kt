package com.nayya.myktor.ui.profile.bottomsheet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.nayya.myktor.R
import com.nayya.myktor.domain.counterpartyentity.Country

class CountrySpinnerAdapter(
    context: Context,
    private val countries: List<Country>
) : ArrayAdapter<Country>(context, R.layout.spinner_selected_item_white, countries) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Видимый элемент: показываем только код (например, "+48")
        val view = super.getView(position, convertView, parent)
        (view as TextView).text = "${countries[position].phoneCode}"
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        // В выпадающем списке — название и код
        val view = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        (view.findViewById(android.R.id.text1) as TextView).text =
            "${countries[position].name} (${countries[position].phoneCode})"
        return view
    }
}
