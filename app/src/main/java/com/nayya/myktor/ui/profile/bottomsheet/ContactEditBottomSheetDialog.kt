package com.nayya.myktor.ui.profile.bottomsheet

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
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
import com.nayya.myktor.ui.dialogs.UnsavedChangesDialogHelper
import com.nayya.myktor.ui.dialogs.showSnackbar

// BottomSheetDialogFragment для редактирования контактов контрагента
class ContactEditBottomSheetDialog : BottomSheetDialogFragment() {

    private var counterpartyId: Long = -1L

    private val contacts = mutableListOf<CounterpartyContactRequest>()
    private lateinit var adapter: ContactsAdapter
    private var onSave: ((Long, List<CounterpartyContactRequest>) -> Unit)? = null
    private val countries = mutableListOf<Country>()

    // для определения изменений на экране
    private var initialContactsSnapshot: List<CounterpartyContactRequest> = emptyList()
    private var isCancelAttemptPending = false
    private var wasSwipedDown = false

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

        val mapped = initialContacts.map {
            CounterpartyContactRequest(
                contactType = it.contactType,
                contactValue = it.contactValue,
                countryCodeId = it.countryCodeId
            )
        }

        contacts.clear()
        contacts.addAll(mapped)
        initialContactsSnapshot = mapped.map { it.copy() } // сохраняем копию
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

            if (!hasChanges()) dismiss() else saveAndClose()
        }

        binding.btnClose.setOnClickListener {
            if (hasChanges()) {
                UnsavedChangesDialogHelper.show(
                    context = requireContext(),
                    onConfirm = {
                        if (adapter.triggerValidationAndReturnValid(requireContext())) {
                            saveAndClose()
                        }
                    },
                    onDiscard = {
                        dismissAllowingStateLoss()
                    }
                )
            } else {
                dismiss()
            }
        }

        return binding.root
    }

    override fun onCancel(dialog: DialogInterface) {
        if (wasSwipedDown) {
            // ⛔️ Ничего не делаем, уже обработали в onStateChanged
            wasSwipedDown = false
            return
        }

        handleCancelAttempt()
    }


    private fun hasChanges(): Boolean {
        return contacts != initialContactsSnapshot
    }

    private fun saveAndClose() {
        onSave?.invoke(counterpartyId, contacts.toList())
        parentFragmentManager.setFragmentResult("contacts_updated", Bundle())
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)

        dialog.setCanceledOnTouchOutside(false)

        dialog.setOnShowListener { dlg ->
            val bottomSheet = (dlg as? BottomSheetDialog)
                ?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                    isHideable = true
                    isFitToContents = false
                    halfExpandedRatio = 0.7f
                    expandedOffset = dpToPx(140)

                    var lastState: Int = BottomSheetBehavior.STATE_EXPANDED

                    addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            val isSwipe = lastState == BottomSheetBehavior.STATE_SETTLING
                            lastState = newState

                            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                                if (isSwipe) {
                                    wasSwipedDown = true
                                    if (hasChanges()) {
                                        showSnackbar("Изменения не были сохранены")
                                    }
                                    dismissAllowingStateLoss()
                                } else {
                                    handleCancelAttempt()
                                }
                            }
                        }

                        override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
                    })
                }

                it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                it.requestLayout()
            }

            val outsideView = dialog.window?.decorView
                ?.findViewById<View>(com.google.android.material.R.id.touch_outside)
            outsideView?.setOnClickListener {
                handleCancelAttempt()
            }
        }

        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                handleCancelAttempt()
                true
            } else {
                false
            }
        }

        return dialog
    }

    private fun handleCancelAttempt() {
        if (!isCancelAttemptPending && hasChanges()) {
            isCancelAttemptPending = true
            UnsavedChangesDialogHelper.show(
                context = requireContext(),
                onConfirm = {
                    if (adapter.triggerValidationAndReturnValid(requireContext())) {
                        saveAndClose()
                    } else {
                        isCancelAttemptPending = false
                    }
                },
                onDiscard = {
                    isCancelAttemptPending = false
                    dismissAllowingStateLoss()
                }
            )
        } else {
            dismiss()
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
