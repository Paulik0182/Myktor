package com.nayya.myktor.ui.profile.detailscounterparty

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentCounterpartyDetailsBinding
import com.nayya.myktor.databinding.LayoutCardActionBinding
import com.nayya.myktor.databinding.LayoutLegalEntityBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
import com.nayya.myktor.utils.viewBinding

class CounterpartyDetailsFragment : Fragment(R.layout.fragment_counterparty_details) {

    private val binding by viewBinding<FragmentCounterpartyDetailsBinding>()
    private lateinit var viewModel: CounterpartyDetailsViewModel

    private lateinit var legalEntityBinding: LayoutLegalEntityBinding
    private lateinit var contactsBinding: LayoutCardActionBinding
    private lateinit var addressesBinding: LayoutCardActionBinding
    private lateinit var bankBinding: LayoutCardActionBinding

    private var counterpartyId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        counterpartyId = arguments?.getLong(ARG_COUNTERPARTY_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(CounterpartyDetailsViewModel::class.java)

        counterpartyId?.let { viewModel.loadCounterpartyById(it) }

        legalEntityBinding = LayoutLegalEntityBinding.bind(binding.includeLegalEntity.root)
        contactsBinding = LayoutCardActionBinding.bind(binding.includeContactsInfo.root)
        addressesBinding = LayoutCardActionBinding.bind(binding.includeAddressesInfo.root)
        bankBinding = LayoutCardActionBinding.bind(binding.includeBankInfo.root)

        setupToolbar()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.btnEdit.setOnClickListener {
            if (viewModel.hasUnsavedChanges.value == true) {
                // Показать диалог: Сохранить изменения или отменить
                showUnsavedChangesDialog()
            } else {
                viewModel.toggleEditMode()
            }
        }
    }

    private fun updateToolbarState(isEditMode: Boolean) {
        if (isEditMode) {
            binding.toolbar.btnEdit.setImageResource(R.drawable.ic_edit_red)
            binding.toolbar.tvTitle.text = "Редактирование"
            binding.toolbar.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        } else {
            binding.toolbar.btnEdit.setImageResource(R.drawable.ic_edit)
            binding.toolbar.tvTitle.text = ""
            binding.toolbar.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            // Тут позже добавить проверку "Есть несохранённые изменения"
        }
    }

    private fun observeViewModel() {
        observeCounterparty()
        observeEditMode()
    }

    private fun observeCounterparty() {
        viewModel.counterparty.observe(viewLifecycleOwner) { counterparty ->
            updateEditableState(viewModel.isEditMode.value ?: false)
            updateCounterpartyInfo(counterparty)
        }
    }

    private fun observeEditMode() {
        viewModel.isEditMode.observe(viewLifecycleOwner) { isEditMode ->
            updateToolbarState(isEditMode)
            updateEditableState(isEditMode)
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
            text = counterparty.firstName
        }

        binding.tvLastName.apply {
            visibility = if (counterparty.isLegalEntity) View.GONE else View.VISIBLE
            text = counterparty.lastName
        }

        binding.tvShortName.text = counterparty.shortName

        bindCounterparty(counterparty)
    }

    private fun bindCounterparty(counterparty: CounterpartyEntity) {
        contactsBinding.cardActionContainer.visibility = View.VISIBLE
        addressesBinding.cardActionContainer.visibility = View.VISIBLE

        // TODO Положить логику на эти кнопки. При режиме редактирования не должнобыть кнопок
        //  btnLogout, btnDeleteAccount. А в обычном режиме не должно быть кнопки btnSaveData
        binding.btnLogout.visibility = View.VISIBLE
        binding.btnDeleteAccount.visibility = View.VISIBLE
        binding.btnSaveData.visibility = View.VISIBLE

        if (counterparty.isLegalEntity) {
            bankBinding.cardActionContainer.visibility = View.VISIBLE
            legalEntityBinding.layoutLegalEntity.visibility = View.VISIBLE

            contactsBinding.tvActionTitle.apply {
                val contactsInfo = counterparty.representativesContact?.firstOrNull()
                if (contactsInfo.isNullOrBlank()) {
                    hint = "Контакты"
                } else {
                    setText(contactsInfo)
                }
            }

            counterparty.addressesInformation?.firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?.let { addressesBinding.tvActionTitle.setText(it) }
                ?: run { addressesBinding.tvActionTitle.hint = "Адрес" }

            with(bankBinding.tvActionTitle) {
                val bankInfo = counterparty.bankAccountInformation?.firstOrNull()
                if (bankInfo.isNullOrBlank()) hint = "Банковский счет" else setText(bankInfo)
            }

            legalEntityBinding.cbSupplier.isChecked = counterparty.isSupplier
            legalEntityBinding.cbWarehouse.isChecked = counterparty.isWarehouse
            legalEntityBinding.cbCustomer.isChecked = counterparty.isCustomer
            legalEntityBinding.etCompanyName.setText(counterparty.companyName)
            legalEntityBinding.etType.setText(counterparty.type)
            legalEntityBinding.etNIP.setText(counterparty.nip)
            legalEntityBinding.etKRS.setText(counterparty.krs)
        } else {
            legalEntityBinding.layoutLegalEntity.visibility = View.GONE
            bankBinding.cardActionContainer.visibility = View.GONE

            val representativeInfo = counterparty.counterpartyContact?.firstOrNull()
            if (representativeInfo.isNullOrBlank()) {
                contactsBinding.tvActionTitle.hint = "Контакты"
            } else {
                contactsBinding.tvActionTitle.setText(representativeInfo)
            }

            counterparty.addressesInformation?.firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?.let { addressesBinding.tvActionTitle.setText(it) }
                ?: run { addressesBinding.tvActionTitle.hint = "Адрес" }
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
            // Когда редактируем — слушаем изменения
            binding.scEntityStatus.setOnCheckedChangeListener { _, isChecked ->
                binding.scEntityStatus.text = if (isChecked) {
                    "Юридическое лицо"
                } else {
                    "Физическое лицо"
                }

                viewModel.setHasUnsavedChanges(true) // <-- СТАВИМ, ЧТО ЕСТЬ ИЗМЕНЕНИЯ
            }
        } else {
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

    private fun showUnsavedChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Несохранённые изменения")
            .setMessage("Вы хотите сохранить изменения?")
            .setPositiveButton("Сохранить") { _, _ ->
                viewModel.saveChanges()
            }
            .setNegativeButton("Отменить") { _, _ ->
                viewModel.discardChanges()
            }
            .show()
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
