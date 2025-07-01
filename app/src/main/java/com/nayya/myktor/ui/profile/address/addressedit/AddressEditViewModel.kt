package com.nayya.myktor.ui.profile.address.addressedit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.data.network.CounterpartyAddressRequest
import com.nayya.myktor.domain.counterpartyentity.City
import com.nayya.myktor.domain.counterpartyentity.CounterpartyAddresse
import com.nayya.myktor.domain.counterpartyentity.Country
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response

class AddressEditViewModel(
    private val repository: AddressEditRepository
) : ViewModel() {

    private val _countries = MutableLiveData<List<Country>>()
    val countries: LiveData<List<Country>> = _countries

    private val _cities = MutableLiveData<List<City>>()
    val cities: LiveData<List<City>> = _cities

    private var selectedCountryId: Long? = null

    val navigateBack = MutableLiveData(false)
    val isLoading = MutableLiveData(false)
    var counterpartyId: Long = 0L
        private set

    val counterpartyName = MutableLiveData<String>()

    private val _isEditMode = MutableLiveData<Boolean>(false)
    val isEditMode: LiveData<Boolean> = _isEditMode

    private val _isRecipientNameValid = MutableLiveData<Boolean>(true)
    val isRecipientNameValid: LiveData<Boolean> = _isRecipientNameValid

    private val _isPostalCodeValid = MutableLiveData<Boolean>(true)
    val isPostalCodeValid: LiveData<Boolean> = _isPostalCodeValid

    private val _isStreetValid = MutableLiveData<Boolean>(true)
    val isStreetValid: LiveData<Boolean> = _isStreetValid

    private val _isHouseNumberValid = MutableLiveData<Boolean>(true)
    val isHouseNumberValid: LiveData<Boolean> = _isHouseNumberValid

    private val _isLocationNumberValid = MutableLiveData<Boolean>(true)
    val isLocationNumberValid: LiveData<Boolean> = _isLocationNumberValid

    private val _isEntranceNumberValid = MutableLiveData<Boolean>(true)
    val isEntranceNumberValid: LiveData<Boolean> = _isEntranceNumberValid

    private val _isFloorValid = MutableLiveData<Boolean>(true)
    val isFloorValid: LiveData<Boolean> = _isFloorValid

    private val _isNumberIntercomValid = MutableLiveData<Boolean>(true)
    val isNumberIntercomValid: LiveData<Boolean> = _isNumberIntercomValid

    val originalAddress = MutableLiveData<CounterpartyAddresse>()
    val formState = MutableLiveData<AddressFormState>()

    val hasUnsavedChanges = MediatorLiveData<Boolean>().apply {
        addSource(formState) { value = compareFormWithOriginal() }
        addSource(originalAddress) { value = compareFormWithOriginal() }
    }

    init {
        loadCountries()
    }

    fun setCounterpartyId(id: Long) {
        counterpartyId = id
        Log.d("AddressEdit", "Counterparty ID set to: $id")
    }

    fun saveAddress(address: CounterpartyAddresse) {
        isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val success = repository.saveOrUpdateAddress(address)
                if (success) {
                    Log.d("AddressEdit", "Адрес успешно сохранён")
                    navigateBack.postValue(true)
                } else {
                    Log.e("AddressEdit", "Ошибка при сохранении адреса")
                }
            } catch (e: Exception) {
                Log.e("AddressEdit",  "Ошибка сети: ${e.localizedMessage}")
                Log.e("AddressEdit", "Ошибка при сохранении адреса", e)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                _countries.postValue(repository.getCountries())
            } catch (e: Exception) {
                Log.e("AddressEditVM", "Error loading countries", e)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun loadCities(countryId: Long) {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                Log.d("AddressEditVM", "Загружаем города для страны $countryId")
                selectedCountryId = countryId
                _cities.postValue(repository.getCitiesByCountry(countryId))
            } catch (e: Exception) {
                Log.e("AddressEditVM", "Error loading cities", e)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun deleteAddress(counterpartyId: Long, addressId: Long) {
        isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val success = repository.deleteAddress(counterpartyId, addressId)
                if (success) {
                    Log.d("AddressEdit", "Адрес успешно удалён")
                    navigateBack.postValue(true)
                } else {
                    Log.e("AddressEdit", "Ошибка при удалении адреса")
                }
            } catch (e: Exception) {
                Log.e("AddressEdit", "Ошибка сети при удалении адреса", e)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun loadCounterpartyName(counterpartyId: Long) {
        viewModelScope.launch {
            try {
                counterpartyName.postValue(repository.getCounterpartyName(counterpartyId))
            } catch (e: Exception) {
                counterpartyName.postValue("")
            }
        }
    }

    // синхронизирует форму с сущностью (использовать при открытии экрана)
    fun setInitialAddress(address: CounterpartyAddresse) {
        originalAddress.value = address
        formState.value = address.toFormState()
    }

    // --- Обновление отдельных полей формы (например, при TextChanged)
    fun updateForm(update: AddressFormState.() -> AddressFormState) {
        formState.value = formState.value?.update() ?: AddressFormState().update()
    }

    private fun compareFormWithOriginal(): Boolean {
        val form = formState.value ?: return false
        val original = originalAddress.value ?: return false
        return !form.equalsEntity(original)
    }

    fun setRecipientNameValid(isValid: Boolean) {
        _isRecipientNameValid.value = isValid
    }

    fun setPostalCodeValid(isValid: Boolean) {
        _isPostalCodeValid.value = isValid
    }

    fun setStreetValid(isValid: Boolean) {
        _isStreetValid.value = isValid
    }

    fun setHouseNumberValid(isValid: Boolean) {
        _isHouseNumberValid.value = isValid
    }

    fun setLocationNumberValid(isValid: Boolean) {
        _isLocationNumberValid.value = isValid
    }

    fun setEntranceNumberValid(isValid: Boolean) {
        _isEntranceNumberValid.value = isValid
    }

    fun setFloorValid(isValid: Boolean) {
        _isFloorValid.value = isValid
    }

    fun setNumberIntercomValid(isValid: Boolean) {
        _isNumberIntercomValid.value = isValid
    }

    fun toggleEditMode() {
        _isEditMode.value = !(_isEditMode.value ?: false)
    }

    fun setEditMode(isEdit: Boolean) {
        _isEditMode.value = isEdit
    }


    // Преобразование сущности в состояние формы (очень важно)
    private fun CounterpartyAddresse.toFormState(): AddressFormState {
        return AddressFormState(
            recipientName = counterpartyFirstLastName?.firstOrNull()
                ?: counterpartyShortName?.firstOrNull() ?: "",
            postalCode = postalCode.orEmpty(),
            streetName = streetName.orEmpty(),
            houseNumber = houseNumber.orEmpty(),
            locationNumber = locationNumber.orEmpty(),
            entranceNumber = entranceNumber.orEmpty(),
            floor = floor.orEmpty(),
            numberIntercom = numberIntercom.orEmpty(),
            countryId = countryId,
            cityId = cityId,
            isMain = isMain
        )
    }

    // Преобразование формы обратно в сущность (для сохранения)
    fun getAddressToSave(): CounterpartyAddresse {
        val form = formState.value!!
        return CounterpartyAddresse(
            id = originalAddress.value?.id,
            counterpartyId = counterpartyId,
            countryId = form.countryId ?: 0L,
            countryName = null, // присвоить если надо
            cityId = form.cityId ?: 0L,
            cityName = null,
            postalCode = form.postalCode,
            streetName = form.streetName,
            houseNumber = form.houseNumber,
            locationNumber = form.locationNumber.takeIf { it.isNotBlank() },
            latitude = null,
            longitude = null,
            entranceNumber = form.entranceNumber.takeIf { it.isNotBlank() },
            floor = form.floor.takeIf { it.isNotBlank() },
            numberIntercom = form.numberIntercom.takeIf { it.isNotBlank() },
            counterpartyContactId = null,
            counterpartyShortName = emptyList(),
            counterpartyFirstLastName = listOf(form.recipientName),
            country = null,
            city = null,
            isMain = form.isMain
        )
    }
}

class AddressEditModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddressEditViewModel(AddressModifyRepository()) as T
    }
}

interface AddressEditRepository {
    suspend fun saveOrUpdateAddress(address: CounterpartyAddresse): Boolean

    suspend fun getCountries(): List<Country>

    suspend fun getCitiesByCountry(countryId: Long): List<City>

    suspend fun deleteAddress(counterpartyId: Long, addressId: Long): Boolean
    suspend fun getCounterpartyName(counterpartyId: Long): String?
}

class AddressModifyRepository : AddressEditRepository {
    private val api = RetrofitInstance.api

    override suspend fun saveOrUpdateAddress(address: CounterpartyAddresse): Boolean {
        return try {
            val request = address.toRequest()
            Log.d("AddressEdit", "saveOrUpdateAddress: ${address.id?.let { "UPDATE id=$it" } ?: "CREATE"}")
            Log.d("AddressEdit", "Request: ${request}")

            val response = if (address.id == null) {
                api.createCounterpartyAddress(
                    address.counterpartyId,
                    request
                )
            } else {
                api.updateCounterpartyAddress(
                    counterpartyId = address.counterpartyId,
                    addressId = address.id,
                    request = request
                )
            }
            Log.d("AddressEdit", "Response: code=${response.code()}, success=${response.isSuccessful}")
            response.errorBody()?.string()?.let {
                Log.e("AddressEdit", "Error body: $it")
            }

            logResponse(response)

            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AddressEdit", "Exception in saveOrUpdateAddress", e)

            false
        }
    }

    override suspend fun getCountries(): List<Country> {
        return api.getCountries()
    }

    override suspend fun getCitiesByCountry(countryId: Long): List<City> {
        return api.getCitiesByCountry(countryId)
    }

    override suspend fun deleteAddress(counterpartyId: Long, addressId: Long): Boolean {
        return try {
            val response = api.deleteCounterpartyAddress(counterpartyId, addressId)
            Log.d("AddressEdit", "Delete response: ${response.code()} ${response.isSuccessful}")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("AddressEdit", "Ошибка при удалении адреса", e)
            false
        }
    }

    override suspend fun getCounterpartyName(counterpartyId: Long): String? {
        return try {
            val counterparty = api.getCounterpartyById(counterpartyId)
            listOfNotNull(
                counterparty?.firstName,
                counterparty?.lastName
            )
                .filter { !it.isNullOrBlank() }
                .joinToString(" ")
                .ifBlank {
                    counterparty?.companyName
                }
                ?.ifBlank {
                    counterparty?.shortName
                }
                ?: "Без имени"
        } catch (e: Exception) {
            null
        }
    }

    private fun logResponse(response: Response<Unit>) {
        Log.d("AddressEdit", "Response: code=${response.code()}, success=${response.isSuccessful}")
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            Log.e("AddressEdit", "Error: ${response.code()} - $errorBody")
        }
    }

    private fun logError(e: Exception) {
        Log.e("AddressEdit", "Exception in saveOrUpdateAddress", e)
        if (e is HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("AddressEdit", "HTTP error: ${e.code()} - $errorBody")
        }
    }

    private fun CounterpartyAddresse.toRequest(): CounterpartyAddressRequest {
        return CounterpartyAddressRequest(
            id = this.id,
            countryId = this.countryId,
            cityId = this.cityId,
            postalCode = this.postalCode,
            streetName = this.streetName,
            houseNumber = this.houseNumber,
            locationNumber = this.locationNumber,
            latitude = this.latitude,
            longitude = this.longitude,
            entranceNumber = this.entranceNumber,
            floor = this.floor,
            numberIntercom = this.numberIntercom,
            isMain = this.isMain,
            fullName = getFullName()
        )
    }

    private fun CounterpartyAddresse.getFullName(): String? {
        return counterpartyFirstLastName?.firstOrNull() ?: counterpartyShortName?.firstOrNull()
    }
}

data class AddressUiModel(
    val id: Long?,
    val fullName: String, // Имя + фамилия (или Название компании)
    val country: String,
    val city: String,
    val street: String,
    val postalCode: String?,
    val houseNumber: String,
    val locationNumber: String?,
    val entranceNumber: String?,
    val floor: String?,
    val numberIntercom: String?,
    val isMain: Boolean = false,
)

data class AddressFormState(
    val recipientName: String = "",
    val postalCode: String = "",
    val streetName: String = "",
    val houseNumber: String = "",
    val locationNumber: String = "",
    val entranceNumber: String = "",
    val floor: String = "",
    val numberIntercom: String = "",
    val countryId: Long? = null,
    val cityId: Long? = null,
    val isMain: Boolean = false
)

fun AddressFormState.equalsEntity(entity: CounterpartyAddresse): Boolean {
    fun String?.normalize() = this?.trim().takeIf { !it.isNullOrBlank() } ?: ""

    return recipientName.normalize() == (entity.counterpartyFirstLastName?.firstOrNull().normalize())
            && postalCode.normalize() == entity.postalCode.normalize()
            && streetName.normalize() == entity.streetName.normalize()
            && houseNumber.normalize() == entity.houseNumber.normalize()
            && locationNumber.normalize() == entity.locationNumber.normalize()
            && entranceNumber.normalize() == entity.entranceNumber.normalize()
            && floor.normalize() == entity.floor.normalize()
            && numberIntercom.normalize() == entity.numberIntercom.normalize()
            && countryId == entity.countryId
            && cityId == entity.cityId
            && isMain == entity.isMain
}
