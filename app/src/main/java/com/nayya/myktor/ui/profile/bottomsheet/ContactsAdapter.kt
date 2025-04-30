package com.nayya.myktor.ui.profile.bottomsheet

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.nayya.myktor.databinding.ItemContactEditBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyContactRequest

class ContactsAdapter(
    private val contacts: List<CounterpartyContactRequest>,
    private val onAdd: () -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(val binding: ItemContactEditBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var currentWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactEditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]

        with(holder.binding) {
            // ❗ Удаляем предыдущий TextWatcher
            holder.currentWatcher?.let {
                etContactValue.removeTextChangedListener(it)
            }

            // Устанавливаем текст
            etContactValue.setText(contact.contactValue ?: "")

            // Устанавливаем inputType
            etContactValue.inputType = when (contact.contactType) {
                "email" -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                else -> InputType.TYPE_CLASS_PHONE
            }

            // ✅ Создаем и сохраняем TextWatcher
            val watcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    contact.contactValue = s?.toString()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

            etContactValue.addTextChangedListener(watcher)
            holder.currentWatcher = watcher

            // Устанавливаем тип контакта (email / phone)
            spinnerContactType.setSelection(
                when (contact.contactType) {
                    "email" -> 1
                    else -> 0
                }
            )

            // Слушатель выбора
            spinnerContactType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    val adapterPosition = holder.bindingAdapterPosition
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        val selectedType = if (pos == 1) "email" else "phone"
                        contacts[adapterPosition].contactType = selectedType
                        notifyItemChanged(adapterPosition)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            // Кнопка удаления
            btnRemove.setOnClickListener {
                val adapterPosition = holder.bindingAdapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onDelete(adapterPosition)
                }
            }
        }
    }
}
