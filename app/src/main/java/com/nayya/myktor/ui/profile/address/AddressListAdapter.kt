package com.nayya.myktor.ui.profile.address

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nayya.myktor.databinding.ItemAddressBinding
import com.nayya.myktor.ui.profile.address.addressedit.AddressUiModel

class AddressListAdapter(
    private val onEdit: (AddressUiModel) -> Unit,
    private val onSetMain: (AddressUiModel) -> Unit,
) : ListAdapter<AddressUiModel, AddressListAdapter.AddressViewHolder>(DiffCallback()) {

    private var isSwiping = false

    fun setSwiping(swiping: Boolean) {
        isSwiping = swiping
    }

    inner class AddressViewHolder(private val binding: ItemAddressBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(address: AddressUiModel) = with(binding) {
            tvRecipient.text = address.fullName

            val streetParts = mutableListOf<String>()
            streetParts += address.street
            if (!address.houseNumber.isNullOrBlank()) streetParts += address.houseNumber
            if (!address.locationNumber.isNullOrBlank()) streetParts += address.locationNumber
            binding.tvStreet.text = streetParts.joinToString(", ")

            val cityParts = listOf(
                address.country,
                address.postalCode,
                address.city
            ).filter { !it.isNullOrBlank() }
            binding.tvCity.text = cityParts.joinToString(", ")

            checkBoxMain.setOnCheckedChangeListener(null) // важно сбросить старый listener
            checkBoxMain.isChecked = address.isMain
            checkBoxMain.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !address.isMain && !isSwiping) { // только если состояние действительно изменилось
                    onSetMain(address)
                }
            }

            checkBoxMain.isChecked = address.isMain

            cardContainer.setOnClickListener {
                onEdit(address)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAddressBinding.inflate(inflater, parent, false)
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<AddressUiModel>() {
        override fun areItemsTheSame(oldItem: AddressUiModel, newItem: AddressUiModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: AddressUiModel, newItem: AddressUiModel) =
            oldItem == newItem
    }
}
