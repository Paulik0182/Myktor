package com.nayya.myktor.ui.profile.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nayya.myktor.R
import com.nayya.myktor.databinding.BottomSheetEditContactsBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyContact
import com.nayya.myktor.domain.counterpartyentity.CounterpartyContactRequest
import com.nayya.myktor.domain.counterpartyentity.Country

// BottomSheetDialogFragment для редактирования контактов контрагента
class ContactEditBottomSheetDialog : BottomSheetDialogFragment() {

    private var counterpartyId: Long = -1L

    private val contacts = mutableListOf<CounterpartyContactRequest>()
    private lateinit var adapter: ContactsAdapter
    private var onSave: ((Long, List<CounterpartyContactRequest>) -> Unit)? = null
    private val countries = mutableListOf<Country>()

    fun setInitialData(
        initialContacts: List<CounterpartyContact>,
        counterpartyId: Long,
        countries: List<Country>,
        onSaveCallback: (Long, List<CounterpartyContactRequest>) -> Unit,
    ) {
        Log.d("BottomSheetInit", "Пришло ${initialContacts.size} контактов") // ← ВОТ СЮДА!

        this.counterpartyId = counterpartyId
        this.countries.clear()
        this.countries.addAll(countries)

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
        adapter = ContactsAdapter(contacts, countries, ::onAddContact, ::onDeleteContact)
        binding.rvContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContacts.adapter = adapter

        // 🔸 Передаем recyclerView в адаптер. Немного нарушаем, но это архитектурный компромисс!
        adapter.attachRecyclerView(binding.rvContacts)

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
            val isValid = adapter.triggerValidationAndReturnValid(requireContext())

            if (!isValid) {
                return@setOnClickListener // ❌ ошибки есть, не сохраняем
            }

            onSave?.invoke(counterpartyId, contacts.toList())

            // Сообщаем родительскому фрагменту, что контакты обновлены
            parentFragmentManager.setFragmentResult("contacts_updated", Bundle())
            dismiss()
        }

//        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener { dialog ->
                val bottomSheetDialog = dialog as? BottomSheetDialog
                val bottomSheet = bottomSheetDialog
                    ?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

                bottomSheet?.let {
                    // Отключаем промежуточные состояния
                    val behavior = BottomSheetBehavior.from(it).apply {
                        state = BottomSheetBehavior.STATE_EXPANDED
                        skipCollapsed = true
                        isHideable = true
                        isFitToContents = false
                        halfExpandedRatio = 0.7f // просто дефолт, не используем
                        expandedOffset = dpToPx(140) // ← нужный отступ
                    }

                    // Ограничиваем высоту вручную (если требуется)
                    it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    it.requestLayout()
                }
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
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
