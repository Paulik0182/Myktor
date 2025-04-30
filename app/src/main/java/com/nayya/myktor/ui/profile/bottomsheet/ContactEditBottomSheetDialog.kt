package com.nayya.myktor.ui.profile.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nayya.myktor.R
import com.nayya.myktor.databinding.BottomSheetEditContactsBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyContact
import com.nayya.myktor.domain.counterpartyentity.CounterpartyContactRequest

// BottomSheetDialogFragment для редактирования контактов контрагента
class ContactEditBottomSheetDialog : BottomSheetDialogFragment() {

    private var counterpartyId: Long = -1L

    private val contacts = mutableListOf<CounterpartyContactRequest>()
    private lateinit var adapter: ContactsAdapter
    private var onSave: ((Long, List<CounterpartyContactRequest>) -> Unit)? = null

    fun setInitialData(
        initialContacts: List<CounterpartyContact>,
        counterpartyId: Long,
        onSaveCallback: (Long, List<CounterpartyContactRequest>) -> Unit,
    ) {
        Log.d("BottomSheetInit", "Пришло ${initialContacts.size} контактов") // ← ВОТ СЮДА!

        this.counterpartyId = counterpartyId
        contacts.clear()
        contacts.addAll(
            initialContacts.map {
                CounterpartyContactRequest(
                    contactType = it.contactType,
                    contactValue = it.contactValue,
                    countryCodeId = it.countryCodeId
                )
            }
        )
        onSave = onSaveCallback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = BottomSheetEditContactsBinding.inflate(inflater, container, false)

        // Адаптер создаётся уже после заполнения contacts
        adapter = ContactsAdapter(contacts, ::onAddContact, ::onDeleteContact)
        binding.rvContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContacts.adapter = adapter

        binding.tvTitle.text = "Контакты"
        binding.tvCounter.text = "${contacts.size}/5"

        binding.btnAddContact.setOnClickListener {
            if (contacts.size >= 5) return@setOnClickListener

            contacts.add(
                CounterpartyContactRequest(
                    contactType = "phone",
                    contactValue = "",
                    countryCodeId = null
                )
            )
            adapter.notifyItemInserted(contacts.lastIndex)
            binding.tvCounter.text = "${contacts.size}/5"
        }

        binding.btnSave.setOnClickListener {
            onSave?.invoke(counterpartyId, contacts.toList())

            // Сообщаем родительскому фрагменту, что контакты обновлены
            parentFragmentManager.setFragmentResult("contacts_updated", Bundle())
            dismiss()
        }

        return binding.root
    }

    private fun onAddContact() {
        // можно опционально вызвать при нажатии из адаптера
    }

    private fun onDeleteContact(position: Int) {
        contacts.removeAt(position)
        view?.findViewById<TextView>(R.id.tvCounter)?.text = "${contacts.size}/5"
        adapter.notifyItemRemoved(position)
    }
}
