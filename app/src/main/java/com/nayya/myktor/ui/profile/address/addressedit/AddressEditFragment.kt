package com.nayya.myktor.ui.profile.address.addressedit

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.viewModels
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentAddressEditBinding
import com.nayya.myktor.domain.counterpartyentity.City
import com.nayya.myktor.domain.counterpartyentity.CounterpartyAddresse
import com.nayya.myktor.domain.counterpartyentity.Country
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

    private lateinit var countrySpinnerAdapter: CountrySpinnerAdapter
    private lateinit var citySpinnerAdapter: CitySpinnerAdapter
    private var countryList: List<Country> = emptyList()
    private var cityList: List<City> = emptyList()


    // Для того чтобы скрыть нижнюю навигацию и персчитать размеры container
    override val hideBottomNavigation = true
    override val enableRevealAnimation = true
    override val revealAnimationOrigin = RevealOrigin.RIGHT_CENTER

    private var isFirstCountryLoad = true
    private var isFirstCityLoad = true
    val cityPlaceholder = City(id = null, name = "Город", countryId = null)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        address = arguments?.getSerializable("address") as? CounterpartyAddresse
        counterpartyId = arguments?.getLong(COUNTERPARTY_ADDRESS_ID)

        countrySpinnerAdapter = CountrySpinnerAdapter(requireContext(), countryList.toMutableList())
        citySpinnerAdapter = CitySpinnerAdapter(requireContext(), cityList.toMutableList())

        binding.includeSpinnerCountry.spinner.adapter = countrySpinnerAdapter
        binding.includeSpinnerCity.spinner.adapter = citySpinnerAdapter

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
        setupCountrySelection()
        setupCitySelection()
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
        binding.includeSpinnerCountry.tvDescription.text = "Страна"
        binding.includeSpinnerCity.tvDescription.text = "Город"

        address?.let {
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
        val selectedCountry = binding.includeSpinnerCountry.spinner.selectedItem as? Country
        val selectedCity = binding.includeSpinnerCity.spinner.selectedItem as? City

        if (selectedCountry == null || selectedCity == null) {
            showSnackbar("Выберите страну и город")
            return
        }

        val newAddress = CounterpartyAddresse(
            id = address?.id,
            counterpartyId = viewModel.counterpartyId,
            countryId = selectedCountry.id ?: 0L,
            countryName = selectedCountry.name,
            cityId = selectedCity.id ?: 0L,
            cityName = selectedCity.name,
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

    private fun setupCountrySelection() {


        binding.includeSpinnerCountry.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val country = countrySpinnerAdapter.getItem(position)
                country?.let {
                    Log.d("@@@", "Выбрана страна: $country")

                    // Очищаем города и показываем placeholder "Город"
                    cityList = emptyList()
                    citySpinnerAdapter.clear()
                    citySpinnerAdapter.add(cityPlaceholder)
                    citySpinnerAdapter.notifyDataSetChanged()

                    isFirstCityLoad = false // чтобы не выбирать автоматически город после загрузки
                    viewModel.loadCities(it.id!!)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupCitySelection() {
        binding.includeSpinnerCity.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Можно добавить дополнительную логику при выборе города
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
        viewModel.countries.observe(viewLifecycleOwner) { countries ->
            countryList = countries
            countrySpinnerAdapter.clear()
            countrySpinnerAdapter.addAll(countries)
            countrySpinnerAdapter.notifyDataSetChanged()

            // Только при первом запуске — выставляем нужную страну!
            if (isFirstCountryLoad) {
                val defaultCountryIndex = if (address == null) {
                    countries.indexOfFirst { it.id == 1L } // Польша
                } else {
                    countries.indexOfFirst { it.id == address!!.countryId }
                }
                if (defaultCountryIndex >= 0) {
                    binding.includeSpinnerCountry.spinner.setSelection(defaultCountryIndex)
                }
                isFirstCountryLoad = false
            }
        }
        viewModel.cities.observe(viewLifecycleOwner) { cities ->
            Log.d("@@@", "Города получены: size=${cities.size}, list=$cities")

            cityList = cities
            citySpinnerAdapter.clear()
            citySpinnerAdapter.addAll(cities)
            citySpinnerAdapter.notifyDataSetChanged()

            // В режиме создания: если страна Польша — по умолчанию выбрать Варшаву
            if (isFirstCityLoad && address == null && (binding.includeSpinnerCountry.spinner.selectedItem as? Country)?.id == 1L) {
                val warsawIndex = cities.indexOfFirst { it.id == 1L }
                if (warsawIndex >= 0) binding.includeSpinnerCity.spinner.setSelection(warsawIndex)
            }
            // В режиме редактирования: если город совпадает с адресом
            else if (isFirstCityLoad && address != null) {
                val cityIndex = cities.indexOfFirst { it.id == address!!.cityId }
                if (cityIndex >= 0) binding.includeSpinnerCity.spinner.setSelection(cityIndex)
            }
            isFirstCityLoad = false
        }

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
