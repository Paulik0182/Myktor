package com.nayya.myktor.ui.profile.address.addressedit

import android.app.AlertDialog
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
import com.nayya.myktor.ui.login.logoutaccount.ConfirmActionBottomSheet
import com.nayya.myktor.ui.login.logoutaccount.ConfirmActionType
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.LocaleUtils.goBack
import com.nayya.myktor.utils.showSnackbar
import com.nayya.myktor.utils.viewBinding

class AddressEditFragment : BaseFragment(R.layout.fragment_address_edit),
    ConfirmActionBottomSheet.ConfirmActionCallback {

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

    override fun onConfirmDeleteAddress() {
        address?.let { addr ->
            viewModel.deleteAddress(addr.counterpartyId, addr.id ?: return@let)
        }
    }

    // Для того чтобы скрыть нижнюю навигацию и персчитать размеры container
    override val hideBottomNavigation = true
    override val enableRevealAnimation = true
    override val revealAnimationOrigin = RevealOrigin.RIGHT_CENTER

    private var isFirstCountryLoad = true
    private var isFirstCityLoad = true
    private var isCountryChangedByUser = false

    private lateinit var validator: AddressFieldsValidationDelegate

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

        if (address != null) {
            viewModel.setInitialAddress(address!!)  // ← вот это важно!
        } else {
            // Новый адрес: создаём пустую форму вручную (можно доработать)
            viewModel.formState.value = AddressFormState()
        }

        validator = AddressFieldsValidationDelegate(
            context = requireContext(),
            binding = binding,
            viewModel = viewModel
        )

        validator.setupAll()

        viewModel.setEditMode(true)

        initToolbar()
        initViews()
        setupCountrySelection()
        setupCitySelection()
        observeViewModel()

        if (address == null && counterpartyId != null) {
            viewModel.loadCounterpartyName(counterpartyId!!)
        }

        viewModel.counterpartyName.observe(viewLifecycleOwner) { name ->
            if (address == null && binding.ccavRecipientName.text.isNullOrBlank()) {
                binding.ccavRecipientName.text = name
            }
        }
    }

    private fun initToolbar() {
        binding.toolbar.btnEdit.visibility = View.GONE
        binding.toolbar.btnSave.visibility = View.GONE
        binding.toolbar.btnDelete.visibility = if (address != null) View.VISIBLE else View.GONE

        binding.toolbar.btnBack.setOnClickListener {
            tryNavigateWithSaveCheck {
                exitWithRevealAnimation {
                    goBack()
                }
            }
        }

        binding.toolbar.btnDelete.setOnClickListener {
            ConfirmActionBottomSheet
                .newInstance(
                    ConfirmActionType.DELETE_ADDRESS,
                    subtitle = "Вы уверены, что хотите удалить этот адрес?\nОтменить действие будет невозможно."
                )
                .show(childFragmentManager, "delete_address")
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

            val fullName = it.counterpartyFirstLastName?.firstOrNull()
                ?: it.counterpartyShortName?.firstOrNull()
                ?: ""
            binding.ccavRecipientName.text = fullName
        }

        binding.btnApply.setOnClickListener {
            validator.trimAllFieldsOnSave()
            createOrUpdateAddress()
        }
    }

    private fun createOrUpdateAddress() {

        // Проверка валидации через ViewModel
        if (!viewModel.isRecipientNameValid.value!! ||
            !viewModel.isPostalCodeValid.value!! ||
            !viewModel.isStreetValid.value!! ||
            !viewModel.isHouseNumberValid.value!!
        ) {
            showSnackbar("Проверьте обязательные поля")
            return
        }

        val selectedCountry = binding.includeSpinnerCountry.spinner.selectedItem as? Country
        val selectedCity = binding.includeSpinnerCity.spinner.selectedItem as? City

        if (selectedCountry == null || selectedCity == null) {
            showSnackbar("Выберите страну и город")
            return
        }

        // Обновить формстейт перед сохранением
        viewModel.updateForm {
            copy(
                countryId = selectedCountry.id,
                cityId = selectedCity.id
            )
        }

//        val recipientName = binding.ccavRecipientName.text.toString().trim()
        val newAddress = viewModel.getAddressToSave()
//        val newAddress2 = CounterpartyAddresse(
//            id = address?.id,
//            counterpartyId = viewModel.counterpartyId,
//            countryId = selectedCountry.id ?: 0L,
//            countryName = selectedCountry.name,
//            cityId = selectedCity.id ?: 0L,
//            cityName = selectedCity.name,
//            postalCode = binding.ccavPostalCode.text.toString(),
//            streetName = binding.ccavStreet.text.toString(),
//            houseNumber = binding.ccavHouseNumber.text.toString(),
//            locationNumber = binding.ccavLocationNumber.text.toString().takeIf { it.isNotEmpty() },
//            latitude = null,
//            longitude = null,
//            entranceNumber = binding.ccavEntranceNumber.text.toString().takeIf { it.isNotEmpty() },
//            floor = binding.ccavFloor.text.toString().takeIf { it.isNotEmpty() },
//            numberIntercom = binding.ccavNumberIntercom.text.toString().takeIf { it.isNotEmpty() },
//            counterpartyContactId = null,
//            counterpartyShortName = emptyList(),
//            counterpartyFirstLastName = listOf(recipientName),
//            country = null,
//            city = null,
//            isMain = binding.cbIsMain.isChecked
//        )

        Log.d("AddressEdit", "Создан/обновлен адрес: $newAddress")
        viewModel.saveAddress(newAddress)
    }

    private fun updateCityList(cities: List<City>, selectCityId: Long? = null) {
        val emptyCity = City(id = null, name = "")

        // Если в режиме редактирования текущий город отсутствует в пришедших городах — добавим его
        var newCityList: MutableList<City> = when {
            isFirstCityLoad && address != null -> {
                val exists = cities.any { it.id == address!!.cityId }
                val list = if (!exists && address!!.cityId != null) {
                    cities.toMutableList().apply {
                        add(0, City(id = address!!.cityId, name = address!!.cityName ?: ""))
                    }
                } else {
                    cities.toMutableList()
                }
                list
            }

            else -> cities.toMutableList()
        }

        // Только если режим создания ИЛИ пользователь сменил страну — добавляем пустой город
        if (isCountryChangedByUser || (address == null && isFirstCityLoad)) {
            newCityList = mutableListOf(emptyCity).apply { addAll(newCityList) }
        }

        cityList = newCityList

        // Обновляем адаптер
        citySpinnerAdapter.clear()
        citySpinnerAdapter.addAll(cityList)
        citySpinnerAdapter.notifyDataSetChanged()

        // По умолчанию всегда выбираем либо selectCityId, либо первый элемент (пустой)
        val cityIndex = cityList.indexOfFirst { it.id == selectCityId }
        binding.includeSpinnerCity.spinner.setSelection(if (cityIndex >= 0) cityIndex else 0, false)
    }

    private fun setupCountrySelection() {
        binding.includeSpinnerCountry.spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    val country = countrySpinnerAdapter.getItem(position)
                    country?.let {
                        Log.d("@@@", "Выбрана страна: $country")

                        if (!isFirstCountryLoad) {
                            isCountryChangedByUser = true
                            viewModel.loadCities(it.id!!)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupCitySelection() {
        binding.includeSpinnerCity.spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
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

            if (isFirstCountryLoad) {
                val defaultCountryIndex = if (address == null) {
                    // Режим создания - выбираем Польшу (id 1)
                    countries.indexOfFirst { it.id == 1L }
                } else {
                    // Режим редактирования - выбираем страну из адреса
                    countries.indexOfFirst { it.id == address!!.countryId }
                }
                if (defaultCountryIndex >= 0) {
                    binding.includeSpinnerCountry.spinner.setSelection(defaultCountryIndex, false)
                }
                isFirstCountryLoad = false
            }
        }

        viewModel.cities.observe(viewLifecycleOwner) { cities ->
            Log.d("@@@", "Города получены: size=${cities.size}, list=$cities")

            // Выбираем нужный город:
            val selectCityId = when {
                isFirstCityLoad && address != null -> address!!.cityId    // редактирование
                isFirstCityLoad && address == null -> 1L                  // режим создания, Варшава
                isCountryChangedByUser -> null                            // смена страны - пустой город
                else -> null
            }

            updateCityList(cities, selectCityId)

            isFirstCityLoad = false
            isCountryChangedByUser = false
        }

        viewModel.navigateBack.observe(viewLifecycleOwner) { shouldClose ->
            if (shouldClose) {
                parentFragmentManager.setFragmentResult("counterparty_updated", Bundle())
                parentFragmentManager.setFragmentResult("counterparty_updated_details", Bundle())
                goBack()
            }
        }
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
                createOrUpdateAddress()
                exitWithRevealAnimation { navigateAction() }
            },
            onDiscard = {
                viewModel.setInitialAddress(viewModel.originalAddress.value!!)
                exitWithRevealAnimation { navigateAction() }
            },
            onCancel = {
                // остаться — ничего не делаем
            }
        )
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
        val original = viewModel.originalAddress.value ?: return false
        return !form.equalsEntity(original)
    }

    companion object {
        private const val COUNTERPARTY_ADDRESS_ID = "counterparty_address_id"

        @JvmStatic
        fun newInstance(
            address: CounterpartyAddresse? = null,
            counterpartyId: Long? = null,
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
