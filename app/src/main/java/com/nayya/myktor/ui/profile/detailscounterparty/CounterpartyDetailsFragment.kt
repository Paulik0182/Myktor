package com.nayya.myktor.ui.profile.detailscounterparty

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentCounterpartyDetailsBinding
import com.nayya.myktor.databinding.LayoutLegalEntityBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
import com.nayya.myktor.ui.profile.contacts.ContactEditBottomSheetDialog
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.LocaleUtils.goBack
import com.nayya.myktor.utils.input.InputValidator
import com.nayya.myktor.utils.showSnackbar
import com.nayya.myktor.utils.viewBinding
import com.nayya.uicomponents.BottomTextState
import com.nayya.uicomponents.CustomCardActionView

class CounterpartyDetailsFragment : BaseFragment(R.layout.fragment_counterparty_details) {

    private val binding by viewBinding<FragmentCounterpartyDetailsBinding>()
    private lateinit var viewModel: CounterpartyDetailsViewModel

    private lateinit var legalEntityBinding: LayoutLegalEntityBinding

    private var counterpartyId: Long? = null

    // Для того чтобы скрыть нижнюю навигацию и персчитать размеры container
    override val hideBottomNavigation = true

    private var ignoreChanges = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        counterpartyId = arguments?.getLong(ARG_COUNTERPARTY_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(CounterpartyDetailsViewModel::class.java)

        counterpartyId?.let { viewModel.loadCounterpartyById(it) }

        legalEntityBinding = LayoutLegalEntityBinding.bind(binding.includeLegalEntity.root)

        setupToolbar()
        observeViewModel()

        // TODO Это должно быть во ViewModel
        if (viewModel.countries.value.isNullOrEmpty()) {
            viewModel.loadCountries()
        }

        binding.contactsInfo.setEditClickListener {
            tryNavigateWithSaveCheck {
                openContactsEditor()
            }
        }

        binding.bankInfo.apply {
            setEditClickListener {
                tryNavigateWithSaveCheck {
                    Toast.makeText(context, "Клик на Банк", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.addressesInfo.apply {
            setEditClickListener {
                tryNavigateWithSaveCheck {
                    Toast.makeText(context, "Клик на Адрес", Toast.LENGTH_SHORT).show()
                }
            }
        }

        setFragmentResultListener("contacts_updated") { _, _ ->
            counterpartyId?.let { viewModel.loadCounterpartyById(it) } // ← повторно загружаем с сервера
        }

        saveDate()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    tryNavigateWithSaveCheck {
                        goBack()
                    }
                }
            }
        )
    }

    // Часть механизма отслеживания изменений данных.
    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!ignoreChanges) {
                    viewModel.setHasUnsavedChanges(true)
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        }

        val nameEditTexts = listOf(
            binding.tvShortName,
            binding.tvFirstName,
            binding.tvLastName
        )

        val firmFields = listOf(
            legalEntityBinding.ccavCompanyName,
            legalEntityBinding.ccavType,
            legalEntityBinding.ccavNIP,
            legalEntityBinding.ccavKRS
        )

        (nameEditTexts + firmFields).forEach { view ->
            when (view) {
                is EditText -> view.addTextChangedListener(watcher)
                is CustomCardActionView -> view.addTextChangedListener(watcher)
            }
        }
    }

    private fun openContactsEditor() {
        // TODO Это должно быть во ViewModel
        val countries = viewModel.countries.value
        if (countries.isNullOrEmpty()) {
            Toast.makeText(context, "Список стран ещё загружается", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = ContactEditBottomSheetDialog()
        dialog.setInitialData(
            initialContacts = viewModel.counterparty.value?.counterpartyContacts ?: emptyList(),
            counterpartyId = viewModel.counterparty.value?.id ?: return,
            countries = countries,
            onSaveCallback = { id, updatedContacts ->
                viewModel.updateContacts(id, updatedContacts)
            }
        )
        dialog.show(childFragmentManager, "edit_contacts")
    }

    private fun setupToolbar() {
        binding.toolbar.btnBack.setOnClickListener {
            tryNavigateWithSaveCheck {
                goBack()
            }
        }

        binding.toolbar.btnEdit.setOnClickListener {
            if (viewModel.hasUnsavedChanges.value == true) {
                // Показать диалог: Сохранить изменения или отменить
                showUnsavedChangesDialog(
                    onSave = { saveChangesFragment() },
                    onDiscard = {
                        val id = counterpartyId
                        if (id != null) {
                            viewModel.loadCounterpartyById(id) // ⬅️ загружаем заново с сервера
                        }

                        viewModel.discardChanges()
                    }
                )
            } else {
                viewModel.toggleEditMode()
            }
        }
    }

    private fun saveDate() {
        binding.btnSaveData.setOnClickListener {
            saveChangesFragment()
        }
    }

    private fun saveChangesFragment() {
        val isLegalEntity = binding.scEntityStatus.isChecked

        if (isLegalEntity) {
            val companyNameValid = viewModel.isCompanyNameValid.value == true
            val nipValid = validateNIPOnSave()
            val krsValid = validateKRSOnSave()

            if (!companyNameValid || !nipValid || !krsValid) {
                showSnackbar("Проверьте поля: НИП, KRS, Название компании")
                return
            }
        }

        val isSupplier = legalEntityBinding.cbSupplier.isChecked
        val isWarehouse = legalEntityBinding.cbWarehouse.isChecked
        val isCustomer = legalEntityBinding.cbCustomer.isChecked

        if (!isSupplier && !isWarehouse && !isCustomer) {
            showSnackbar("Выберите хотя бы один тип компании. По умолчанию выбран 'customer'")
            legalEntityBinding.cbCustomer.isChecked = true
        }

        viewModel.saveChanges(
            shortName = binding.tvShortName.text.toString(),
            firstName = binding.tvFirstName.text.toString(),
            lastName = binding.tvLastName.text.toString(),
            companyName = legalEntityBinding.ccavCompanyName.text.orEmpty().trimEnd(),
            type = legalEntityBinding.ccavType.text.orEmpty(),
            nip = legalEntityBinding.ccavNIP.text.orEmpty(),
            krs = legalEntityBinding.ccavKRS.text.orEmpty(),
            isSupplier = legalEntityBinding.cbSupplier.isChecked,
            isWarehouse = legalEntityBinding.cbWarehouse.isChecked,
            isCustomer = legalEntityBinding.cbCustomer.isChecked,
            isLegalEntity = binding.scEntityStatus.isChecked
        )

        if (viewModel.isEditMode.value == true) {
            viewModel.setEditMode(false)
        }

        // ✅ Отложенный вызов гарантирует доставку результата
        view?.post {
            parentFragmentManager.setFragmentResult("counterparty_updated", Bundle())
        }
    }

    private fun updateToolbarState(isEditMode: Boolean) {
        if (isEditMode) {
            binding.toolbar.btnEdit.setImageResource(R.drawable.ic_edit_red)
            binding.toolbar.tvTitle.text = "Редактирование"
            binding.toolbar.tvTitle.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.red
                )
            )
        } else {
            binding.toolbar.btnEdit.setImageResource(R.drawable.ic_edit)
            binding.toolbar.tvTitle.text = ""
            binding.toolbar.tvTitle.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
            // Тут позже добавить проверку "Есть несохранённые изменения"
        }
    }

    private fun observeViewModel() {
        observeCounterparty()
        observeEditMode()

        viewModel.toastMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeCounterparty() {
        viewModel.counterparty.observe(viewLifecycleOwner) { counterparty ->
            ignoreChanges = true // 🔒 Отключаем реакцию на установку текста

            updateEditableState(viewModel.isEditMode.value ?: false)
            updateCounterpartyInfo(counterparty)

            setupTextWatchers()
            setupChangeListeners()
            setupCompanyNameValidation()
            setupNIPValidation()
            setupKRSValidation()

            ignoreChanges = false // 🔓 Включаем обратно
        }
    }

    private fun setupChangeListeners() {

        val updateTypePreview = {
            val types = mutableListOf<String>()
            if (legalEntityBinding.cbSupplier.isChecked) types.add("supplier")
            if (legalEntityBinding.cbWarehouse.isChecked) types.add("warehouse")
            if (legalEntityBinding.cbCustomer.isChecked) types.add("customer")
            legalEntityBinding.ccavType.text = types.joinToString(", ")
        }

        val listeners = CompoundButton.OnCheckedChangeListener { _, _ ->
            if (!ignoreChanges) {
                viewModel.setHasUnsavedChanges(true)
                updateTypePreview()
            }
        }

        legalEntityBinding.cbSupplier.setOnCheckedChangeListener(listeners)
        legalEntityBinding.cbWarehouse.setOnCheckedChangeListener(listeners)
        legalEntityBinding.cbCustomer.setOnCheckedChangeListener(listeners)

        binding.scEntityStatus.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setHasUnsavedChanges(true)
            binding.scEntityStatus.text = if (isChecked) "Юридическое лицо" else "Физическое лицо"
            updateLegalEntityVisibility(isChecked)
        }
    }

    private fun observeEditMode() {
        viewModel.isEditMode.observe(viewLifecycleOwner) { isEditMode ->
            updateToolbarState(isEditMode)
            updateEditableState(isEditMode)
            updateEditableStateEditText(isEditMode)
            updateEditableStateCheckBoxes(isEditMode)

            if (isEditMode) {
                runInitialValidations()
            }
        }
    }

    private fun updateCounterpartyInfo(counterparty: CounterpartyEntity) {
        val placeholderRes = if (counterparty.isLegalEntity) {
            R.drawable.ic_profile_placeholder_firm_orig
        } else {
            R.drawable.ic_profile_placeholder_user_orig
        }

        Glide.with(requireContext())
            .load(counterparty.imagePath.takeIf { !it.isNullOrBlank() } ?: placeholderRes)
            .placeholder(placeholderRes)
            .circleCrop()
            .into(binding.ivAvatar)

        binding.tvFirstName.apply {
            visibility = if (counterparty.isLegalEntity) View.GONE else View.VISIBLE
            setText(counterparty.firstName)
        }

        binding.tvLastName.apply {
            visibility = if (counterparty.isLegalEntity) View.GONE else View.VISIBLE
            setText(counterparty.lastName)
        }

        binding.tvShortName.setText(counterparty.shortName)

        bindCounterparty(counterparty)
    }

    private fun bindCounterparty(counterparty: CounterpartyEntity) {
        binding.contactsInfo.visibility = View.VISIBLE
        binding.addressesInfo.visibility = View.VISIBLE

        if (counterparty.isLegalEntity) {
            binding.bankInfo.visibility = View.VISIBLE
            legalEntityBinding.layoutLegalEntity.visibility = View.VISIBLE

            val contactText = counterparty.counterpartyContact
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(separator = "\n")
            binding.contactsInfo.text = contactText

            val addressesText = counterparty.addressesInformation
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(separator = "\n")
            binding.addressesInfo.text = addressesText

            val bankAccountText = counterparty.bankAccountInformation
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(separator = "\n")
            binding.bankInfo.text = bankAccountText

            legalEntityBinding.cbSupplier.isChecked = counterparty.isSupplier
            legalEntityBinding.cbWarehouse.isChecked = counterparty.isWarehouse
            legalEntityBinding.cbCustomer.isChecked = counterparty.isCustomer
            legalEntityBinding.ccavCompanyName.setTextAndMode(
                counterparty.companyName ?: "",
                readOnly = true
            )
            legalEntityBinding.ccavType.text = counterparty.type
            legalEntityBinding.ccavNIP.text = counterparty.nip
            legalEntityBinding.ccavKRS.text = counterparty.krs
        } else {
            legalEntityBinding.layoutLegalEntity.visibility = View.GONE
            binding.bankInfo.visibility = View.GONE

            val contactText = counterparty.counterpartyContact
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(separator = "\n")
            binding.contactsInfo.text = contactText

            val addressesText = counterparty.addressesInformation
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(separator = "\n")
            binding.addressesInfo.text = addressesText
        }

        binding.scEntityStatus.isChecked = counterparty.isLegalEntity
        binding.scEntityStatus.text = if (counterparty.isLegalEntity) {
            "Юридическое лицо"
        } else {
            "Физическое лицо"
        }
    }

    private fun updateEditableState(isEditMode: Boolean) {
        binding.scEntityStatus.isEnabled = isEditMode

        if (isEditMode) {
            binding.scEntityStatus.thumbTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.switch_thumb_color)
            binding.scEntityStatus.trackTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.switch_track_color)

            binding.contactsInfo.apply {
                showEditIcon = true
                setInputTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            binding.addressesInfo.apply {
                showEditIcon = true
                setInputTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
            binding.bankInfo.apply {
                showEditIcon = true
                setInputTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            binding.btnSaveData.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.GONE
            binding.btnDeleteAccount.visibility = View.GONE

            // Когда редактируем — слушаем изменения
            binding.scEntityStatus.setOnCheckedChangeListener { _, isChecked ->
                binding.scEntityStatus.text = if (isChecked) {
                    "Юридическое лицо"
                } else {
                    "Физическое лицо"
                }

                viewModel.setHasUnsavedChanges(true) // <-- СТАВИМ, ЧТО ЕСТЬ ИЗМЕНЕНИЯ


                // Вот обработка нажатия на бигунок и изменение видимости некоторых элементов
                updateLegalEntityVisibility(isChecked)
            }
        } else {
            binding.contactsInfo.apply {
                showEditIcon = false
                setInputTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.nayya.uicomponents.R.color.uiKitColorForegroundSecondary
                    )
                )
            }

            binding.addressesInfo.apply {
                showEditIcon = false
                setInputTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.nayya.uicomponents.R.color.uiKitColorForegroundSecondary
                    )
                )
            }

            binding.bankInfo.apply {
                showEditIcon = false
                setInputTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.nayya.uicomponents.R.color.uiKitColorForegroundSecondary
                    )
                )
            }

            binding.btnSaveData.visibility = View.GONE
            binding.btnLogout.visibility = View.VISIBLE
            binding.btnDeleteAccount.visibility = View.VISIBLE

            binding.scEntityStatus.thumbTintList = ContextCompat.getColorStateList(
                requireContext(),
                R.color.switch_thumb_color_disabled
            )
            binding.scEntityStatus.trackTintList = ContextCompat.getColorStateList(
                requireContext(),
                R.color.switch_track_color_disabled
            )
            // Когда НЕ редактируем — убираем слушатель, чтобы не сработал зря
            binding.scEntityStatus.setOnCheckedChangeListener(null)
        }
    }

    private fun updateLegalEntityVisibility(isLegalEntity: Boolean) {
        binding.tvFirstName.visibility = if (isLegalEntity) View.GONE else View.VISIBLE
        binding.tvLastName.visibility = if (isLegalEntity) View.GONE else View.VISIBLE
        binding.includeLegalEntity.layoutLegalEntity.visibility =
            if (isLegalEntity) View.VISIBLE else View.GONE
    }

    private fun updateEditableStateEditText(isEditable: Boolean) {
        val nameEditTexts = listOf(
            binding.tvShortName,
            binding.tvFirstName,
            binding.tvLastName
        )

        val firmFields = listOf(
            legalEntityBinding.ccavCompanyName,
            legalEntityBinding.ccavNIP,
            legalEntityBinding.ccavKRS,
        )

        val textColor = if (isEditable) {
            ContextCompat.getColor(requireContext(), R.color.black)
        } else {
            ContextCompat.getColor(requireContext(), R.color.light_gray)
        }

        nameEditTexts.forEach { editText ->
            editText.apply {
                isEnabled = isEditable
                isFocusable = isEditable
                isFocusableInTouchMode = isEditable
                isCursorVisible = isEditable
                setTextColor(textColor)
            }
        }

        firmFields.forEach { legalEntity ->
            legalEntity.apply {
                showUnderline = isEditable
                showDescriptionIcon = isEditable
                showDescriptionText = isEditable
                isInputEnabled = isEditable
                setInputTextColor(textColor)
            }
        }

        legalEntityBinding.ccavType.apply {
            showUnderline = isEditable
            showDescriptionIcon = isEditable
            showDescriptionText = isEditable
            setInputTextColor(textColor)
        }

        if (isEditable) {
            legalEntityBinding.ccavTypeOverlay.visibility = View.VISIBLE
            legalEntityBinding.ccavTypeOverlay.setOnClickListener {
                showSnackbar("Поле заполняется автоматически")
            }
        } else {
            legalEntityBinding.ccavTypeOverlay.visibility = View.GONE
        }


        if (isEditable) {
            legalEntityBinding.ccavCompanyName.apply {
                setReadOnlyMode(false)
                setInputEllipsize(null)
                setInputMaxLines(2)
            }
        } else {
            legalEntityBinding.ccavCompanyName.apply {
                setReadOnlyMode(true)
                setInputEllipsize(TextUtils.TruncateAt.END)
                setInputMaxLines(1)
            }
        }
    }

    private fun updateEditableStateCheckBoxes(isEditable: Boolean) {
        val checkBoxes = listOf(
            legalEntityBinding.cbSupplier,
            legalEntityBinding.cbWarehouse,
            legalEntityBinding.cbCustomer
        )

        val textColor = if (isEditable) {
            ContextCompat.getColor(requireContext(), R.color.black)
        } else {
            ContextCompat.getColor(requireContext(), R.color.black)
        }

        checkBoxes.forEach { checkBox ->
            checkBox.apply {
                isEnabled = isEditable
                isClickable = isEditable
                setTextColor(textColor)
            }
        }
    }

    private fun showUnsavedChangesDialog(
        onSave: (() -> Unit)? = null,
        onDiscard: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null,
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle("Несохранённые изменения")
            .setMessage("Вы хотите сохранить изменения?")
            .setPositiveButton("Сохранить") { _, _ -> onSave?.invoke() }
            .setNegativeButton("Отменить") { _, _ -> onDiscard?.invoke() }
            .setNeutralButton("Остаться") { _, _ -> onCancel?.invoke() }
            .show()
    }

    private fun hasUnsavedChanges(): Boolean {
        val counterparty = viewModel.counterparty.value ?: return false
        return binding.tvShortName.text.toString() != counterparty.shortName ||
                binding.tvFirstName.text.toString() != (counterparty.firstName ?: "") ||
                binding.tvLastName.text.toString() != (counterparty.lastName ?: "") ||
                binding.scEntityStatus.isChecked != counterparty.isLegalEntity ||
                legalEntityBinding.cbSupplier.isChecked != counterparty.isSupplier ||
                legalEntityBinding.cbWarehouse.isChecked != counterparty.isWarehouse ||
                legalEntityBinding.cbCustomer.isChecked != counterparty.isCustomer ||
                legalEntityBinding.ccavCompanyName.text.orEmpty() != (counterparty.companyName
            ?: "") ||
                legalEntityBinding.ccavType.text.orEmpty() != counterparty.type ||
                legalEntityBinding.ccavNIP.text.orEmpty() != (counterparty.nip ?: "") ||
                legalEntityBinding.ccavKRS.text.orEmpty() != (counterparty.krs ?: "")
    }

    private fun tryNavigateWithSaveCheck(navigateAction: () -> Unit) {
        if (!hasUnsavedChanges()) {
            navigateAction()
            return
        }

        showUnsavedChangesDialog(
            onSave = {
                saveChangesFragment()
                navigateAction()
            },
            onDiscard = {
                viewModel.discardChanges()
                navigateAction()
            },
            onCancel = {
                // остаться — ничего не делаем
            }
        )
    }

    // ВАЛИДАЦИЯ ПОЛЯ ccavCompanyName
    private fun setupCompanyNameValidation() {
        val context = requireContext()
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
                                    descriptionText = resources.getQuantityString(
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
                        getString(R.string.error_max_length, 10)

                    !text.all { it.isDigit() } ->
                        getString(R.string.error_digits_only)

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
                                descriptionText = getString(R.string.exact_length, 10)
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
                                descriptionText = resources.getQuantityString(
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
                        getString(R.string.error_max_length, 10)

                    !text.all { it.isDigit() } ->
                        getString(R.string.error_digits_only)

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
                                descriptionText = getString(R.string.exact_length, 10)
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
                                descriptionText = resources.getQuantityString(
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
            text.length > 10 -> getString(R.string.error_max_length, 10)
            !text.all { it.isDigit() } -> getString(R.string.error_digits_only)
            text.length < 10 -> getString(R.string.error_length, 10)
            else -> null
        }
    }

    private fun validateNIPOnSave(): Boolean {
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

    private fun validateKRSOnSave(): Boolean {
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

    private fun runInitialValidations() {
        val context = requireContext()

        // Company Name
        legalEntityBinding.ccavCompanyName.text = legalEntityBinding.ccavCompanyName.text
        // NIP
        legalEntityBinding.ccavNIP.text = legalEntityBinding.ccavNIP.text
        // KRS
        legalEntityBinding.ccavKRS.text = legalEntityBinding.ccavKRS.text

        viewModel.setHasUnsavedChanges(false)
    }

    companion object {
        private const val ARG_COUNTERPARTY_ID = "counterparty_id"

        @JvmStatic
        fun newInstance(counterpartyId: Long? = null): CounterpartyDetailsFragment {
            return CounterpartyDetailsFragment().apply {
                arguments = Bundle().apply {
                    counterpartyId?.let { putLong(ARG_COUNTERPARTY_ID, it) }
                }
            }
        }
    }
}
