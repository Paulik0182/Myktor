package com.nayya.myktor.ui.profile.address.addressedit

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentAddressEditBinding
import com.nayya.myktor.utils.input.InputValidator
import com.nayya.uicomponents.BottomTextState
import com.nayya.uicomponents.CustomCardActionView

class AddressFieldsValidationDelegate(
    private val context: Context,
    private val binding: FragmentAddressEditBinding,
    private val viewModel: AddressEditViewModel,
) {
    fun setupAll() {
        setupRecipientNameValidation()
        setupPostalCodeValidation()
        setupStreetValidation()
        setupHouseNumberValidation()
        setupLocationNumberValidation()
        setupEntranceNumberValidation()
        setupFloorValidation()
        setupNumberIntercomValidation()
    }

    // --- fullName ---
    private fun setupRecipientNameValidation() {
        val field = binding.ccavRecipientName
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

                // Удаляем переносы строк, заменяем множественные пробелы на один
                var cleaned = original
                    .replace("\n", "")
                    .replace(Regex(" {2,}"), " ")
                if (cleaned != original) {
                    field.text = cleaned
                    field.setSelection(minOf(cursorPosition, cleaned.length))
                }
                val trimmedText = cleaned.trim()

                viewModel.updateForm { copy(recipientName = trimmedText) }

                val error = when {
                    InputValidator.validateEmpty(context, trimmedText) != null ->
                        InputValidator.validateEmpty(context, trimmedText)

                    InputValidator.validateLength(context, trimmedText, 100) != null ->
                        InputValidator.validateLength(context, trimmedText, 100)

                    InputValidator.validateMinAllowedInitialLength(
                        context,
                        trimmedText,
                        5
                    ) != null ->
                        InputValidator.validateMinAllowedInitialLength(context, trimmedText, 5)

                    InputValidator.validateByPattern(
                        context = context,
                        text = trimmedText,
                        pattern = ALLOWED_CHARACTERS_REGEX
                    ) != null ->
                        InputValidator.validateByPattern(
                            context = context,
                            text = trimmedText,
                            pattern = ALLOWED_CHARACTERS_REGEX
                        )

                    InputValidator.validateOnlySingleSpaces(context, trimmedText) != null ->
                        InputValidator.validateOnlySingleSpaces(context, trimmedText)

                    InputValidator.validateNoLeadingTrailingSpace(context, trimmedText) != null ->
                        InputValidator.validateNoLeadingTrailingSpace(context, trimmedText)

                    InputValidator.validateNoLineBreaks(context, trimmedText) != null ->
                        InputValidator.validateNoLineBreaks(context, trimmedText)

                    else -> null
                }

                // Обновляем валидность во ViewModel
                viewModel.setRecipientNameValid(error == null)

                // UI
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
                        val remaining = 100 - trimmedText.length
                        if (remaining in 0..100) // TODO тут нужно чтобы подсчет велся не от нуля , чтобы показывалось что поле обязательное
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
                        else
                            field.setBottomTextState(BottomTextState.Empty)
                    }
                }
                isEditing = false
            }
        })
    }

    // --- postalCode ---
    private fun setupPostalCodeValidation() {
        val field = binding.ccavPostalCode
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
                var cleaned = original
                    .replace("\n", "")
                    .replace(Regex(" {2,}"), " ")
                if (cleaned != original) {
                    field.text = cleaned
                    field.setSelection(minOf(cursorPosition, cleaned.length))
                }
                val trimmedText = cleaned.trim()

                viewModel.updateForm { copy(postalCode = trimmedText) }

                val error = when {
                    InputValidator.validateEmpty(context, trimmedText) != null ->
                        InputValidator.validateEmpty(context, trimmedText)

                    InputValidator.validateLength(context, trimmedText, 9) != null ->
                        InputValidator.validateLength(context, trimmedText, 9)

                    InputValidator.validateMinAllowedInitialLength(
                        context,
                        trimmedText,
                        2
                    ) != null ->
                        InputValidator.validateMinAllowedInitialLength(context, trimmedText, 2)

                    InputValidator.validateByPattern(
                        context = context,
                        text = trimmedText,
                        pattern = ALLOWED_HOUSE_NUMBER_REGEX
                    ) != null ->
                        InputValidator.validateByPattern(
                            context = context,
                            text = trimmedText,
                            pattern = ALLOWED_HOUSE_NUMBER_REGEX
                        )

                    InputValidator.validateOnlySingleSpaces(context, trimmedText) != null ->
                        InputValidator.validateOnlySingleSpaces(context, trimmedText)

                    InputValidator.validateNoLeadingTrailingSpace(context, trimmedText) != null ->
                        InputValidator.validateNoLeadingTrailingSpace(context, trimmedText)

                    InputValidator.validateNoLineBreaks(context, trimmedText) != null ->
                        InputValidator.validateNoLineBreaks(context, trimmedText)

                    else -> null
                }
                viewModel.setPostalCodeValid(error == null)
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
                        val remaining = 9 - trimmedText.length
                        if (remaining in 0..9)
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
                        else
                            field.setBottomTextState(BottomTextState.Empty)
                    }
                }
                isEditing = false
            }
        })
    }

    // --- streetName ---
    private fun setupStreetValidation() {
        val field = binding.ccavStreet
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
                var cleaned = original
                    .replace("\n", "")
                    .replace(Regex(" {2,}"), " ")
                if (cleaned != original) {
                    field.text = cleaned
                    field.setSelection(minOf(cursorPosition, cleaned.length))
                }
                val trimmedText = cleaned.trim()

                viewModel.updateForm { copy(streetName = trimmedText) }

                val error = when {
                    InputValidator.validateEmpty(context, trimmedText) != null ->
                        InputValidator.validateEmpty(context, trimmedText)

                    InputValidator.validateLength(context, trimmedText, 60) != null ->
                        InputValidator.validateLength(context, trimmedText, 60)

                    InputValidator.validateMinAllowedInitialLength(
                        context,
                        trimmedText,
                        4
                    ) != null ->
                        InputValidator.validateMinAllowedInitialLength(context, trimmedText, 4)

                    InputValidator.validateByPattern(
                        context = context,
                        text = trimmedText,
                        pattern = ALLOWED_ADDRESS_DESCRIPTION_REGEX
                    ) != null ->
                        InputValidator.validateByPattern(
                            context = context,
                            text = trimmedText,
                            pattern = ALLOWED_ADDRESS_DESCRIPTION_REGEX
                        )

                    InputValidator.validateOnlySingleSpaces(context, trimmedText) != null ->
                        InputValidator.validateOnlySingleSpaces(context, trimmedText)

                    InputValidator.validateNoLeadingTrailingSpace(context, trimmedText) != null ->
                        InputValidator.validateNoLeadingTrailingSpace(context, trimmedText)

                    InputValidator.validateNoLineBreaks(context, trimmedText) != null ->
                        InputValidator.validateNoLineBreaks(context, trimmedText)

                    else -> null
                }
                viewModel.setStreetValid(error == null)
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
                        val remaining = 60 - trimmedText.length
                        if (remaining in 0..60)
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
                        else
                            field.setBottomTextState(BottomTextState.Empty)
                    }
                }
                isEditing = false
            }
        })
    }

    // --- houseNumber ---
    private fun setupHouseNumberValidation() {
        val field = binding.ccavHouseNumber
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
                var cleaned = original
                    .replace("\n", "")
                    .replace(Regex(" {2,}"), " ")
                if (cleaned != original) {
                    field.text = cleaned
                    field.setSelection(minOf(cursorPosition, cleaned.length))
                }
                val trimmedText = cleaned.trim()

                viewModel.updateForm { copy(houseNumber = trimmedText) }

                val error = when {
                    InputValidator.validateEmpty(context, trimmedText) != null ->
                        InputValidator.validateEmpty(context, trimmedText)

                    InputValidator.validateLength(context, trimmedText, 5) != null ->
                        InputValidator.validateLength(context, trimmedText, 5)

                    InputValidator.validateByPattern(
                        context = context,
                        text = trimmedText,
                        pattern = ALLOWED_ADDRESS_NUMBER_REGEX
                    ) != null ->
                        InputValidator.validateByPattern(
                            context = context,
                            text = trimmedText,
                            pattern = ALLOWED_ADDRESS_NUMBER_REGEX
                        )

                    InputValidator.validateOnlySingleSpaces(context, trimmedText) != null ->
                        InputValidator.validateOnlySingleSpaces(context, trimmedText)

                    InputValidator.validateNoLeadingTrailingSpace(context, trimmedText) != null ->
                        InputValidator.validateNoLeadingTrailingSpace(context, trimmedText)

                    InputValidator.validateNoLineBreaks(context, trimmedText) != null ->
                        InputValidator.validateNoLineBreaks(context, trimmedText)

                    else -> null
                }
                viewModel.setHouseNumberValid(error == null)
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
                        val remaining = 5 - trimmedText.length
                        if (remaining in 0..5)
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
                        else
                            field.setBottomTextState(BottomTextState.Empty)
                    }
                }
                isEditing = false
            }
        })
    }

    // --- locationNumber, entranceNumber, floor ---
    private fun setupLocationNumberValidation() {
        setupOptionalFieldValidation(
            binding.ccavLocationNumber,
            5,
            ALLOWED_SHORT_FIELD_REGEX,
            viewModel::setLocationNumberValid
        ) { value -> viewModel.updateForm { copy(locationNumber = value) } }
    }

    private fun setupEntranceNumberValidation() {
        setupOptionalFieldValidation(
            binding.ccavEntranceNumber,
            5,
            ALLOWED_SHORT_FIELD_REGEX,
            viewModel::setEntranceNumberValid
        ) { value -> viewModel.updateForm { copy(entranceNumber = value) } }
    }

    private fun setupFloorValidation() {
        setupOptionalFieldValidation(
            binding.ccavFloor,
            5,
            ALLOWED_SHORT_FIELD_REGEX,
            viewModel::setFloorValid
        ) { value -> viewModel.updateForm { copy(floor = value) } }
    }

    // --- numberIntercom ---
    private fun setupNumberIntercomValidation() {
        setupOptionalFieldValidation(
            binding.ccavNumberIntercom,
            15,
            ALLOWED_INTERCOM_NUMBER_REGEX,
            viewModel::setNumberIntercomValid
        ) { value -> viewModel.updateForm { copy(numberIntercom = value) } }
    }

    // --- общая функция для необязательных полей ---
    private fun setupOptionalFieldValidation(
        field: CustomCardActionView,
        maxLen: Int,
        regex: Regex,
        setValid: (Boolean) -> Unit,
        updateFormField: (String) -> Unit
    ) {
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
                var cleaned = original
                    .replace("\n", "")
                    .replace(Regex(" {2,}"), " ")
                if (cleaned != original) {
                    field.text = cleaned
                    field.setSelection(minOf(cursorPosition, cleaned.length))
                }
                val trimmedText = cleaned.trim()

                // ⬇️ вот тут обновляем formState!
                updateFormField(trimmedText)

                var error: String? = null
                if (trimmedText.isNotEmpty()) {
                    error = when {
                        trimmedText.length > maxLen ->
                            context.getString(R.string.error_max_length, maxLen)

                        !trimmedText.matches(regex) ->
                            context.getString(R.string.error_invalid_characters)

                        trimmedText.contains("  ") ->
                            context.getString(R.string.error_only_single_space)

                        trimmedText.startsWith(" ") || trimmedText.endsWith(" ") ->
                            context.getString(R.string.error_no_leading_trailing_space)

                        InputValidator.validateName(context, trimmedText) != null ->
                            InputValidator.validateName(context, trimmedText)

                        trimmedText.contains("\n") || trimmedText.contains("\r") ->
                            context.getString(R.string.error_invalid_characters)

                        else -> null
                    }
                }
                setValid(error == null)
                if (viewModel.isEditMode.value == true) {
                    if (error != null) {
                        field.setBottomTextState(
                            BottomTextState.Error(
                                showErrorText = true,
                                showErrorIcon = true,
                                errorText = error
                            )
                        )
                    } else if (trimmedText.isNotEmpty()) {
                        val remaining = maxLen - trimmedText.length
                        if (remaining in 0..maxLen)
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
                        else
                            field.setBottomTextState(BottomTextState.Empty)
                    } else {
                        field.setBottomTextState(BottomTextState.Empty)
                    }
                }
                isEditing = false
            }
        })
    }

    /** При сохранении (по кнопке) обязательно подчищаем пробелы на краях у всех полей! */
    fun trimAllFieldsOnSave() {
        binding.ccavRecipientName.text = binding.ccavRecipientName.text.toString().trim()
        binding.ccavPostalCode.text = binding.ccavPostalCode.text.toString().trim()
        binding.ccavStreet.text = binding.ccavStreet.text.toString().trim()
        binding.ccavHouseNumber.text = binding.ccavHouseNumber.text.toString().trim()
        binding.ccavLocationNumber.text = binding.ccavLocationNumber.text.toString().trim()
        binding.ccavEntranceNumber.text = binding.ccavEntranceNumber.text.toString().trim()
        binding.ccavFloor.text = binding.ccavFloor.text.toString().trim()
        binding.ccavNumberIntercom.text = binding.ccavNumberIntercom.text.toString().trim()
    }

    companion object {
        /**
         * Разрешает только:
         * - латинские буквы (a-z, A-Z)
         * - цифры (0-9)
         * - тире ("-")
         * - точка (".")
         * - слеш ("/")
         * - пробел (" ")
         *
         * Запрещены любые другие символы, в том числе:
         * - кириллица и другие языки
         * - специальные символы (@, #, !, %, *, _, =, &, скобки, и т.д.)
         * - запятые и подчеркивания
         * - переводы строк, табуляция
         * - эмодзи и невидимые символы
         * - двойные пробелы (дополнительно проверяется вне regex)
         */
        private val ALLOWED_CHARACTERS_REGEX = Regex("^[A-Za-z0-9\\-./ ]{1,100}$")

        /**
         * Разрешает:
         * - только цифры (0-9)
         * - тире (-), слеш (/), подчеркивание (_), пробел
         * Минимум 2 символа, максимум 9.
         * Запрещены буквы, точки, запятые, другие спецсимволы и эмодзи.
         */
        private val ALLOWED_HOUSE_NUMBER_REGEX = Regex("^[0-9\\-_/ ]{2,9}$")

        /**
         * Разрешает только:
         * - латинские буквы (a-z, A-Z)
         * - цифры (0-9)
         * - точка (.)
         * - запятая (,)
         * - подчёркивание (_)
         * - тире (-)
         * - слеш (/)
         * - пробел
         *
         * Длина: от 4 до 60 символов.
         * Запрещает любые другие символы (например, кириллицу, спецсимволы, эмодзи и т.д.).
         */
        private val ALLOWED_ADDRESS_DESCRIPTION_REGEX = Regex("^[A-Za-z0-9.,_\\-\\/ ]{4,60}$")

        /**
         * Разрешает только:
         * - латинские буквы (a-z, A-Z)
         * - цифры (0-9)
         * - тире (-)
         * - слеш (/)
         * - пробел
         *
         * Длина: от 1 до 5 символов.
         * Запрещает любые другие символы (например, точки, запятые, подчёркивания, кириллицу, спецсимволы, эмодзи и т.д.).
         */
        private val ALLOWED_ADDRESS_NUMBER_REGEX = Regex("^[A-Za-z0-9\\-/ ]{1,5}$")

        /**
         * Разрешает только:
         * - латинские буквы (a-z, A-Z)
         * - цифры (0-9)
         * - тире (-)
         * - слеш (/)
         * - пробел
         *
         * Длина: от 1 до 5 символов.
         * Запрещает любые другие символы (точки, запятые, подчёркивания, кириллицу, спецсимволы, эмодзи и т.д.).
         *
         * Используется для: номера помещения, подъезда, этажа и т.д.
         */
        private val ALLOWED_SHORT_FIELD_REGEX = Regex("^[A-Za-z0-9\\-/ ]{1,5}$")

        /**
         * Разрешает только:
         * - латинские буквы (a-z, A-Z)
         * - цифры (0-9)
         * - тире (-)
         * - слеш (/)
         * - решётка (#)
         * - пробел
         *
         * Длина: от 1 до 15 символов.
         * Запрещены любые другие символы (точки, запятые, подчёркивания, кириллица, спецсимволы, эмодзи и т.д.).
         *
         * Используется для: поля "Номер домофона" (intercom).
         */
        private val ALLOWED_INTERCOM_NUMBER_REGEX = Regex("^[A-Za-z0-9\\-/# ]{1,15}$")

    }
}
