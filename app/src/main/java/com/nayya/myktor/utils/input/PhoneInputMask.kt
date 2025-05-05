package com.nayya.myktor.utils.input

import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText

object PhoneInputMask {

    private val masks = mapOf(
        "+48" to "### ### ###",     // Польша
        "+49" to "#### #######",    // Германия
        "+33" to "# ## ## ## ##",   // Франция
        "+39" to "### #######",     // Италия
        "+34" to "### ### ###",     // Испания
        "+44" to "#### ######",     // Великобритания
        "+31" to "## ### ####",     // Нидерланды
        "+32" to "### ## ## ##",    // Бельгия
        "+46" to "##-### ## ##",    // Швеция
        "+43" to "### #######"      // Австрия
    )

    private val textWatchers = mutableMapOf<EditText, TextWatcher>()

    fun applyMask(editText: EditText, countryCode: String?) {
        val rawMask = masks[countryCode] ?: "###########"
        val maxLength = rawMask.count { it == '#' }

        // Удаляем старый TextWatcher, если был
        textWatchers[editText]?.let { editText.removeTextChangedListener(it) }

        // Фильтр по длине
        editText.filters = arrayOf(InputFilter.LengthFilter(rawMask.length))
        editText.inputType = InputType.TYPE_CLASS_PHONE

        val watcher = object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true

                val digits = s?.toString()?.replace(Regex("[^\\d]"), "") ?: ""
                val formatted = formatWithMask(digits, rawMask)

                if (formatted != s.toString()) {
                    editText.setText(formatted)
                    editText.setSelection(formatted.length.coerceAtMost(editText.text.length))
                }

                isUpdating = false
            }
        }

        editText.addTextChangedListener(watcher)
        textWatchers[editText] = watcher
    }

    fun clearMask(editText: EditText) {
        textWatchers[editText]?.let {
            editText.removeTextChangedListener(it)
            textWatchers.remove(editText)
        }
        editText.filters = arrayOf()
        editText.inputType = InputType.TYPE_CLASS_TEXT
    }

    private fun formatWithMask(digits: String, mask: String): String {
        val result = StringBuilder()
        var digitIndex = 0

        for (char in mask) {
            if (char == '#') {
                if (digitIndex < digits.length) {
                    result.append(digits[digitIndex])
                    digitIndex++
                } else {
                    break
                }
            } else {
                if (digitIndex < digits.length) {
                    result.append(char)
                } else {
                    break
                }
            }
        }

        return result.toString()
    }
}
