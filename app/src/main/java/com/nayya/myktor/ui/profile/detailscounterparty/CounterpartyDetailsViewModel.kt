package com.nayya.myktor.ui.profile.detailscounterparty

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.data.network.CounterpartyContactRequest
import com.nayya.myktor.data.network.CounterpartyPatchRequest
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
import com.nayya.myktor.domain.counterpartyentity.Country
import com.nayya.myktor.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CounterpartyDetailsViewModel : ViewModel() {

    private val _counterparty = MutableLiveData<CounterpartyEntity>()
    val counterparty: LiveData<CounterpartyEntity> = _counterparty

    // Режим редактирования.
    private val _isEditMode = MutableLiveData<Boolean>(false)
    val isEditMode: LiveData<Boolean> = _isEditMode

    // Флаг не сохраненых данных.
    // ✅ Состояние текущей формы (что введено в UI)
    private val _formState = MutableLiveData<CounterpartyFormState>()
    val formState: LiveData<CounterpartyFormState> = _formState

    // ✅ [MODIFIED] hasUnsavedChanges теперь сравнивает formState и оригинальные данные
    val hasUnsavedChanges = MediatorLiveData<Boolean>().apply {
        addSource(_formState) { value = compareFormWithOriginal() }
        addSource(_counterparty) { value = compareFormWithOriginal() }
    }

    private val _countries = MutableLiveData<List<Country>>()
    val countries: LiveData<List<Country>> get() = _countries

    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> get() = _toastMessage

    private val _isCompanyNameValid = MutableLiveData<Boolean>(true)
    val isCompanyNameValid: LiveData<Boolean> = _isCompanyNameValid

    private val _isNipValid = MutableLiveData<Boolean>(true)
    val isNipValid: LiveData<Boolean> = _isNipValid

    private val _isKrsValid = MutableLiveData<Boolean>(true)
    val isKrsValid: LiveData<Boolean> = _isKrsValid

    // TODO Перенести в репозиторий или в Интерактор
    fun loadCounterpartyById(counterpartyId: Long) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getCounterpartyById(counterpartyId)
                }
                _counterparty.value = result

                // ✅ Сброс formState на базу оригинала
                _formState.value = CounterpartyFormState(
                    shortName = result.shortName.orEmpty(),
                    firstName = result.firstName.orEmpty(),
                    lastName = result.lastName.orEmpty(),
                    companyName = result.companyName.orEmpty(),
                    nip = result.nip.orEmpty(),
                    krs = result.krs.orEmpty(),
                    type = result.type.orEmpty(),
                    isLegalEntity = result.isLegalEntity,
                    isSupplier = result.isSupplier,
                    isWarehouse = result.isWarehouse,
                    isCustomer = result.isCustomer
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CounterpartyDetailsVM", "Ошибка загрузки данных: ${e.localizedMessage}", e)
            }
        }
    }

    // ✅ Метод обновления полей формы
    fun updateForm(update: CounterpartyFormState.() -> CounterpartyFormState) {
        _formState.value = _formState.value?.update() ?: CounterpartyFormState().update()
    }

    // ✅ Сравниваем formState с оригиналом
    private fun compareFormWithOriginal(): Boolean {
        val form = _formState.value ?: return false
        val original = _counterparty.value ?: return false
        return !form.equalsEntity(original)
    }

    fun toggleEditMode() {
        _isEditMode.value = !(_isEditMode.value ?: false)
    }

    fun setEditMode(isEdit: Boolean) {
        _isEditMode.value = isEdit
    }

    fun discardChanges() {
        _isEditMode.value = false
        _formState.value = _counterparty.value?.let { original ->
            CounterpartyFormState(
                shortName = original.shortName.orEmpty(),
                firstName = original.firstName.orEmpty(),
                lastName = original.lastName.orEmpty(),
                companyName = original.companyName.orEmpty(),
                nip = original.nip.orEmpty(),
                krs = original.krs.orEmpty(),
                type = original.type.orEmpty(),
                isLegalEntity = original.isLegalEntity,
                isSupplier = original.isSupplier,
                isWarehouse = original.isWarehouse,
                isCustomer = original.isCustomer
            )
        }
    }

    // TODO Перенести в репозиторий или в Интерактор
    fun updateContacts(counterpartyId: Long, contacts: List<CounterpartyContactRequest>) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.patchContacts(counterpartyId, contacts)
                }
                if (!response.isSuccessful) {
                    _toastMessage.postValue(Event("Ошибка сохранения: ${response.code()}"))
                    return@launch
                }

                loadCounterpartyById(counterpartyId) // ← ВСЁ обновится через LiveData
            } catch (e: Exception) {
                Log.e("ViewModel", "Ошибка обновления контактов: ${e.localizedMessage}", e)
                _toastMessage.postValue(Event("Не удалось сохранить контакты"))
            }
        }
    }

    // TODO Перенести в репозиторий или в Интерактор
    fun loadCountries() {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getCountries()
                }
                _countries.value = result
            } catch (e: Exception) {
                Log.e("ViewModel", "Ошибка загрузки стран: ${e.localizedMessage}", e)
            }
        }
    }

    fun saveChanges(
        shortName: String,
        firstName: String?,
        lastName: String?,
        companyName: String?,
        type: String,
        nip: String?,
        krs: String?,
        isSupplier: Boolean,
        isWarehouse: Boolean,
        isCustomer: Boolean,
        isLegalEntity: Boolean,
    ) {
        val old = _counterparty.value ?: return

        val patchRequest = CounterpartyPatchRequest(
            shortName = shortName,
            companyName = companyName,
            type = type,
            isSupplier = isSupplier,
            isWarehouse = isWarehouse,
            isCustomer = isCustomer,
            isLegalEntity = isLegalEntity,
            nip = nip,
            krs = krs,
            firstName = firstName,
            lastName = lastName
        )

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.patchBasicFields(
                        old.id ?: return@withContext null,
                        patchRequest
                    )
                }

                if (result != null && result.isSuccessful) {
                    _toastMessage.postValue(Event("Изменения сохранены"))
                    loadCounterpartyById(old.id!!)
                } else {
                    _toastMessage.postValue(Event("Ошибка при сохранении основных полей"))
                }
            } catch (e: Exception) {
                _toastMessage.postValue(Event("Ошибка сети: ${e.localizedMessage}"))
            }
        }
    }

    fun setCompanyNameValid(isValid: Boolean) {
        _isCompanyNameValid.value = isValid
    }

    fun setNipValid(isValid: Boolean) {
        _isNipValid.value = isValid
    }

    fun setKrsValid(isValid: Boolean) {
        _isKrsValid.value = isValid
    }
}

// ✅ [NEW FILE or placed in same ViewModel file]
data class CounterpartyFormState(
    val shortName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val companyName: String = "",
    val nip: String = "",
    val krs: String = "",
    val type: String = "",
    val isLegalEntity: Boolean = true,
    val isSupplier: Boolean = false,
    val isWarehouse: Boolean = false,
    val isCustomer: Boolean = false
)

fun CounterpartyFormState.equalsEntity(entity: CounterpartyEntity): Boolean {
    return shortName == (entity.shortName ?: "") &&
            firstName == (entity.firstName ?: "") &&
            lastName == (entity.lastName ?: "") &&
            companyName == (entity.companyName ?: "") &&
            nip == (entity.nip ?: "") &&
            krs == (entity.krs ?: "") &&
            type == (entity.type ?: "") &&
            isLegalEntity == entity.isLegalEntity &&
            isSupplier == entity.isSupplier &&
            isWarehouse == entity.isWarehouse &&
            isCustomer == entity.isCustomer
}
