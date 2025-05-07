package com.nayya.myktor.ui.profile.detailscounterparty

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
import com.nayya.myktor.ui.profile.contacts.ContactEditBottomSheetDialog
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.LocaleUtils.goBack
import com.nayya.myktor.utils.viewBinding

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

        val firmEditTexts = listOf(
            legalEntityBinding.etCompanyName,
            legalEntityBinding.etType,
            legalEntityBinding.etNIP,
            legalEntityBinding.etKRS
        )

        (nameEditTexts + firmEditTexts).forEach { it.addTextChangedListener(watcher) }
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
                    onDiscard = { viewModel.discardChanges() }
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
        viewModel.saveChanges(
            shortName = binding.tvShortName.text.toString(),
            firstName = binding.tvFirstName.text.toString(),
            lastName = binding.tvLastName.text.toString(),
            companyName = legalEntityBinding.etCompanyName.text.toString(),
            type = legalEntityBinding.etType.text.toString(),
            nip = legalEntityBinding.etNIP.text.toString(),
            krs = legalEntityBinding.etKRS.text.toString(),
            isSupplier = legalEntityBinding.cbSupplier.isChecked,
            isWarehouse = legalEntityBinding.cbWarehouse.isChecked,
            isCustomer = legalEntityBinding.cbCustomer.isChecked,
            isLegalEntity = binding.scEntityStatus.isChecked
        )
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

            ignoreChanges = false // 🔓 Включаем обратно
        }
    }

    private fun setupChangeListeners() {
        val listeners = CompoundButton.OnCheckedChangeListener { _, _ ->
            if (!ignoreChanges) {
                viewModel.setHasUnsavedChanges(true)
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
            legalEntityBinding.etCompanyName.setText(counterparty.companyName)
            legalEntityBinding.etType.setText(counterparty.type)
            legalEntityBinding.etNIP.setText(counterparty.nip)
            legalEntityBinding.etKRS.setText(counterparty.krs)
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

        val firmEditTexts = listOf(
            legalEntityBinding.etCompanyName,
            legalEntityBinding.etType,
            legalEntityBinding.etNIP,
            legalEntityBinding.etKRS,
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

        firmEditTexts.forEach { editText ->
            editText.apply {
                isEnabled = isEditable
                isFocusable = isEditable
                isFocusableInTouchMode = isEditable
                isCursorVisible = isEditable
                setTextColor(textColor)
            }
        }

        legalEntityBinding.etCompanyName.minLines = if (isEditable) 2 else 1
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
                legalEntityBinding.etCompanyName.text.toString() != (counterparty.companyName
            ?: "") ||
                legalEntityBinding.etType.text.toString() != counterparty.type ||
                legalEntityBinding.etNIP.text.toString() != (counterparty.nip ?: "") ||
                legalEntityBinding.etKRS.text.toString() != (counterparty.krs ?: "")
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
