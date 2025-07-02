package com.nayya.myktor.ui.profile.address

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentAddressListBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyAddresse
import com.nayya.myktor.ui.dialogs.InfoDialogHelper
import com.nayya.myktor.ui.login.logoutaccount.ConfirmActionBottomSheet
import com.nayya.myktor.ui.login.logoutaccount.ConfirmActionType
import com.nayya.myktor.ui.profile.address.addressedit.AddressUiModel
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.LocaleUtils.goBack
import com.nayya.myktor.utils.viewBinding

class AddressListFragment : BaseFragment(R.layout.fragment_address_list),
    ConfirmActionBottomSheet.ConfirmActionCallback,
    ConfirmActionBottomSheet.OnConfirmSheetClosedListener {

    private val binding by viewBinding<FragmentAddressListBinding>()
    private val viewModel: AddressListViewModel by viewModels {
        AddressListViewModelFactory()
    }

    private lateinit var adapter: AddressListAdapter

    // Для того чтобы скрыть нижнюю навигацию и персчитать размеры container
    override val hideBottomNavigation = true
    override val enableRevealAnimation = true
    override val revealAnimationOrigin = RevealOrigin.RIGHT_CENTER

    private var counterpartyId: Long? = null

    private var pendingDeleteAddress: AddressUiModel? = null

    private var wasChanged = false

    private var isDeleteSheetShown = false

    override fun onConfirmDeleteAddress() {
        pendingDeleteAddress?.let { address ->
            viewModel.deleteAddress(address)
            wasChanged = true
            pendingDeleteAddress = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        counterpartyId = arguments?.getLong(ARG_COUNTERPARTY_ADDRESS_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.btnEdit.visibility = View.GONE

        initToolbar()
        initRecyclerView()
        observeViewModel()
        initAddButton()

        counterpartyId?.let { viewModel.loadAddresses(it) }

        parentFragmentManager.setFragmentResultListener(
            "counterparty_updated",
            viewLifecycleOwner
        ) { _, _ ->
            isDeleteSheetShown = false
            counterpartyId?.let { viewModel.loadAddresses(it) }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    sendResultIfNeeded()
                    if (viewModel.needSave) {
                        viewModel.saveChanges()
                        parentFragmentManager.setFragmentResult("counterparty_updated", Bundle())
                    }
                    exitWithRevealAnimation { goBack() }
                }
            }
        )
    }

    private fun initToolbar() {
        binding.toolbar.btnBack.setOnClickListener {
            sendResultIfNeeded()
            safeExitWithSave {
                exitWithRevealAnimation { goBack() }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.needSave) {
            viewModel.saveChanges()
            wasChanged = true
        }
    }

    private fun sendResultIfNeeded() {
        if (wasChanged) {
            parentFragmentManager.setFragmentResult("counterparty_updated_details", Bundle())
            wasChanged = false
        }
    }

    private fun initRecyclerView() {
        adapter = AddressListAdapter(
            onEdit = { address -> viewModel.onEditAddress(address) },
            onSetMain = { address -> viewModel.setAsMainAddress(address) }
        )
        binding.recyclerViewAddresses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAddresses.adapter = adapter

        val freeSwipe = FreeSwipeCallback(
            requireContext(),
            onDelete = { position ->
                val address = adapter.currentList.getOrNull(position) ?: return@FreeSwipeCallback
                showConfirmDelete(address)
            },
            onSwipingStateChanged = { isSwiping ->
                adapter.setSwiping(isSwiping) // Передаем состояние свайпа в адаптер
            }
        )
        freeSwipe.attachTo(binding.recyclerViewAddresses)
    }

    private fun showConfirmDelete(address: AddressUiModel) {
        if (isDeleteSheetShown || childFragmentManager.findFragmentByTag("delete_address") != null) {
            return
        }
        isDeleteSheetShown = true // ← Защита от повторного показа

        pendingDeleteAddress = address

        val addressString = listOfNotNull(
            address.country,
            address.postalCode,
            address.city,
            address.street,
            address.houseNumber,
            address.locationNumber
        ).filter { it.isNotBlank() }
            .joinToString(", ")

        val subtitle =
            "Вы уверены, что хотите удалить адрес:\n$addressString?\nОтменить действие будет невозможно"

        val sheet = ConfirmActionBottomSheet.newInstance(
            ConfirmActionType.DELETE_ADDRESS,
            subtitle = subtitle,
        )
        sheet.show(childFragmentManager, "delete_address")
    }

    private fun observeViewModel() {
        viewModel.addresses.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.navigateToEdit.observe(viewLifecycleOwner) { address ->
            address?.let {
                requireController<AddressListFragment.Controller>().openAddressEdit(it)
            }
        }

        viewModel.addressCount.observe(viewLifecycleOwner) { count ->
            binding.toolbar.tvTitle.text = "Адреса $count/${AddressListViewModel.MAX_ADDRESSES}"
        }
    }

    override fun onDestroyView() {
        binding.recyclerViewAddresses.setOnTouchListener(null)
        super.onDestroyView()
    }

    private fun initAddButton() {
        binding.btnAddAddress.setOnClickListener {
            // Проверяем в момент нажатия
            if ((viewModel.addressCount.value ?: 0) >= AddressListViewModel.MAX_ADDRESSES) {
                InfoDialogHelper.show(
                    requireContext(),
                    "Максимально можно внести пять адресов.\n" +
                            "Вы можете удалить не нужный адрес сдвинув выбранный адрес влево."
                )
            } else {
                safeExitWithSave {
                    viewModel.onAddAddress()
                    counterpartyId?.let { id ->
                        requireController<AddressListFragment.Controller>().openAddressCreate(id)
                    }
                }
            }
        }
    }

    private fun safeExitWithSave(action: () -> Unit) {
        if (viewModel.needSave) {
            viewModel.saveChanges()
//            parentFragmentManager.setFragmentResult("counterparty_updated", Bundle())
            wasChanged = true
        }
        action()
    }

    override fun onConfirmSheetClosed() {
        isDeleteSheetShown = false
    }

    interface Controller : BaseFragment.Controller {
        fun openAddressEdit(address: CounterpartyAddresse?)
        fun openAddressCreate(counterpartyId: Long)
    }

    companion object {
        private const val ARG_COUNTERPARTY_ADDRESS_ID = "counterparty_address_id"

        @JvmStatic
        fun newInstance(counterpartyId: Long? = null): AddressListFragment {
            return AddressListFragment().apply {
                arguments = Bundle().apply {
                    counterpartyId?.let { putLong(ARG_COUNTERPARTY_ADDRESS_ID, it) }
                }
            }
        }
    }
}
