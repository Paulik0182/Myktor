package com.nayya.myktor.ui.profile.address

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentAddressListBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyAddresse
import com.nayya.myktor.ui.login.logoutaccount.ConfirmActionBottomSheet
import com.nayya.myktor.ui.login.logoutaccount.ConfirmActionType
import com.nayya.myktor.ui.profile.address.addressedit.AddressUiModel
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.LocaleUtils.goBack
import com.nayya.myktor.utils.viewBinding
import kotlinx.coroutines.launch

class AddressListFragment : BaseFragment(R.layout.fragment_address_list),
    ConfirmActionBottomSheet.ConfirmActionCallback{

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

    override fun onConfirmDeleteAddress() {
        pendingDeleteAddress?.let { address ->
            viewModel.deleteAddress(address)
            parentFragmentManager.setFragmentResult("counterparty_updated", Bundle())
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

        parentFragmentManager.setFragmentResultListener("counterparty_updated", viewLifecycleOwner) { _, _ ->
            counterpartyId?.let { viewModel.loadAddresses(it) }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
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
            safeExitWithSave {
                exitWithRevealAnimation { goBack() }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.needSave) {
            viewModel.saveChanges()
            viewLifecycleOwner.lifecycleScope.launch {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    parentFragmentManager.setFragmentResult("counterparty_updated", Bundle())
                }
            }
        }
    }

    private fun initRecyclerView() {
        adapter = AddressListAdapter(
            onEdit = { address -> viewModel.onEditAddress(address) },
            onSetMain = { address -> viewModel.setAsMainAddress(address) }
        )
        binding.recyclerViewAddresses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAddresses.adapter = adapter

        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val position = vh.adapterPosition
                val address = adapter.currentList.getOrNull(position) ?: return
                // Показываем BottomSheet и возвращаем item назад:
                adapter.notifyItemChanged(position)
                showConfirmDelete(address)
            }

            override fun onChildDraw(
                c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                // фон и иконка удаления
                val itemView = vh.itemView
                val paint = Paint()
                paint.color = Color.parseColor("#FFFFFFFF")

                if (dX < 0) {
                    // Свайп влево
                    c.drawRect(
                        itemView.right + dX, itemView.top.toFloat(),
                        itemView.right.toFloat(), itemView.bottom.toFloat(),
                        paint
                    )
                    val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
                    val iconMargin = (itemView.height - (icon?.intrinsicHeight ?: 0)) / 2
                    icon?.setBounds(
                        itemView.right - iconMargin - (icon?.intrinsicWidth ?: 0),
                        itemView.top + iconMargin,
                        itemView.right - iconMargin,
                        itemView.bottom - iconMargin
                    )
                    icon?.draw(c)
                }
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerViewAddresses)
    }

    private fun showConfirmDelete(address: AddressUiModel) {
        pendingDeleteAddress = address

        val addressString = listOfNotNull(
            address.country,
            address.city,
            address.street,
            address.houseNumber,
            address.locationNumber
        ).filter { it.isNotBlank() }
            .joinToString(", ")

        val subtitle = "Вы уверены, что хотите удалить адрес:\n$addressString?\nОтменить действие будет невозможно"

        ConfirmActionBottomSheet.newInstance(
            ConfirmActionType.DELETE_ADDRESS,
            subtitle = subtitle,
        ).show(childFragmentManager, "delete_address")
    }

    private fun observeViewModel() {
        viewModel.addresses.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.navigateToEdit.observe(viewLifecycleOwner) { address ->
            address?.let {
                requireController<AddressListFragment.Controller>().openAddressEdit(it)
            }
        }
    }

    private fun initAddButton() {
        binding.btnAddAddress.setOnClickListener {
            safeExitWithSave {
                viewModel.onAddAddress()
                counterpartyId?.let { id ->
                    requireController<AddressListFragment.Controller>().openAddressCreate(
                        id
                    )
                }
            }
        }
    }

    private fun safeExitWithSave(action: () -> Unit) {
        if (viewModel.needSave) {
            viewModel.saveChanges()
            parentFragmentManager.setFragmentResult("counterparty_updated", Bundle())
        }
        action()
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
