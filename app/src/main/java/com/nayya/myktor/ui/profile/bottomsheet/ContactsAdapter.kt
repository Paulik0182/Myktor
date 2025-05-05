package com.nayya.myktor.ui.profile.bottomsheet

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.recyclerview.widget.RecyclerView
import com.nayya.myktor.R
import com.nayya.myktor.databinding.ItemContactEditBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyContactRequest
import com.nayya.myktor.domain.counterpartyentity.Country
import com.nayya.myktor.utils.input.PhoneInputMask

class ContactsAdapter(
    private val contacts: List<CounterpartyContactRequest>,
    private val countries: List<Country>,
    private val onAdd: () -> Unit,
    private val onDelete: (Int) -> Unit,
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(val binding: ItemContactEditBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var currentWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding =
            ItemContactEditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]

        with(holder.binding) {
            val context = holder.itemView.context

            // ❗ Удаляем старый TextWatcher
            holder.currentWatcher?.let {
                etContactValue.removeTextChangedListener(it)
            }

            // Устанавливаем значение
            etContactValue.setText(contact.contactValue ?: "")

            // Устанавливаем inputType
            etContactValue.inputType = when (contact.contactType) {
                "email" -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                else -> InputType.TYPE_CLASS_PHONE
            }

            // Новый TextWatcher
            val watcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    contact.contactValue = s?.toString()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

            etContactValue.addTextChangedListener(watcher)
            holder.currentWatcher = watcher

            // Тип контакта (spinnerContactType)
            val adapter = ArrayAdapter.createFromResource(
                context,
                R.array.contact_type_entries,
                R.layout.spinner_selected_item_white
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            spinnerContactType.adapter = adapter

            // Установка типа
            val contactTypePosition = if (contact.contactType == "email") 1 else 0
            if (spinnerContactType.selectedItemPosition != contactTypePosition) {
                spinnerContactType.setSelection(contactTypePosition, false)
            }

            // Обработка выбора типа
            spinnerContactType.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        pos: Int,
                        id: Long,
                    ) {
                        val selectedType = when (pos) {
                            0 -> "phone"   // Tel
                            1 -> "email"   // Email
                            2 -> "fax"     // Fax
                            else -> "other" // Другое
                        }

                        val safeTypes = setOf("phone", "fax")

                        if (contact.contactType != selectedType) {
                            val wasSafe = contact.contactType in safeTypes
                            val isSafe = selectedType in safeTypes

                            contact.contactType = selectedType

                            if (!(wasSafe && isSafe)) {
                                contact.contactValue = ""
                                contact.countryCodeId = null
                                etContactValue.setText("")
                                spinnerCountryCode.setSelection(0)
                            }
                        }

                        spinnerCountryCode.visibility =
                            if (contact.contactType == "phone" || contact.contactType == "fax")
                                View.VISIBLE else View.GONE

                        // Применяем маску
                        if (contact.contactType == "phone" || contact.contactType == "fax") {
                            val code = countries.getOrNull(spinnerCountryCode.selectedItemPosition)?.phoneCode
                            PhoneInputMask.applyMask(etContactValue, code)
                        } else {
                            PhoneInputMask.clearMask(etContactValue)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

            // Страны
            val countryAdapter = CountrySpinnerAdapter(context, countries)
            spinnerCountryCode.adapter = countryAdapter

            // ✅ Ограничение высоты выпадающего списка (5 строк по 40dp = ~200dp)
            spinnerCountryCode.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    try {
                        val field = AppCompatSpinner::class.java.getDeclaredField("mPopup")
                        field.isAccessible = true
                        val popup = field.get(spinnerCountryCode)
                        val popupClass =
                            Class.forName("androidx.appcompat.widget.AppCompatSpinner\$DropdownPopup")
                        if (popupClass.isInstance(popup)) {
                            val method = popupClass.getMethod("setHeight", Int::class.java)
                            method.invoke(popup, dpToPx(250, context)) // ← высота в dp
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        spinnerCountryCode.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            })

            // Установка выбранной страны
            val selectedIndex = countries.indexOfFirst { it.id == contact.countryCodeId }
            if (selectedIndex != -1) {
                spinnerCountryCode.setSelection(selectedIndex)
            }

            // Обработка выбора страны
            spinnerCountryCode.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        pos: Int,
                        id: Long,
                    ) {
                        contact.countryCodeId = countries[pos].id

                        if (contact.contactType == "phone" || contact.contactType == "fax") {
                            val code = countries[pos].phoneCode
                            PhoneInputMask.applyMask(etContactValue, code)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

            // Удаление
            btnRemove.setOnClickListener {
                val adapterPosition = holder.bindingAdapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onDelete(adapterPosition)
                }
            }
        }
    }

    private fun dpToPx(dp: Int, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
