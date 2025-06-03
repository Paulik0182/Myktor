package com.nayya.myktor.ui.profile.address.addressedit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.data.network.CounterpartyAddressRequest
import com.nayya.myktor.domain.counterpartyentity.CounterpartyAddresse
import kotlinx.coroutines.launch

class AddressEditViewModel(private val repository: AddressEditRepository) : ViewModel() {


    val navigateBack = MutableLiveData(false)

    var counterpartyId: Long = 0L
        private set

    fun setCounterpartyId(id: Long) {
        counterpartyId = id
    }

    fun saveAddress(address: CounterpartyAddresse) {
        viewModelScope.launch {
            repository.saveOrUpdateAddress(address)
            navigateBack.postValue(true)
        }
    }

    fun resolveCountryId(countryName: String): Long {
        // Пока заглушка.
        return 1L
    }

    fun resolveCityId(cityName: String): Long {
        // Пока заглушка.
        return 1L
    }
}

class AddressEditModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddressEditViewModel(AddressModifyRepository()) as T
    }
}

interface AddressEditRepository {
    suspend fun saveOrUpdateAddress(address: CounterpartyAddresse): Boolean
}

class AddressModifyRepository : AddressEditRepository {
    private val api = RetrofitInstance.api

    override suspend fun saveOrUpdateAddress(address: CounterpartyAddresse): Boolean {
        return try {
            val request = address.toRequest()
            val response = if (address.id == null) {
                api.createCounterpartyAddress(address.counterpartyId, request)
            } else {
                api.updateCounterpartyAddress(address.id, request)
            }
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
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
            isMain = this.isMain
        )
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
