package com.nayya.myktor.ui.profile.detailscounterparty

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import com.nayya.myktor.R
import com.nayya.myktor.databinding.LayoutLegalEntityBinding
import com.nayya.myktor.databinding.PersonNameFieldsBinding
import com.nayya.myktor.utils.input.InputValidator
import com.nayya.uicomponents.BottomTextState

class CounterpartyValidationDelegate(
    private val context: Context,
    private val viewModel: CounterpartyDetailsViewModel,
    private val binding: PersonNameFieldsBinding,
    private val legalEntityBinding: LayoutLegalEntityBinding
) {

    fun setupAll() {
        setupShortNameValidation()
        setupFirstNameValidation()
        setupLastNameValidation()
        setupCompanyNameValidation()
        setupNIPValidation()
        setupKRSValidation()
    }

    private fun setupShortNameValidation() {
        val field = binding.ccavShortName
        field.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val original = s?.toString() ?: ""
                val cursorPosition = field.getSelection()

                // Удаляем переносы строк, заменяем множественные пробелы на один, но не трогаем крайние пробелы
                var cleaned = original
                    .replace("\n", "") // убираем переносы строк
                    .replace(Regex(" {2,}"), " ") // заменяем 2+ пробелов на 1

                // Если изменили текст — обновляем поле
                if (cleaned != original) {
                    field.text = cleaned
                    field.setSelection(minOf(cursorPosition, cleaned.length))
                }

                // Для валидации обрезаем пробелы по краям (НЕ В field.text!)
                val trimmedText = cleaned.trim()

                val error = when {
                    InputValidator.validateEmpty(context, trimmedText) != null ->
                        InputValidator.validateEmpty(context, trimmedText)

                    InputValidator.validateLength(context, trimmedText, 20) != null ->
                        InputValidator.validateLength(context, trimmedText, 20)

                    InputValidator.validateMinAllowedInitialLength(
                        context,
                        trimmedText,
                        5
                    ) != null ->
                        InputValidator.validateMinAllowedInitialLength(context, trimmedText, 5)

                    InputValidator.validateStartingDot(context, trimmedText) != null ->
                        InputValidator.validateStartingDot(context, trimmedText)

                    InputValidator.validateStartingOrEndingSpace(context, trimmedText) != null ->
                        InputValidator.validateStartingOrEndingSpace(context, trimmedText)

                    InputValidator.validateLineBreaksAndCharacters(context, trimmedText) != null ->
                        InputValidator.validateLineBreaksAndCharacters(context, trimmedText)

                    InputValidator.validateCharacters(context, trimmedText) != null ->
                        InputValidator.validateCharacters(context, trimmedText)

                    InputValidator.validateName(context, trimmedText) != null ->
                        InputValidator.validateName(context, trimmedText)

                    else -> null
                }

                // ✅ ВАЖНО: обновляем валидность во ViewModel
                viewModel.setCompanyNameValid(error == null)

                if (viewModel.isEditMode.value == true) {
                    if (error != null) {
                        field.setBottomTextState(
                            BottomTextState.Error(
                                showErrorText = true,
                                showErrorIcon = true,
                                errorText = error
                            )
                        )
                    } else {
                        val remaining = 20 - trimmedText.length
                        if (remaining in 0..20) {
                            field.setBottomTextState(
                                BottomTextState.Description(
                                    showDescriptionText = true,
                                    descriptionText = context.resources.getQuantityString(
                                        R.plurals.remaining_characters,
                                        remaining,
                                        remaining
                                    )
                                )
                            )
                        } else {
                            field.setBottomTextState(BottomTextState.Empty)
                        }
                    }
                }
                isEditing = false
            }
        })
    }

    private fun setupFirstNameValidation() {
        val field = binding.ccavFirstName
        field.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val original = s?.toString() ?: ""
                val cursorPosition = field.getSelection()

                // Удаляем переносы строк, заменяем множественные пробелы на один, но не трогаем крайние пробелы
                var cleaned = original
                    .replace("\n", "") // убираем переносы строк
                    .replace(Regex(" {2,}"), " ") // заменяем 2+ пробелов на 1

                // Если изменили текст — обновляем поле
                if (cleaned != original) {
                    field.text = cleaned
                    field.setSelection(minOf(cursorPosition, cleaned.length))
                }

                // Для валидации обрезаем пробелы по краям (НЕ В field.text!)
                val trimmedText = cleaned.trim()

                val error = when {
                    InputValidator.validateLength(context, trimmedText, 35) != null ->
                        InputValidator.validateLength(context, trimmedText, 35)

                    InputValidator.validateStartingDot(context, trimmedText) != null ->
                        InputValidator.validateStartingDot(context, trimmedText)

                    InputValidator.validateEndDot(context, trimmedText) != null ->
                        InputValidator.validateEndDot(context, trimmedText)

                    InputValidator.validateStartingOrEndingSpace(context, trimmedText) != null ->
                        InputValidator.validateStartingOrEndingSpace(context, trimmedText)

                    InputValidator.validateLineBreaksAndCharacters(context, trimmedText) != null ->
                        InputValidator.validateLineBreaksAndCharacters(context, trimmedText)

                    InputValidator.validateOnlyLettersDashAndSpace(context, trimmedText) != null ->
                        InputValidator.validateOnlyLettersDashAndSpace(context, trimmedText)

                    InputValidator.validateName(context, trimmedText) != null ->
                        InputValidator.validateName(context, trimmedText)

                    else -> null
                }

                // ✅ ВАЖНО: обновляем валидность во ViewModel
                viewModel.setCompanyNameValid(error == null)

                if (viewModel.isEditMode.value == true) {
                    if (error != null) {
                        field.setBottomTextState(
                            BottomTextState.Error(
                                showErrorText = true,
                                showErrorIcon = true,
                                errorText = error
                            )
                        )
                    } else {
                        val remaining = 35 - trimmedText.length
                        if (remaining in 2..35) {
                            field.setBottomTextState(
                                BottomTextState.Description(
                                    showDescriptionText = true,
                                    descriptionText = context.resources.getQuantityString(
                                        R.plurals.remaining_characters,
                                        remaining,
                                        remaining
                                    )
                                )
                            )
                        } else {
                            field.setBottomTextState(BottomTextState.Empty)
                        }
                    }
                }
                isEditing = false
            }
        })
    }

    private fun setupLastNameValidation() {
        val field = binding.ccavLastName
        field.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val original = s?.toString() ?: ""
                val cursorPosition = field.getSelection()

                // Удаляем переносы строк, заменяем множественные пробелы на один, но не трогаем крайние пробелы
                var cleaned = original
                    .replace("\n", "") // убираем переносы строк
                    .replace(Regex(" {2,}"), " ") // заменяем 2+ пробелов на 1

                // Если изменили текст — обновляем поле
                if (cleaned != original) {
                    field.text = cleaned
                    field.setSelection(minOf(cursorPosition, cleaned.length))
                }

                // Для валидации обрезаем пробелы по краям (НЕ В field.text!)
                val trimmedText = cleaned.trim()

                val error = when {

                    InputValidator.validateLength(context, trimmedText, 35) != null ->
                        InputValidator.validateLength(context, trimmedText, 35)

                    InputValidator.validateStartingDot(context, trimmedText) != null ->
                        InputValidator.validateStartingDot(context, trimmedText)

                    InputValidator.validateEndDot(context, trimmedText) != null ->
                        InputValidator.validateEndDot(context, trimmedText)

                    InputValidator.validateStartingOrEndingSpace(context, trimmedText) != null ->
                        InputValidator.validateStartingOrEndingSpace(context, trimmedText)

                    InputValidator.validateLineBreaksAndCharacters(context, trimmedText) != null ->
                        InputValidator.validateLineBreaksAndCharacters(context, trimmedText)

                    InputValidator.validateOnlyLettersDashAndSpace(context, trimmedText) != null ->
                        InputValidator.validateOnlyLettersDashAndSpace(context, trimmedText)

                    InputValidator.validateName(context, trimmedText) != null ->
                        InputValidator.validateName(context, trimmedText)

                    else -> null
                }

                // ✅ ВАЖНО: обновляем валидность во ViewModel
                viewModel.setCompanyNameValid(error == null)

                if (viewModel.isEditMode.value == true) {
                    if (error != null) {
                        field.setBottomTextState(
                            BottomTextState.Error(
                                showErrorText = true,
                                showErrorIcon = true,
                                errorText = error
                            )
                        )
                    } else {
                        val remaining = 35 - trimmedText.length
                        if (remaining in 2..35) {
                            field.setBottomTextState(
                                BottomTextState.Description(
                                    showDescriptionText = true,
                                    descriptionText = context.resources.getQuantityString(
                                        R.plurals.remaining_characters,
                                        remaining,
                                        remaining
                                    )
                                )
                            )
                        } else {
                            field.setBottomTextState(BottomTextState.Empty)
                        }
                    }
                }
                isEditing = false
            }
        })
    }

    private fun setupCompanyNameValidation() {
        val field = legalEntityBinding.ccavCompanyName
        field.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val original = s?.toString() ?: ""
                val cursorPosition = field.getSelection()

                // Удаляем переносы строк, заменяем множественные пробелы на один, но не трогаем крайние пробелы
                var cleaned = original
                    .replace("\n", "") // убираем переносы строк
                    .replace(Regex(" {2,}"), " ") // заменяем 2+ пробелов на 1

                // Если изменили текст — обновляем поле
                if (cleaned != original) {
                    field.text = cleaned
                    field.setSelection(minOf(cursorPosition, cleaned.length))
                }

                // Для валидации обрезаем пробелы по краям (НЕ В field.text!)
                val trimmedText = cleaned.trim()

                val error = when {
                    InputValidator.validateEmpty(context, trimmedText) != null ->
                        InputValidator.validateEmpty(context, trimmedText)

                    InputValidator.validateLength(context, trimmedText, 90) != null ->
                        InputValidator.validateLength(context, trimmedText, 90)

                    InputValidator.validateMinAllowedInitialLength(context, trimmedText) != null ->
                        InputValidator.validateMinAllowedInitialLength(context, trimmedText)

                    InputValidator.validateStartingDot(context, trimmedText) != null ->
                        InputValidator.validateStartingDot(context, trimmedText)

                    InputValidator.validateStartingOrEndingSpace(context, trimmedText) != null ->
                        InputValidator.validateStartingOrEndingSpace(context, trimmedText)

                    InputValidator.validateLineBreaksAndCharacters(context, trimmedText) != null ->
                        InputValidator.validateLineBreaksAndCharacters(context, trimmedText)

                    InputValidator.validateCharacters(context, trimmedText) != null ->
                        InputValidator.validateCharacters(context, trimmedText)

                    InputValidator.validateName(context, trimmedText) != null ->
                        InputValidator.validateName(context, trimmedText)

                    else -> null
                }

                // ✅ ВАЖНО: обновляем валидность во ViewModel
                viewModel.setCompanyNameValid(error == null)

                if (viewModel.isEditMode.value == true) {
                    if (error != null) {
                        field.setBottomTextState(
                            BottomTextState.Error(
                                showErrorText = true,
                                showErrorIcon = true,
                                errorText = error
                            )
                        )
                    } else {
                        val remaining = 90 - trimmedText.length
                        if (remaining in 0..90) {
                            field.setBottomTextState(
                                BottomTextState.Description(
                                    showDescriptionText = true,
                                    descriptionText = context.resources.getQuantityString(
                                        R.plurals.remaining_characters,
                                        remaining,
                                        remaining
                                    )
                                )
                            )
                        } else {
                            field.setBottomTextState(BottomTextState.Empty)
                        }
                    }
                }
                isEditing = false
            }
        })
    }

    private fun setupNIPValidation() {
        legalEntityBinding.ccavNIP.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                var text = s?.toString().orEmpty()

                // Удаляем пробелы и переносы строк
                text = text.replace("\\s".toRegex(), "")

                // Применяем очищенный текст, если был изменён
                if (text != legalEntityBinding.ccavNIP.text) {
                    legalEntityBinding.ccavNIP.text = text
                    legalEntityBinding.ccavNIP.setSelection(text.length)
                }

                val error = when {
                    text.length > 10 ->
                        context.getString(R.string.error_max_length, 10)

                    !text.all { it.isDigit() } ->
                        context.getString(R.string.error_digits_only)

                    else -> null
                }

                // ✅ ВАЖНО: обновляем валидность во ViewModel
                // валидности — true, только если длина = 10 и все цифры
                viewModel.setNipValid(text.length == 10 && text.all { it.isDigit() })

                if (viewModel.isEditMode.value == true) {
                    if (text.isBlank()) {
                        legalEntityBinding.ccavNIP.setBottomTextState(
                            BottomTextState.Description(
                                showDescriptionText = true,
                                descriptionText = context.getString(R.string.exact_length, 10)
                            )
                        )
                    } else if (error != null) {
                        legalEntityBinding.ccavNIP.setBottomTextState(
                            BottomTextState.Error(
                                showErrorText = true,
                                showErrorIcon = true,
                                errorText = error
                            )
                        )
                    } else {
                        val remaining = 10 - text.length
                        legalEntityBinding.ccavNIP.setBottomTextState(
                            BottomTextState.Description(
                                showDescriptionText = true,
                                descriptionText = context.resources.getQuantityString(
                                    R.plurals.remaining_characters,
                                    remaining,
                                    remaining
                                )
                            )
                        )
                    }
                }
                isEditing = false
            }
        })
    }

    private fun setupKRSValidation() {
        legalEntityBinding.ccavKRS.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                var text = s?.toString().orEmpty()

                // Удаляем пробелы и переносы строк
                text = text.replace("\\s".toRegex(), "")

                // Применяем очищенный текст, если был изменён
                if (text != legalEntityBinding.ccavKRS.text) {
                    legalEntityBinding.ccavKRS.text = text
                    legalEntityBinding.ccavKRS.setSelection(text.length)
                }

                val error = when {
                    text.length > 10 ->
                        context.getString(R.string.error_max_length, 10)

                    !text.all { it.isDigit() } ->
                        context.getString(R.string.error_digits_only)

                    else -> null
                }

                // ✅ ВАЖНО: обновляем валидность во ViewModel
                // валидности — true, только если длина = 10 и все цифры
                viewModel.setKrsValid(text.length == 10 && text.all { it.isDigit() })

                if (viewModel.isEditMode.value == true) {
                    if (text.isBlank()) {
                        legalEntityBinding.ccavKRS.setBottomTextState(
                            BottomTextState.Description(
                                showDescriptionText = true,
                                descriptionText = context.getString(R.string.exact_length, 10)
                            )
                        )
                    } else if (error != null) {
                        legalEntityBinding.ccavKRS.setBottomTextState(
                            BottomTextState.Error(
                                showErrorText = true,
                                showErrorIcon = true,
                                errorText = error
                            )
                        )
                    } else {
                        val remaining = 10 - text.length
                        legalEntityBinding.ccavKRS.setBottomTextState(
                            BottomTextState.Description(
                                showDescriptionText = true,
                                descriptionText = context.resources.getQuantityString(
                                    R.plurals.remaining_characters,
                                    remaining,
                                    remaining
                                )
                            )
                        )
                    }
                }
                isEditing = false
            }
        })
    }


    private fun validateNIPAndKRS(text: String): String? {
        if (text.isBlank()) return null  // ⬅️ поле не обязательно, пропускаем

        return when {
            text.length > 10 -> context.getString(R.string.error_max_length, 10)
            !text.all { it.isDigit() } -> context.getString(R.string.error_digits_only)
            text.length < 10 -> context.getString(R.string.error_length, 10)
            else -> null
        }
    }

    fun validateNIPOnSave(): Boolean {
        val text = legalEntityBinding.ccavNIP.text.orEmpty()
        val error = validateNIPAndKRS(text)
        val isValid = error == null

        viewModel.setNipValid(isValid)

        if (!isValid) {
            legalEntityBinding.ccavNIP.setBottomTextState(
                BottomTextState.Error(
                    showErrorText = true,
                    showErrorIcon = true,
                    errorText = requireNotNull(error)
                )
            )
        }

        return isValid
    }

    fun validateKRSOnSave(): Boolean {
        val text = legalEntityBinding.ccavKRS.text.orEmpty()
        val error = validateNIPAndKRS(text)
        val isValid = error == null

        viewModel.setKrsValid(isValid)

        if (!isValid) {
            legalEntityBinding.ccavKRS.setBottomTextState(
                BottomTextState.Error(
                    showErrorText = true,
                    showErrorIcon = true,
                    errorText = requireNotNull(error)
                )
            )
        }

        return isValid
    }

    fun runInitialValidations() {

        binding.ccavShortName.text = binding.ccavShortName.text
        binding.ccavFirstName.text = binding.ccavFirstName.text
        binding.ccavLastName.text = binding.ccavLastName.text
        legalEntityBinding.ccavCompanyName.text = legalEntityBinding.ccavCompanyName.text
        legalEntityBinding.ccavNIP.text = legalEntityBinding.ccavNIP.text
        legalEntityBinding.ccavKRS.text = legalEntityBinding.ccavKRS.text

        viewModel.setHasUnsavedChanges(false)
    }
}