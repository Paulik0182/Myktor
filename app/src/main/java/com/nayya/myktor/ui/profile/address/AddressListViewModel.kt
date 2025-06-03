package com.nayya.myktor.ui.profile.address

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.data.network.CounterpartyAddressRequest
import com.nayya.myktor.domain.counterpartyentity.CounterpartyAddresse
import com.nayya.myktor.ui.profile.address.addressedit.AddressUiModel
import kotlinx.coroutines.launch

class AddressListViewModel(private val repository: AddressListRepository) : ViewModel() {

    private val _addresses = MutableLiveData<List<AddressUiModel>>()
    val addresses: LiveData<List<AddressUiModel>> = _addresses

    private val _saveButtonEnabled = MutableLiveData(false)
    val saveButtonEnabled: LiveData<Boolean> = _saveButtonEnabled

    private val _navigateToEdit = MutableLiveData<CounterpartyAddresse?>()
    val navigateToEdit: LiveData<CounterpartyAddresse?> = _navigateToEdit

    private val addressEntities = mutableListOf<CounterpartyAddresse>()
    private var counterpartyId: Long = 0L

    private val originalStates = mutableMapOf<Long, Boolean>()

    private var originalMainAddressId: Long? = null
    private var currentMainAddressId: Long? = null
    private var initialStates = mutableMapOf<Long, Boolean>()
    fun loadAddresses(counterpartyId: Long) {
        this.counterpartyId = counterpartyId
        viewModelScope.launch {
            val loaded = repository.getAddresses(counterpartyId)
            addressEntities.clear()
            addressEntities.addAll(loaded)

            // Сохраняем исходные состояния isMain
            initialStates.clear()
            loaded.forEach { address ->
                address.id?.let { id -> // Проверяем, что id не null
                    initialStates[id] = address.isMain
                }
            }

            originalMainAddressId = loaded.firstOrNull { it.isMain }?.id
            currentMainAddressId = originalMainAddressId

            _addresses.postValue(loaded.map { it.toUiModel() })
            _saveButtonEnabled.postValue(false)
        }
    }

    private fun hasChanges(): Boolean {
        // Проверяем, изменился ли main адрес
        val mainChanged = currentMainAddressId != originalMainAddressId

        // Проверяем, вернулись ли мы к исходному состоянию
        val backToInitial = currentMainAddressId == originalMainAddressId &&
                originalMainAddressId != null

        return if (backToInitial) {
            // Если вернулись к исходному - изменений нет
            false
        } else {
            // Иначе проверяем, был ли изменен main адрес
            mainChanged
        }
    }

    fun onAddAddress() {
        _navigateToEdit.postValue(null)
    }

    fun onEditAddress(address: AddressUiModel) {
        val original = addressEntities.find { it.id == address.id }
        _navigateToEdit.postValue(original)
    }

    fun deleteAddress(address: AddressUiModel) {
        addressEntities.removeAll { it.id == address.id }
        _addresses.postValue(addressEntities.map { it.toUiModel() })
        _saveButtonEnabled.postValue(true)
    }

    fun setAsMainAddress(address: AddressUiModel) {
        // Если уже выбран - ничего не делаем
        if (currentMainAddressId == address.id) return

        // Обновляем текущий main адрес
        currentMainAddressId = address.id

        // Обновляем UI
        addressEntities.replaceAll {
            if (it.id == address.id) it.copy(isMain = true)
            else it.copy(isMain = false)
        }
        _addresses.postValue(addressEntities.map { it.toUiModel() })

        // Проверяем изменения
        _saveButtonEnabled.postValue(hasChanges())
    }

    fun saveChanges() {
        viewModelScope.launch {
            val success =
                repository.updateAddressList(counterpartyId, addressEntities.map { it.toRequest() })
            if (success) {
                // После сохранения обновляем исходное состояние
                originalMainAddressId = currentMainAddressId
                // Обновляем initialStates
                initialStates.clear()
                addressEntities.forEach { address ->
                    address.id?.let { id -> // Проверяем, что id не null
                        initialStates[id] = address.isMain
                    }
                }
                _saveButtonEnabled.postValue(false)
            }
        }
    }

    private fun CounterpartyAddresse.toUiModel(): AddressUiModel {
        return AddressUiModel(
            id = this.id,
            fullName = counterpartyFirstLastName?.firstOrNull() ?: "",
            country = countryName ?: "",
            city = cityName ?: "",
            street = streetName,
            postalCode = postalCode,
            houseNumber = houseNumber,
            locationNumber = locationNumber,
            entranceNumber = entranceNumber,
            floor = floor,
            numberIntercom = numberIntercom,
            isMain = this.isMain
        )
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
            isMain = this.isMain
        )
    }
}

class AddressListViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddressListViewModel(AddressPreviewRepository()) as T
    }
}

interface AddressListRepository {
    suspend fun getAddresses(counterpartyId: Long): List<CounterpartyAddresse>
    suspend fun updateAddressList(
        counterpartyId: Long,
        addresses: List<CounterpartyAddressRequest>,
    ): Boolean
}

class AddressPreviewRepository : AddressListRepository {
    private val api = RetrofitInstance.api

    override suspend fun getAddresses(counterpartyId: Long): List<CounterpartyAddresse> {
        return try {
            api.getCounterpartyById(counterpartyId)?.counterpartyAddresses ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun updateAddressList(
        counterpartyId: Long,
        addresses: List<CounterpartyAddressRequest>,
    ): Boolean {
        return try {
            val response = api.updateAllAddresses(counterpartyId, addresses)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
