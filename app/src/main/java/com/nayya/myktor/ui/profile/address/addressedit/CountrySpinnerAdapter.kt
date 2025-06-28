package com.nayya.myktor.ui.profile.address.addressedit

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.nayya.myktor.R
import com.nayya.myktor.domain.counterpartyentity.Country

class CountrySpinnerAdapter(
    context: Context,
    countries: List<Country>
) : ArrayAdapter<Country>(context, R.layout.spinner_item_text_large, countries) {
    init {
        setDropDownViewResource(R.layout.spinner_item_text_large)
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        (view as TextView).text = getItem(position)?.name ?: ""
        return view
    }
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        (view as TextView).text = getItem(position)?.name ?: ""
        return view
    }
}
