package com.nayya.myktor.ui.profile.detailscounterparty

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentCounterpartyDetailsBinding
import com.nayya.myktor.databinding.LayoutLegalEntityBinding
import com.nayya.myktor.databinding.PersonNameFieldsBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
import com.nayya.myktor.ui.login.logoutaccount.ConfirmActionBottomSheet
import com.nayya.myktor.ui.login.logoutaccount.ConfirmActionType
import com.nayya.myktor.ui.profile.contacts.ContactEditBottomSheetDialog
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.LocaleUtils.goBack
import com.nayya.myktor.utils.showSnackbar
import com.nayya.myktor.utils.viewBinding

class CounterpartyDetailsFragment : BaseFragment(R.layout.fragment_counterparty_details) {

    private val binding by viewBinding<FragmentCounterpartyDetailsBinding>()
    private lateinit var viewModel: CounterpartyDetailsViewModel

    private lateinit var legalEntityBinding: LayoutLegalEntityBinding
    private lateinit var personNameDetailsBinding: PersonNameFieldsBinding

    override val enableRevealAnimation = true
    override val revealAnimationOrigin = RevealOrigin.TOP_RIGHT

    private var counterpartyId: Long? = null

    // Для того чтобы скрыть нижнюю навигацию и персчитать размеры container
    override val hideBottomNavigation = true

    private var ignoreChanges = false

    private lateinit var validator: CounterpartyValidationDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        counterpartyId = arguments?.getLong(ARG_COUNTERPARTY_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(CounterpartyDetailsViewModel::class.java)

        counterpartyId?.let { viewModel.loadCounterpartyById(it) }

        legalEntityBinding = LayoutLegalEntityBinding.bind(binding.includeLegalEntity.root)
        personNameDetailsBinding =
            PersonNameFieldsBinding.bind(binding.includePersonNameDetails.root)

        validator = CounterpartyValidationDelegate(
            context = requireContext(),
            viewModel = viewModel,
            binding = personNameDetailsBinding,
            legalEntityBinding = legalEntityBinding
        )

        setupToolbar()
        observeViewModel()

        // TODO Это должно быть во ViewModel
        if (viewModel.countries.value.isNullOrEmpty()) {
            viewModel.loadCountries()
        }

        binding.contactsInfo.setEditClickListener {
            openContactsEditor()
        }

        binding.bankInfo.apply {
            setEditClickListener {
                Toast.makeText(context, "Клик на Банк", Toast.LENGTH_SHORT).show()
            }
        }

        binding.addressesInfo.apply {
            setEditClickListener {
                counterpartyId?.let { counterpartyId ->
                    requireController<CounterpartyDetailsFragment.Controller>().openAddressList(
                        counterpartyId
                    )
                }
            }
        }

        setFragmentResultListener("contacts_updated") { _, _ ->
            counterpartyId?.let {
                viewModel.loadCounterpartyById(it)
            } // ← повторно загружаем с сервера
        }

        setFragmentResultListener("counterparty_updated_details") { _, _ ->
            counterpartyId?.let { id ->
                viewModel.loadCounterpartyById(id)
            }
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

        binding.btnLogout.setOnClickListener {
            ConfirmActionBottomSheet.newInstance(ConfirmActionType.LOGOUT)
                .show(childFragmentManager, "logout")
        }

        binding.btnDeleteAccount.setOnClickListener {
            ConfirmActionBottomSheet.newInstance(ConfirmActionType.DELETE_ACCOUNT)
                .show(childFragmentManager, "delete")
        }
    }

    // Часть механизма отслеживания изменений данных.
    private fun setupTextWatchers() {
        personNameDetailsBinding.ccavShortName.doAfterTextChanged { editable ->
            if (!ignoreChanges) {
                viewModel.updateForm { copy(shortName = editable?.toString().orEmpty()) }
            }
        }

        personNameDetailsBinding.ccavFirstName.doAfterTextChanged { editable ->
            if (!ignoreChanges) {
                viewModel.updateForm { copy(firstName = editable?.toString().orEmpty()) }
            }
        }

        personNameDetailsBinding.ccavLastName.doAfterTextChanged { editable ->
            if (!ignoreChanges) {
                viewModel.updateForm { copy(lastName = editable?.toString().orEmpty()) }
            }
        }

        legalEntityBinding.ccavCompanyName.doAfterTextChanged { editable ->
            if (!ignoreChanges) {
                viewModel.updateForm { copy(companyName = editable?.toString().orEmpty()) }
            }
        }

        legalEntityBinding.ccavNIP.doAfterTextChanged { editable ->
            if (!ignoreChanges) {
                viewModel.updateForm { copy(nip = editable?.toString().orEmpty()) }
            }
        }

        legalEntityBinding.ccavKRS.doAfterTextChanged { editable ->
            if (!ignoreChanges) {
                viewModel.updateForm { copy(krs = editable?.toString().orEmpty()) }
            }
        }

        legalEntityBinding.ccavType.doAfterTextChanged { editable ->
            if (!ignoreChanges) {
                viewModel.updateForm { copy(type = editable?.toString().orEmpty()) }
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
            tryToggleEditModeWithCheck()
//            val isEditMode = viewModel.isEditMode.value == true
//            val hasChanges = hasUnsavedChanges()
//
//            if (isEditMode && hasChanges) {
//                // Показать диалог: Сохранить изменения или отменить
//                showUnsavedChangesDialog(
//                    onSave = { saveChangesFragment() },
//                    onDiscard = {
//                        val id = counterpartyId
//                        if (id != null) {
//                            exitWithRevealAnimation { // анимация при закрытии экрана
//                                viewModel.loadCounterpartyById(id) // ⬅️ загружаем заново с сервера
//                            }
//                        }
//
//                        viewModel.discardChanges()
//                        viewModel.setEditMode(false) // обязательно выйти из режима
//                    }
//                )
//            } else {
//                viewModel.toggleEditMode()
//            }
        }
    }

    private fun saveDate() {
        binding.btnSaveData.setOnClickListener {
            saveChangesFragment()
        }
    }

    private fun saveChangesFragment() {
        val form = viewModel.formState.value ?: return

        // 🔍 Проверка: есть ли вообще изменения
        val original = viewModel.counterparty.value
        if (original != null && form.equalsEntity(original)) {
            showSnackbar("Изменений не было")
            viewModel.setEditMode(false)
            return
        }

        val isLegalEntity = binding.scEntityStatus.isChecked

        if (isLegalEntity) {
            val companyNameValid = viewModel.isCompanyNameValid.value == true
            val nipValid = validator.validateNIPOnSave()
            val krsValid = validator.validateKRSOnSave()

            if (!companyNameValid || !nipValid || !krsValid) {
                showSnackbar("Проверьте поля: НИП, KRS, Название компании")
                return
            }
        }

        // Валидация
        if (form.isLegalEntity && !form.isSupplier && !form.isWarehouse && !form.isCustomer) {
            showSnackbar("Выберите хотя бы один тип компании. По умолчанию выбран 'customer'")
            legalEntityBinding.cbCustomer.isChecked = true
            viewModel.updateForm { copy(isCustomer = true) }
            return
        }

        viewModel.saveChanges(
            shortName = form.shortName,
            firstName = form.firstName,
            lastName = form.lastName,
            companyName = form.companyName,
            type = form.type,
            nip = form.nip,
            krs = form.krs,
            isSupplier = form.isSupplier,
            isWarehouse = form.isWarehouse,
            isCustomer = form.isCustomer,
            isLegalEntity = form.isLegalEntity
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
            validator.setupAll()

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
                viewModel.updateForm {
                    copy(
                        isSupplier = legalEntityBinding.cbSupplier.isChecked,
                        isWarehouse = legalEntityBinding.cbWarehouse.isChecked,
                        isCustomer = legalEntityBinding.cbCustomer.isChecked
                    )
                }
                updateTypePreview()
            }
        }

        legalEntityBinding.cbSupplier.setOnCheckedChangeListener(listeners)
        legalEntityBinding.cbWarehouse.setOnCheckedChangeListener(listeners)
        legalEntityBinding.cbCustomer.setOnCheckedChangeListener(listeners)

        binding.scEntityStatus.setOnCheckedChangeListener { _, isChecked ->
            if (!ignoreChanges) {
                viewModel.updateForm { copy(isLegalEntity = isChecked) }
            }

            binding.scEntityStatus.text = if (isChecked) "Юридическое лицо" else "Физическое лицо"
            updateLegalEntityVisibility(isChecked)
        }

    }

    private fun observeEditMode() {
        viewModel.isEditMode.observe(viewLifecycleOwner) { isEditMode ->
            updateToolbarState(isEditMode)
            updateEditableState(isEditMode)

            viewModel.counterparty.value?.let { counterparty ->
                updateNameFieldsVisibility(counterparty)
            }

            updateEditableStateEditText(isEditMode)
            updateEditableStateCheckBoxes(isEditMode)

            if (isEditMode) {
                validator.runInitialValidations()
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

        updateNameFieldsVisibility(counterparty)

        val isEditMode = viewModel.isEditMode.value == true

        personNameDetailsBinding.ccavShortName.setTextAndMode(
            counterparty.shortName ?: "",
            readOnly = !isEditMode
        )
        bindCounterparty(counterparty)
    }

    private fun updateNameFieldsVisibility(counterparty: CounterpartyEntity) {
        val isEditMode = viewModel.isEditMode.value == true
        val isIndividual = !counterparty.isLegalEntity

        // Поля заполняются только для физических лиц
        val firstNameVisible =
            isIndividual && (isEditMode || !counterparty.firstName.isNullOrBlank())
        val lastNameVisible = isIndividual && (isEditMode || !counterparty.lastName.isNullOrBlank())

        personNameDetailsBinding.ccavFirstName.apply {
            visibility = if (firstNameVisible) View.VISIBLE else View.GONE
            setTextAndMode(counterparty.firstName ?: "", readOnly = !isEditMode)
        }

        personNameDetailsBinding.ccavLastName.apply {
            visibility = if (lastNameVisible) View.VISIBLE else View.GONE
            setTextAndMode(counterparty.lastName ?: "", readOnly = !isEditMode)
        }
    }

    private fun bindCounterparty(counterparty: CounterpartyEntity) {
        val isEditMode = viewModel.isEditMode.value == true
        binding.contactsInfo.visibility = View.VISIBLE
        binding.addressesInfo.visibility = View.VISIBLE

        if (counterparty.isLegalEntity) {
            binding.bankInfo.visibility = View.VISIBLE
            legalEntityBinding.layoutLegalEntity.visibility = View.VISIBLE
            legalEntityBinding.llTypes.visibility = View.VISIBLE

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
                readOnly = !isEditMode
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
//            binding.scEntityStatus.setOnCheckedChangeListener { _, isChecked ->
//                binding.scEntityStatus.text = if (isChecked) {
//                    "Юридическое лицо"
//                } else {
//                    "Физическое лицо"
//                }
//
//                if (!ignoreChanges) {
//                    viewModel.updateForm { copy(isLegalEntity = isChecked) }
//                }
//
//                // Вот обработка нажатия на бигунок и изменение видимости некоторых элементов
//                updateLegalEntityVisibility(isChecked)
//            }
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
//            binding.scEntityStatus.setOnCheckedChangeListener(null)
        }
    }

    private fun updateLegalEntityVisibility(isLegalEntity: Boolean) {
        personNameDetailsBinding.ccavFirstName.visibility =
            if (isLegalEntity) View.GONE else View.VISIBLE
        personNameDetailsBinding.ccavLastName.visibility =
            if (isLegalEntity) View.GONE else View.VISIBLE
        binding.includeLegalEntity.layoutLegalEntity.visibility =
            if (isLegalEntity) View.VISIBLE else View.GONE
        binding.includeLegalEntity.llTypes.visibility =
            if (isLegalEntity) View.VISIBLE else View.GONE
    }

    private fun updateEditableStateEditText(isEditable: Boolean) {

        val firmFields = listOf(
            personNameDetailsBinding.ccavShortName,
            personNameDetailsBinding.ccavFirstName,
            personNameDetailsBinding.ccavLastName,
            legalEntityBinding.ccavCompanyName,
            legalEntityBinding.ccavNIP,
            legalEntityBinding.ccavKRS,
        )

        val textColor = if (isEditable) {
            ContextCompat.getColor(requireContext(), R.color.black)
        } else {
            ContextCompat.getColor(requireContext(), R.color.light_gray)
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

        personNameDetailsBinding.ccavShortName.apply {
            setInputTextColor(ContextCompat.getColor(requireContext(), R.color.black))
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
            personNameDetailsBinding.ccavShortName.apply {
                setReadOnlyMode(false)
            }
            personNameDetailsBinding.ccavFirstName.apply {
                setReadOnlyMode(false)
            }
            personNameDetailsBinding.ccavLastName.apply {
                setReadOnlyMode(false)
            }
            legalEntityBinding.ccavCompanyName.apply {
                setReadOnlyMode(false)
                setInputEllipsize(null)
                setInputMaxLines(2)
            }

        } else {
            personNameDetailsBinding.ccavShortName.apply {
                setReadOnlyMode(true)
            }
            personNameDetailsBinding.ccavFirstName.apply {
                setReadOnlyMode(true)
            }
            personNameDetailsBinding.ccavLastName.apply {
                setReadOnlyMode(true)
            }
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
        val form = viewModel.formState.value ?: return false
        val original = viewModel.counterparty.value ?: return false
        return !form.equalsEntity(original)
    }

    private fun tryNavigateWithSaveCheck(navigateAction: () -> Unit) {
        if (!hasUnsavedChanges()) {
            exitWithRevealAnimation {
                navigateAction() // ← сюда передаётся goBack(), и он уже безопасен
            }
            return
        }

        showUnsavedChangesDialog(
            onSave = {
                saveChangesFragment()
                exitWithRevealAnimation {
                    navigateAction()
                }
            },
            onDiscard = {
                exitWithRevealAnimation { // анимация при закрытии экрана
                    viewModel.discardChanges()
                    navigateAction()
                }
            },
            onCancel = {
                // остаться — ничего не делаем
            }
        )
    }

    private fun tryToggleEditModeWithCheck() {
        val isEditMode = viewModel.isEditMode.value == true
        val hasChanges = hasUnsavedChanges()

        if (isEditMode && hasChanges) {
            showUnsavedChangesDialog(
                onSave = { saveChangesFragment() },
                onDiscard = {
                    viewModel.discardChanges()
                    viewModel.setEditMode(false)
                },
                onCancel = {
                    // ничего не делаем
                }
            )
        } else {
            viewModel.toggleEditMode()
        }
    }

    interface Controller : BaseFragment.Controller {
        fun openAddressList(counterpartyId: Long)
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
