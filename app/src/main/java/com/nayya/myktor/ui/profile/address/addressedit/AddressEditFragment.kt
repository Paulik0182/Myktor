package com.nayya.myktor.ui.profile.address.addressedit

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentAddressEditBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyAddresse
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.LocaleUtils.goBack
import com.nayya.myktor.utils.showSnackbar
import com.nayya.myktor.utils.viewBinding

class AddressEditFragment : BaseFragment(R.layout.fragment_address_edit) {

    private val binding by viewBinding<FragmentAddressEditBinding>()
    private val viewModel: AddressEditViewModel by viewModels {
        AddressEditModelFactory()
    }

    private var address: CounterpartyAddresse? = null
    private var counterpartyId: Long? = null

    // Для того чтобы скрыть нижнюю навигацию и персчитать размеры container
    override val hideBottomNavigation = true
    override val enableRevealAnimation = true
    override val revealAnimationOrigin = RevealOrigin.RIGHT_CENTER

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        address = arguments?.getSerializable("address") as? CounterpartyAddresse
        counterpartyId = arguments?.getLong(COUNTERPARTY_ADDRESS_ID)

        // Устанавливаем counterpartyId в зависимости от режима
        when {
            address != null -> {
                // Режим редактирования - берем из существующего адреса
                viewModel.setCounterpartyId(address!!.counterpartyId)
            }
            counterpartyId != null -> {
                // Режим создания - берем из аргументов
                viewModel.setCounterpartyId(counterpartyId!!)
            }
            else -> {
                // Ошибка - нет нужных данных
                showSnackbar("Не указан контрагент")
                goBack()
                return
            }
        }

        initToolbar()
        initViews()
        observeViewModel()
    }

    private fun initToolbar() {
        binding.toolbar.btnBack.setOnClickListener {
            exitWithRevealAnimation {
                goBack()
            }
        }
    }

    private fun initViews() {
        address?.let {
            binding.etCountry.setText(it.countryName)
            binding.etCity.setText(it.cityName)
            binding.ccavPostalCode.text = it.postalCode
            binding.ccavStreet.text = it.streetName
            binding.ccavHouseNumber.text = it.houseNumber
            binding.ccavLocationNumber.text = it.locationNumber
            binding.ccavEntranceNumber.text = it.entranceNumber
            binding.ccavFloor.text = it.floor
            binding.ccavNumberIntercom.text = it.numberIntercom
            binding.cbIsMain.isChecked = it.isMain
        }

        binding.btnApply.setOnClickListener {
            createOrUpdateAddress()
        }
    }

    private fun createOrUpdateAddress() {
        val newAddress = CounterpartyAddresse(
            id = address?.id,
            counterpartyId = viewModel.counterpartyId,
            countryId = viewModel.resolveCountryId(binding.etCountry.text.toString()),
            countryName = binding.etCountry.text.toString(),
            cityId = viewModel.resolveCityId(binding.etCity.text.toString()),
            cityName = binding.etCity.text.toString(),
            postalCode = binding.ccavPostalCode.text.toString(),
            streetName = binding.ccavStreet.text.toString(),
            houseNumber = binding.ccavHouseNumber.text.toString(),
            locationNumber = binding.ccavLocationNumber.text.toString().takeIf { it.isNotEmpty() },
            latitude = null,
            longitude = null,
            entranceNumber = binding.ccavEntranceNumber.text.toString().takeIf { it.isNotEmpty() },
            floor = binding.ccavFloor.text.toString().takeIf { it.isNotEmpty() },
            numberIntercom = binding.ccavNumberIntercom.text.toString().takeIf { it.isNotEmpty() },
            counterpartyContactId = null,
            counterpartyShortName = emptyList(),
            counterpartyFirstLastName = emptyList(),
            country = null,
            city = null,
            isMain = binding.cbIsMain.isChecked
        )

        Log.d("AddressEdit", "Создан/обновлен адрес: $newAddress")
        viewModel.saveAddress(newAddress)
    }

    private fun observeViewModel() {
        viewModel.navigateBack.observe(viewLifecycleOwner) { shouldClose ->
            if (shouldClose) {
                parentFragmentManager.setFragmentResult("counterparty_updated", Bundle())
                parentFragmentManager.setFragmentResult("counterparty_updated_details", Bundle())
                goBack()
            }
        }
    }

    companion object {
        private const val COUNTERPARTY_ADDRESS_ID = "counterparty_address_id"

        @JvmStatic
        fun newInstance(
            address: CounterpartyAddresse? = null,
            counterpartyId: Long? = null
        ): AddressEditFragment {
            return AddressEditFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("address", address)
                    counterpartyId?.let { putLong(COUNTERPARTY_ADDRESS_ID, it) }
                }
            }
        }
    }
}