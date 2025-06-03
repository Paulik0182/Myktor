package com.nayya.myktor.ui.profile.address.addressedit

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentAddressEditBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyAddresse
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.LocaleUtils.goBack
import com.nayya.myktor.utils.viewBinding

class AddressEditFragment : BaseFragment(R.layout.fragment_address_edit) {

    private val binding by viewBinding<FragmentAddressEditBinding>()
    private val viewModel: AddressEditViewModel by viewModels {
        AddressEditModelFactory()
    }

    private var address: CounterpartyAddresse? = null

    // Для того чтобы скрыть нижнюю навигацию и персчитать размеры container
    override val hideBottomNavigation = true
    override val enableRevealAnimation = true
    override val revealAnimationOrigin = RevealOrigin.RIGHT_CENTER

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        address = arguments?.getSerializable("address") as? CounterpartyAddresse
        address?.let { viewModel.setCounterpartyId(it.counterpartyId) }

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
        }

        binding.btnApply.setOnClickListener {
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
                locationNumber = binding.ccavLocationNumber.text.toString() ?: null,
                latitude = null,
                longitude = null,
                entranceNumber = binding.ccavEntranceNumber.text.toString() ?: null,
                floor = binding.ccavFloor.text.toString() ?: null,
                numberIntercom = binding.ccavNumberIntercom.text.toString() ?: null,
                counterpartyContactId = null,
                counterpartyShortName = emptyList(),
                counterpartyFirstLastName = emptyList(),
                country = null,
                city = null
            )
            viewModel.saveAddress(newAddress)
        }
    }

    private fun observeViewModel() {
        viewModel.navigateBack.observe(viewLifecycleOwner) { shouldClose ->
            if (shouldClose) goBack()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(address: CounterpartyAddresse? = null): AddressEditFragment {
            return AddressEditFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("address", address)
                }
            }
        }
    }
}