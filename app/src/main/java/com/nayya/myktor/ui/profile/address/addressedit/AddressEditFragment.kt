package com.nayya.myktor.ui.profile.address.addressedit

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
    ConfirmActionBottomSheet.ConfirmActionCallback,
    ConfirmActionBottomSheet.OnConfirmSheetClosedListener  {

    private val binding by viewBinding<FragmentAddressEditBinding>()
    private val viewModel: AddressEditViewModel by viewModels {
        AddressEditModelFactory()
    }

    private var address: CounterpartyAddresse? = null
    private var counterpartyId: Long? = null

    private lateinit var countryAdapter: CountryFilterAdapter
    private lateinit var cityAdapter: CityFilterAdapter
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

    private var isDeleteSheetShown = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        address = arguments?.getSerializable("address") as? CounterpartyAddresse
        counterpartyId = arguments?.getLong(COUNTERPARTY_ADDRESS_ID)

        // Инициализация адаптеров
        countryAdapter = CountryFilterAdapter(requireContext(), countryList)
        cityAdapter = CityFilterAdapter(requireContext(), cityList)

        // Устанавливаем адаптеры для AutoCompleteTextView
        binding.includeSpinnerCountry.autoCompleteTextView.setAdapter(countryAdapter)
        binding.includeSpinnerCity.autoCompleteTextView.setAdapter(cityAdapter)

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
            viewModel = viewModel,
            coroutineScope = viewLifecycleOwner.lifecycleScope
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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackPressed()
                }
            }
        )
    }

    private fun initToolbar() {
        binding.toolbar.btnEdit.visibility = View.GONE
        binding.toolbar.btnSave.visibility = View.GONE
        binding.toolbar.btnDelete.visibility = if (address != null) View.VISIBLE else View.GONE

        binding.toolbar.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.toolbar.btnDelete.setOnClickListener {
            // ЗАЩИТА от повторного открытия!
            if (isDeleteSheetShown) return@setOnClickListener
            isDeleteSheetShown = true

            ConfirmActionBottomSheet
                .newInstance(
                    ConfirmActionType.DELETE_ADDRESS,
                    subtitle = "Вы уверены, что хотите удалить этот адрес?\nОтменить действие будет невозможно."
                )
                .show(childFragmentManager, "delete_address")
        }
    }

    private fun handleBackPressed() {
        tryNavigateWithSaveCheck {
            exitWithRevealAnimation {
                goBack()
            }
        }
    }

    private fun initViews() {
        binding.includeSpinnerCountry.tvDescription.text = "Страна"
        binding.includeSpinnerCity.tvDescription.text = "Город"

        // Настройка AutoCompleteTextView
        binding.includeSpinnerCountry.autoCompleteTextView.threshold = 1 // начинать поиск после 1 символа
        binding.includeSpinnerCity.autoCompleteTextView.threshold = 1

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

            binding.cbIsMain.isChecked = it.isMain ?: false
        }

        // ВАЖНО: подписка на изменение чекбокса
        binding.cbIsMain.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateForm { copy(isMain = isChecked) }
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

        // Получаем выбранные значения
        val countryId = binding.includeSpinnerCountry.autoCompleteTextView.getTag(R.id.country_id_tag) as? Long
        val cityId = binding.includeSpinnerCity.autoCompleteTextView.getTag(R.id.city_id_tag) as? Long

        val selectedCountry = countryList.find { it.id == countryId }
        val selectedCity = cityList.find { it.id == cityId }

        if (selectedCountry == null || selectedCity == null) {
            showSnackbar("Выберите страну и город из списка")
            return
        }

        // Обновить формстейт перед сохранением
        viewModel.updateForm {
            copy(
                countryId = selectedCountry.id,
                cityId = selectedCity.id
            )
        }

        val newAddress = viewModel.getAddressToSave()

        Log.d("AddressEdit", "Создан/обновлен адрес: $newAddress")
        viewModel.saveAddress(newAddress)
    }

    private fun updateCityList(cities: List<City>, selectCityId: Long? = null) {
        val newCityList = cities.toMutableList()

        // Добавляем текущий город, если он не в списке (для режима редактирования)
        if (isFirstCityLoad && address != null && address!!.cityId != null) {
            val exists = cities.any { it.id == address!!.cityId }
            if (!exists) {
                newCityList.add(0, City(id = address!!.cityId, name = address!!.cityName ?: ""))
            }
        }

        cityList = newCityList
        cityAdapter = CityFilterAdapter(requireContext(), cityList)
        binding.includeSpinnerCity.autoCompleteTextView.setAdapter(cityAdapter)

        // Устанавливаем выбранный город
        if (selectCityId != null) {
            val selectedCity = cityList.find { it.id == selectCityId }
            selectedCity?.let {
                binding.includeSpinnerCity.autoCompleteTextView.post {
                    binding.includeSpinnerCity.autoCompleteTextView.setText(it.name, false)
                    binding.includeSpinnerCity.autoCompleteTextView.setTag(R.id.city_id_tag, it.id)
                }
            }
        } else if (isCountryChangedByUser) {
            binding.includeSpinnerCity.autoCompleteTextView.post {
                binding.includeSpinnerCity.autoCompleteTextView.text.clear()
                binding.includeSpinnerCity.autoCompleteTextView.setTag(R.id.city_id_tag, null)
            }
        }
    }

    private fun setupCountrySelection() {
        binding.includeSpinnerCountry.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val country = countryAdapter.getItem(position)
            country?.let {
                binding.includeSpinnerCountry.autoCompleteTextView.setTag(R.id.country_id_tag, it.id)
                if (!isFirstCountryLoad) {
                    isCountryChangedByUser = true
                    viewModel.loadCities(it.id!!)
                }
            }
        }

        binding.includeSpinnerCountry.autoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val currentText = binding.includeSpinnerCountry.autoCompleteTextView.text.toString()
                val exists = countryList.any { it.name == currentText }
                if (!exists) {
                    binding.includeSpinnerCountry.autoCompleteTextView.text.clear()
                    binding.includeSpinnerCountry.autoCompleteTextView.setTag(R.id.country_id_tag, null)
                }
            }
        }
    }

    private fun setupCitySelection() {
        // Можно добавить обработчик выбора города, если нужно
        binding.includeSpinnerCity.autoCompleteTextView.setOnItemClickListener { _, _, _, _ ->
            // Дополнительная логика при выборе города
        }
    }

    private fun observeViewModel() {
        viewModel.countries.observe(viewLifecycleOwner) { countries ->
            countryList = countries
            countryAdapter = CountryFilterAdapter(requireContext(), countries)
            binding.includeSpinnerCountry.autoCompleteTextView.setAdapter(countryAdapter)

            if (isFirstCountryLoad) {
                val defaultCountry = if (address == null) {
                    countries.find { it.id == 1L } // Польша по умолчанию
                } else {
                    countries.find { it.id == address!!.countryId }
                }

                defaultCountry?.let { country ->
                    binding.includeSpinnerCountry.autoCompleteTextView.post {
                        binding.includeSpinnerCountry.autoCompleteTextView.setText(country.name, false)
                        binding.includeSpinnerCountry.autoCompleteTextView.setTag(R.id.country_id_tag, country.id)

                        // Загружаем города для выбранной страны
                        viewModel.loadCities(country.id!!)
                    }
                }
                isFirstCountryLoad = false
            }
        }

        viewModel.cities.observe(viewLifecycleOwner) { cities ->
            val selectCityId = when {
                isFirstCityLoad && address != null -> address!!.cityId
                isFirstCityLoad && address == null -> {
                    // Для Польши выбираем Варшаву (id=1)
                    if (binding.includeSpinnerCountry.autoCompleteTextView.getTag(R.id.country_id_tag) == 1L) {
                        1L
                    } else null
                }
                else -> null
            }

            updateCityList(cities, selectCityId)
            isFirstCityLoad = false
            isCountryChangedByUser = false
        }

        viewModel.navigateBack.observe(viewLifecycleOwner) { shouldClose ->
            if (shouldClose) {
                exitWithRevealAnimation {
                    parentFragmentManager.setFragmentResult("counterparty_updated", Bundle())
                    parentFragmentManager.setFragmentResult(
                        "counterparty_updated_details",
                        Bundle()
                    )
                    goBack()
                }
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

    override fun onConfirmSheetClosed() {
        isDeleteSheetShown = false
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
