package com.nayya.myktor.ui.profile.address

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentAddressListBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyAddresse
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.LocaleUtils.goBack
import com.nayya.myktor.utils.viewBinding

class AddressListFragment : BaseFragment(R.layout.fragment_address_list) {

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
    }

    private fun initToolbar() {
        binding.toolbar.btnBack.setOnClickListener {
            exitWithRevealAnimation {
                goBack()
            }
        }
        binding.toolbar.btnSave.setOnClickListener {
            viewModel.saveChanges()
            // оповещение
            view?.post {
                parentFragmentManager.setFragmentResult("counterparty_updated", Bundle())
            }
        }
    }

    private fun initRecyclerView() {
        adapter = AddressListAdapter(
            onEdit = { address -> viewModel.onEditAddress(address) },
            onDelete = { address -> viewModel.deleteAddress(address) },
            onSetMain = { address -> viewModel.setAsMainAddress(address) }
        )
        binding.recyclerViewAddresses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAddresses.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.addresses.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.saveButtonEnabled.observe(viewLifecycleOwner) { enabled ->
            if (enabled) {
                binding.toolbar.btnSave.visibility = View.VISIBLE
            } else {
                binding.toolbar.btnSave.visibility = View.GONE
            }

        }
        viewModel.navigateToEdit.observe(viewLifecycleOwner) { address ->
            address?.let {
                requireController<AddressListFragment.Controller>().openAddressEdit(it)
            }
        }
    }

    private fun initAddButton() {
        binding.btnAddAddress.setOnClickListener {
            viewModel.onAddAddress()
        }
    }

    interface Controller : BaseFragment.Controller {
        fun openAddressEdit(address: CounterpartyAddresse?)
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
