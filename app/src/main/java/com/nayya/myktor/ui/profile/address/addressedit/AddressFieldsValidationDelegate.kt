package com.nayya.myktor.ui.profile.address.addressedit

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentAddressEditBinding
import com.nayya.myktor.utils.input.InputValidator
import com.nayya.uicomponents.BottomTextState
import com.nayya.uicomponents.CustomCardActionView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddressFieldsValidationDelegate(
    private val context: Context,
    private val binding: FragmentAddressEditBinding,
    private val viewModel: AddressEditViewModel,
    private val coroutineScope: CoroutineScope
) {

    private var isFirstEntry = true

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

        // Удобная функция для смены текста
        val showDescription: (String) -> Unit = { text ->
            field.setBottomTextState(
                BottomTextState.Description(
                    showDescriptionText = true,
                    descriptionText = text
                )
            )
        }

        // Начальное состояние
        showDescription(if (field.text.isNullOrBlank()) RECIPIENT_DESCRIPTION else RECIPIENT_PLACEHOLDER)

        field.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val original = s?.toString() ?: ""
                val cursorPosition = field.getSelection()
                val cleaned = original.replace("\n", "").replace(Regex(" {2,}"), " ")
                if (cleaned != original) {
                    field.text = cleaned
                    field.setSelection(minOf(cursorPosition, cleaned.length))
                }
                val trimmedText = cleaned.trim()
                viewModel.updateForm { copy(recipientName = trimmedText) }

                // Показываем описание на первом входе
                if (isFirstEntry) {
                    showDescription(if (trimmedText.isBlank()) RECIPIENT_DESCRIPTION else RECIPIENT_PLACEHOLDER)
                    isFirstEntry = false
                    isEditing = false
                    return
                }

                // Валидация
                val error = InputValidator.validateEmpty(context, trimmedText)
                    ?: InputValidator.validateLength(context, trimmedText, 100)
                    ?: InputValidator.validateMinAllowedInitialLength(context, trimmedText, 5)
                    ?: InputValidator.validateByPattern(context, trimmedText, ALLOWED_CHARACTERS_REGEX)
                    ?: InputValidator.validateOnlySingleSpaces(context, trimmedText)
                    ?: InputValidator.validateNoLeadingTrailingSpace(context, trimmedText)
                    ?: InputValidator.validateNoLineBreaks(context, trimmedText)

                viewModel.setRecipientNameValid(error == null)

                if (viewModel.isEditMode.value == true) {
                    descriptionResetJob?.cancel()
                    when {
                        error != null -> {
                            field.setBottomTextState(
                                BottomTextState.Error(
                                    showErrorText = true,
                                    showErrorIcon = true,
                                    errorText = error
                                )
                            )
                        }
                        trimmedText.isEmpty() -> {
                            showDescription(RECIPIENT_DESCRIPTION)
                        }
                        else -> {
                            val remaining = 100 - trimmedText.length
                            if (remaining in 0..100) {
                                val tempDescription = context.resources.getQuantityString(
                                    R.plurals.remaining_characters, remaining, remaining
                                )
                                showDescription(tempDescription)
                                descriptionResetJob = coroutineScope.launch {
                                    delay(FIELD_HINT_DELAY_MS)
                                    val currentText = field.text.toString()
                                    val currentError = InputValidator.validateEmpty(context, currentText)
                                        ?: InputValidator.validateLength(context, currentText, 100)
                                        ?: InputValidator.validateMinAllowedInitialLength(context, currentText, 5)
                                        ?: InputValidator.validateByPattern(context, currentText, ALLOWED_CHARACTERS_REGEX)
                                        ?: InputValidator.validateOnlySingleSpaces(context, currentText)
                                        ?: InputValidator.validateNoLeadingTrailingSpace(context, currentText)
                                        ?: InputValidator.validateNoLineBreaks(context, currentText)
                                    if (currentError == null && currentText.isNotEmpty()) {
                                        showDescription(RECIPIENT_PLACEHOLDER)
                                    } else if (currentText.isBlank()) {
                                        showDescription(RECIPIENT_DESCRIPTION)
                                    }
                                }
                            } else {
                                field.setBottomTextState(BottomTextState.Empty)
                            }
                        }
                    }
                }
                isEditing = false
            }
        })
    }

    // --- postalCode ---
    private fun setupPostalCodeValidation() {
        val field = binding.ccavPostalCode

        var isFirstEntry = true
        var descriptionResetJob: Job? = null

        // Первое состояние при открытии
        if (field.text.isNullOrBlank()) {
            field.setBottomTextState(
                BottomTextState.Description(
                    showDescriptionText = true,
                    descriptionText = POSTAL_CODE_DESCRIPTION
                )
            )
        } else {
            field.setBottomTextState(
                BottomTextState.Description(
                    showDescriptionText = true,
                    descriptionText = POSTAL_CODE_PLACEHOLDER
                )
            )
        }

        field.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val original = s?.toString() ?: ""
                val cursorPosition = field.getSelection()
                val cleaned = original.replace("\n", "").replace(Regex(" {2,}"), " ")
                if (cleaned != original) {
                    field.text = cleaned
                    field.setSelection(minOf(cursorPosition, cleaned.length))
                }
                val trimmedText = cleaned.trim()

                viewModel.updateForm { copy(postalCode = trimmedText) }

                // Первая загрузка: всегда дефолт/placeholder
                if (isFirstEntry) {
                    if (trimmedText.isBlank()) {
                        field.setBottomTextState(
                            BottomTextState.Description(
                                showDescriptionText = true,
                                descriptionText = POSTAL_CODE_DESCRIPTION
                            )
                        )
                    } else {
                        field.setBottomTextState(
                            BottomTextState.Description(
                                showDescriptionText = true,
                                descriptionText = POSTAL_CODE_PLACEHOLDER
                            )
                        )
                    }
                    isFirstEntry = false
                    isEditing = false
                    return
                }

                val error = InputValidator.validateEmpty(context, trimmedText)
                    ?: InputValidator.validateLength(context, trimmedText, 9)
                    ?: InputValidator.validateMinAllowedInitialLength(context, trimmedText, 2)
                    ?: InputValidator.validateByPattern(context, trimmedText, ALLOWED_HOUSE_NUMBER_REGEX)
                    ?: InputValidator.validateOnlySingleSpaces(context, trimmedText)
                    ?: InputValidator.validateNoLeadingTrailingSpace(context, trimmedText)
                    ?: InputValidator.validateNoLineBreaks(context, trimmedText)

                viewModel.setPostalCodeValid(error == null)

                if (viewModel.isEditMode.value == true) {
                    descriptionResetJob?.cancel()
                    if (error != null) {
                        // Ошибка
                        field.setBottomTextState(
                            BottomTextState.Error(
                                showErrorText = true,
                                showErrorIcon = true,
                                errorText = error
                            )
                        )
                    } else {
                        if (trimmedText.isEmpty()) {
                            // Если пусто — всегда дефолтное описание!
                            field.setBottomTextState(
                                BottomTextState.Description(
                                    showDescriptionText = true,
                                    descriptionText = POSTAL_CODE_DESCRIPTION
                                )
                            )
                        } else {
                            // Временное описание (сколько осталось символов)
                            val remaining = 9 - trimmedText.length
                            if (remaining in 0..9) {
                                val tempDescription = context.resources.getQuantityString(
                                    R.plurals.remaining_characters, remaining, remaining
                                )
                                field.setBottomTextState(
                                    BottomTextState.Description(
                                        showDescriptionText = true,
                                        descriptionText = tempDescription
                                    )
                                )
                                // Через 3 секунды вернуть плейсхолдер/дефолт
                                descriptionResetJob = coroutineScope.launch {
                                    delay(FIELD_HINT_DELAY_MS)
                                    val currentText = field.text.toString()
                                    val currentError = InputValidator.validateEmpty(context, currentText)
                                        ?: InputValidator.validateLength(context, currentText, 9)
                                        ?: InputValidator.validateMinAllowedInitialLength(context, currentText, 2)
                                        ?: InputValidator.validateByPattern(context, currentText, ALLOWED_HOUSE_NUMBER_REGEX)
                                        ?: InputValidator.validateOnlySingleSpaces(context, currentText)
                                        ?: InputValidator.validateNoLeadingTrailingSpace(context, currentText)
                                        ?: InputValidator.validateNoLineBreaks(context, currentText)
                                    if (currentError == null && currentText.isNotEmpty()) {
                                        // Вернуть плейсхолдер для непустого
                                        field.setBottomTextState(
                                            BottomTextState.Description(
                                                showDescriptionText = true,
                                                descriptionText = POSTAL_CODE_PLACEHOLDER
                                            )
                                        )
                                    } else if (currentText.isBlank()) {
                                        // Вернуть дефолт если пусто
                                        field.setBottomTextState(
                                            BottomTextState.Description(
                                                showDescriptionText = true,
                                                descriptionText = POSTAL_CODE_DESCRIPTION
                                            )
                                        )
                                    }
                                }
                            } else {
                                field.setBottomTextState(BottomTextState.Empty)
                            }
                        }
                    }
                }

                isEditing = false
            }
        })
    }

    // --- streetName ---
    private fun setupStreetValidation() {
        val field = binding.ccavStreet

        var isFirstEntry = true
        var descriptionResetJob: Job? = null

        // Первое состояние при открытии
        if (field.text.isNullOrBlank()) {
            field.setBottomTextState(
                BottomTextState.Description(
                    showDescriptionText = true,
                    descriptionText = STREET_DESCRIPTION
                )
            )
        } else {
            field.setBottomTextState(
                BottomTextState.Description(
                    showDescriptionText = true,
                    descriptionText = STREET_PLACEHOLDER
                )
            )
        }

        field.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val original = s?.toString() ?: ""
                val cursorPosition = field.getSelection()
                val cleaned = original.replace("\n", "").replace(Regex(" {2,}"), " ")
                if (cleaned != original) {
                    field.text = cleaned
                    field.setSelection(minOf(cursorPosition, cleaned.length))
                }
                val trimmedText = cleaned.trim()

                viewModel.updateForm { copy(streetName = trimmedText) }

                // Первая загрузка: всегда дефолт/placeholder
                if (isFirstEntry) {
                    if (trimmedText.isBlank()) {
                        field.setBottomTextState(
                            BottomTextState.Description(
                                showDescriptionText = true,
                                descriptionText = STREET_DESCRIPTION
                            )
                        )
                    } else {
                        field.setBottomTextState(
                            BottomTextState.Description(
                                showDescriptionText = true,
                                descriptionText = STREET_PLACEHOLDER
                            )
                        )
                    }
                    isFirstEntry = false
                    isEditing = false
                    return
                }

                val error = InputValidator.validateEmpty(context, trimmedText)
                    ?: InputValidator.validateLength(context, trimmedText, 60)
                    ?: InputValidator.validateMinAllowedInitialLength(context, trimmedText, 4)
                    ?: InputValidator.validateByPattern(context, trimmedText, ALLOWED_ADDRESS_DESCRIPTION_REGEX)
                    ?: InputValidator.validateOnlySingleSpaces(context, trimmedText)
                    ?: InputValidator.validateNoLeadingTrailingSpace(context, trimmedText)
                    ?: InputValidator.validateNoLineBreaks(context, trimmedText)

                viewModel.setStreetValid(error == null)

                if (viewModel.isEditMode.value == true) {
                    descriptionResetJob?.cancel()
                    if (error != null) {
                        field.setBottomTextState(
                            BottomTextState.Error(
                                showErrorText = true,
                                showErrorIcon = true,
                                errorText = error
                            )
                        )
                    } else {
                        if (trimmedText.isEmpty()) {
                            field.setBottomTextState(
                                BottomTextState.Description(
                                    showDescriptionText = true,
                                    descriptionText = STREET_DESCRIPTION
                                )
                            )
                        } else {
                            val remaining = 50 - trimmedText.length
                            if (remaining in 0..50) {
                                val tempDescription = context.resources.getQuantityString(
                                    R.plurals.remaining_characters, remaining, remaining
                                )
                                field.setBottomTextState(
                                    BottomTextState.Description(
                                        showDescriptionText = true,
                                        descriptionText = tempDescription
                                    )
                                )
                                // Таймер — через 3 сек вернуть плейсхолдер или дефолт
                                descriptionResetJob = coroutineScope.launch {
                                    delay(FIELD_HINT_DELAY_MS)
                                    val currentText = field.text.toString()
                                    val currentError = InputValidator.validateEmpty(context, currentText)
                                        ?: InputValidator.validateLength(context, currentText, 60)
                                        ?: InputValidator.validateMinAllowedInitialLength(context, currentText, 4)
                                        ?: InputValidator.validateByPattern(context, currentText, ALLOWED_ADDRESS_DESCRIPTION_REGEX)
                                        ?: InputValidator.validateOnlySingleSpaces(context, currentText)
                                        ?: InputValidator.validateNoLeadingTrailingSpace(context, currentText)
                                        ?: InputValidator.validateNoLineBreaks(context, currentText)
                                    if (currentError == null && currentText.isNotEmpty()) {
                                        field.setBottomTextState(
                                            BottomTextState.Description(
                                                showDescriptionText = true,
                                                descriptionText = STREET_PLACEHOLDER
                                            )
                                        )
                                    } else if (currentText.isBlank()) {
                                        field.setBottomTextState(
                                            BottomTextState.Description(
                                                showDescriptionText = true,
                                                descriptionText = STREET_DESCRIPTION
                                            )
                                        )
                                    }
                                }
                            } else {
                                field.setBottomTextState(BottomTextState.Empty)
                            }
                        }
                    }
                }
                isEditing = false
            }
        })
    }

    // --- houseNumber ---
    private fun setupHouseNumberValidation() {
        val field = binding.ccavHouseNumber

        var isFirstEntry = true
        var descriptionResetJob: Job? = null

        // Первое состояние при открытии
        if (field.text.isNullOrBlank()) {
            field.setBottomTextState(
                BottomTextState.Description(
                    showDescriptionText = true,
                    descriptionText = HOUSE_NUMBER_DESCRIPTION
                )
            )
        } else {
            field.setBottomTextState(
                BottomTextState.Description(
                    showDescriptionText = true,
                    descriptionText = HOUSE_NUMBER_PLACEHOLDER
                )
            )
        }

        field.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val original = s?.toString() ?: ""
                val cursorPosition = field.getSelection()
                val cleaned = original.replace("\n", "").replace(Regex(" {2,}"), " ")
                if (cleaned != original) {
                    field.text = cleaned
                    field.setSelection(minOf(cursorPosition, cleaned.length))
                }
                val trimmedText = cleaned.trim()

                viewModel.updateForm { copy(houseNumber = trimmedText) }

                // Первая загрузка: всегда дефолт/placeholder
                if (isFirstEntry) {
                    if (trimmedText.isBlank()) {
                        field.setBottomTextState(
                            BottomTextState.Description(
                                showDescriptionText = true,
                                descriptionText = HOUSE_NUMBER_DESCRIPTION
                            )
                        )
                    } else {
                        field.setBottomTextState(
                            BottomTextState.Description(
                                showDescriptionText = true,
                                descriptionText = HOUSE_NUMBER_PLACEHOLDER
                            )
                        )
                    }
                    isFirstEntry = false
                    isEditing = false
                    return
                }

                val error = InputValidator.validateEmpty(context, trimmedText)
                    ?: InputValidator.validateLength(context, trimmedText, 5)
                    ?: InputValidator.validateByPattern(context, trimmedText, ALLOWED_ADDRESS_NUMBER_REGEX)
                    ?: InputValidator.validateOnlySingleSpaces(context, trimmedText)
                    ?: InputValidator.validateNoLeadingTrailingSpace(context, trimmedText)
                    ?: InputValidator.validateNoLineBreaks(context, trimmedText)

                viewModel.setHouseNumberValid(error == null)

                if (viewModel.isEditMode.value == true) {
                    descriptionResetJob?.cancel()
                    if (error != null) {
                        field.setBottomTextState(
                            BottomTextState.Error(
                                showErrorText = true,
                                showErrorIcon = true,
                                errorText = error
                            )
                        )
                    } else {
                        if (trimmedText.isEmpty()) {
                            field.setBottomTextState(
                                BottomTextState.Description(
                                    showDescriptionText = true,
                                    descriptionText = HOUSE_NUMBER_DESCRIPTION
                                )
                            )
                        } else {
                            val remaining = 5 - trimmedText.length
                            if (remaining in 0..5) {
                                val tempDescription = context.resources.getQuantityString(
                                    R.plurals.remaining_characters, remaining, remaining
                                )
                                field.setBottomTextState(
                                    BottomTextState.Description(
                                        showDescriptionText = true,
                                        descriptionText = tempDescription
                                    )
                                )
                                // Таймер — через 3 сек вернуть плейсхолдер или дефолт
                                descriptionResetJob = coroutineScope.launch {
                                    delay(FIELD_HINT_DELAY_MS)
                                    val currentText = field.text.toString()
                                    val currentError = InputValidator.validateEmpty(context, currentText)
                                        ?: InputValidator.validateLength(context, currentText, 5)
                                        ?: InputValidator.validateByPattern(context, currentText, ALLOWED_ADDRESS_NUMBER_REGEX)
                                        ?: InputValidator.validateOnlySingleSpaces(context, currentText)
                                        ?: InputValidator.validateNoLeadingTrailingSpace(context, currentText)
                                        ?: InputValidator.validateNoLineBreaks(context, currentText)
                                    if (currentError == null && currentText.isNotEmpty()) {
                                        field.setBottomTextState(
                                            BottomTextState.Description(
                                                showDescriptionText = true,
                                                descriptionText = HOUSE_NUMBER_PLACEHOLDER
                                            )
                                        )
                                    } else if (currentText.isBlank()) {
                                        field.setBottomTextState(
                                            BottomTextState.Description(
                                                showDescriptionText = true,
                                                descriptionText = HOUSE_NUMBER_DESCRIPTION
                                            )
                                        )
                                    }
                                }
                            } else {
                                field.setBottomTextState(BottomTextState.Empty)
                            }
                        }
                    }
                }
                isEditing = false
            }
        })
    }

    // --- locationNumber, entranceNumber, floor ---
    private fun setupLocationNumberValidation() {
        setupOptionalFieldValidation(
            field = binding.ccavLocationNumber,
            maxLen = 5,
            regex = ALLOWED_SHORT_FIELD_REGEX,
            setValid = viewModel::setLocationNumberValid,
            updateFormField = { value -> viewModel.updateForm { copy(locationNumber = value) } },
            fullDescription = "Номер квартиры (локации). Не обязательное поле",
            shortDescription = "Номер квартиры (локации)",
        )
    }

    private fun setupEntranceNumberValidation() {
        setupOptionalFieldValidation(
            field = binding.ccavEntranceNumber,
            maxLen = 5,
            regex = ALLOWED_SHORT_FIELD_REGEX,
            setValid = viewModel::setEntranceNumberValid,
            updateFormField = { value -> viewModel.updateForm { copy(entranceNumber = value) } },
            fullDescription = "Номер подъезда. Не обязательное поле",
            shortDescription = "Номер подъезда",
        )
    }

    private fun setupFloorValidation() {
        setupOptionalFieldValidation(
            field = binding.ccavFloor,
            maxLen = 5,
            regex = ALLOWED_SHORT_FIELD_REGEX,
            setValid = viewModel::setFloorValid,
            updateFormField = { value -> viewModel.updateForm { copy(floor = value) } },
            fullDescription = "Этаж. Не обязательное поле",
            shortDescription = "Этаж",
        )
    }

    // --- numberIntercom ---
    private fun setupNumberIntercomValidation() {
        setupOptionalFieldValidation(
            field = binding.ccavNumberIntercom,
            maxLen = 15,
            regex = ALLOWED_INTERCOM_NUMBER_REGEX,
            setValid = viewModel::setNumberIntercomValid,
            updateFormField = { value -> viewModel.updateForm { copy(numberIntercom = value) } },
            fullDescription = "Код домофона. Не обязательное поле",
            shortDescription = "Код домофона",
        )
    }

    // --- общая функция для необязательных полей ---
    private fun setupOptionalFieldValidation(
        field: CustomCardActionView,
        maxLen: Int,
        regex: Regex,
        setValid: (Boolean) -> Unit,
        updateFormField: (String) -> Unit,
        fullDescription: String,
        shortDescription: String,
    ) {
        var isFirstEntry = true
        var descriptionResetJob: Job? = null

        // Инициализация состояния при старте (пусто или есть значение)
        if (field.text.isNullOrBlank()) {
            field.setBottomTextState(
                BottomTextState.Description(
                    showDescriptionText = true,
                    descriptionText = fullDescription
                )
            )
        } else {
            field.setBottomTextState(
                BottomTextState.Description(
                    showDescriptionText = true,
                    descriptionText = shortDescription
                )
            )
        }

        field.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val original = s?.toString() ?: ""
                val cursorPosition = field.getSelection()
                var cleaned = original.replace("\n", "").replace(Regex(" {2,}"), " ")
                if (cleaned != original) {
                    field.text = cleaned
                    field.setSelection(minOf(cursorPosition, cleaned.length))
                }
                val trimmedText = cleaned.trim()

                updateFormField(trimmedText)

                // Первая загрузка — только описание
                if (isFirstEntry) {
                    if (trimmedText.isBlank()) {
                        field.setBottomTextState(
                            BottomTextState.Description(
                                showDescriptionText = true,
                                descriptionText = fullDescription
                            )
                        )
                    } else {
                        field.setBottomTextState(
                            BottomTextState.Description(
                                showDescriptionText = true,
                                descriptionText = shortDescription
                            )
                        )
                    }
                    isFirstEntry = false
                    isEditing = false
                    return
                }

                // Валидация
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
                    descriptionResetJob?.cancel()
                    if (error != null) {
                        field.setBottomTextState(
                            BottomTextState.Error(
                                showErrorText = true,
                                showErrorIcon = true,
                                errorText = error
                            )
                        )
                    } else if (trimmedText.isEmpty()) {
                        // Пустое поле — расширенное описание
                        field.setBottomTextState(
                            BottomTextState.Description(
                                showDescriptionText = true,
                                descriptionText = fullDescription
                            )
                        )
                    } else {
                        // Показываем счетчик, возвращаем краткую надпись через 3 сек
                        val remaining = maxLen - trimmedText.length
                        if (remaining in 0..maxLen) {
                            val tempDescription = context.resources.getQuantityString(
                                R.plurals.remaining_characters, remaining, remaining
                            )
                            field.setBottomTextState(
                                BottomTextState.Description(
                                    showDescriptionText = true,
                                    descriptionText = tempDescription
                                )
                            )
                            descriptionResetJob = coroutineScope.launch {
                                delay(FIELD_HINT_DELAY_MS)
                                val currentText = field.text.toString()
                                val hasError = currentText.length > maxLen ||
                                        !currentText.matches(regex) ||
                                        currentText.contains("  ") ||
                                        currentText.startsWith(" ") ||
                                        currentText.endsWith(" ") ||
                                        InputValidator.validateName(context, currentText) != null ||
                                        currentText.contains("\n") || currentText.contains("\r")
                                if (!hasError && currentText.isNotEmpty()) {
                                    field.setBottomTextState(
                                        BottomTextState.Description(
                                            showDescriptionText = true,
                                            descriptionText = shortDescription
                                        )
                                    )
                                } else if (currentText.isBlank()) {
                                    field.setBottomTextState(
                                        BottomTextState.Description(
                                            showDescriptionText = true,
                                            descriptionText = fullDescription
                                        )
                                    )
                                }
                            }
                        } else {
                            field.setBottomTextState(BottomTextState.Empty)
                        }
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

        private var descriptionResetJob: Job? = null
        private const val RECIPIENT_DESCRIPTION = "Получатель. Обязательное поле"
        private const val RECIPIENT_PLACEHOLDER = "Получатель"

        private const val POSTAL_CODE_DESCRIPTION = "Почтовый индекс. Обязательное поле"
        private const val POSTAL_CODE_PLACEHOLDER = "Почтовый индекс"

        private const val STREET_DESCRIPTION = "Улица. Обязательное поле"
        private const val STREET_PLACEHOLDER = "Улица"

        private const val HOUSE_NUMBER_DESCRIPTION = "Номер дома. Обязательное поле"
        private const val HOUSE_NUMBER_PLACEHOLDER = "Номер дома"

        private const val FIELD_HINT_DELAY_MS = 3_000L
    }
}
