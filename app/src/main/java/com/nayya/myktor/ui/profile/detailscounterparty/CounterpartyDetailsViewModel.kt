package com.nayya.myktor.ui.profile.detailscounterparty

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.counterpartyentity.CounterpartyContact
import com.nayya.myktor.domain.counterpartyentity.CounterpartyContactRequest
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
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
    private val _hasUnsavedChanges = MutableLiveData<Boolean>(false)
    val hasUnsavedChanges: LiveData<Boolean> = _hasUnsavedChanges

    fun loadCounterpartyById(counterpartyId: Long) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getCounterpartyById(counterpartyId)
                }
                _counterparty.value = result
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CounterpartyDetailsVM", "Ошибка загрузки данных: ${e.localizedMessage}", e)
            }
        }
    }

    fun toggleEditMode() {
        _isEditMode.value = !(_isEditMode.value ?: false)
    }

    fun setEditMode(isEdit: Boolean) {
        _isEditMode.value = isEdit
    }

    fun setHasUnsavedChanges(hasChanges: Boolean) {
        _hasUnsavedChanges.value = hasChanges
    }

    fun saveChanges() {
        // TODO: Реализация сохранения данных на сервер
        setHasUnsavedChanges(false)
        toggleEditMode() // Выходим из режима редактирования
    }

    fun discardChanges() {
        setHasUnsavedChanges(false)
        toggleEditMode() // Выходим из режима редактирования без сохранения
    }

    fun updateContacts(counterpartyId: Long, contacts: List<CounterpartyContactRequest>) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitInstance.api.patchContacts(counterpartyId, contacts)
                }
                setHasUnsavedChanges(true)
                // Опционально: обнови LiveData
                _counterparty.value = _counterparty.value?.copy(
                    counterpartyContacts = contacts.map {
                        CounterpartyContact(
                            id = null, // если ID нет — пропускаем
                            counterpartyId = counterpartyId,
                            counterpartyName = null,
                            contactType = it.contactType ?: "",
                            contactValue = it.contactValue ?: "",
                            countryCodeId = it.countryCodeId,
                            countryName = null,
                            countryPhoneCode = null,
                            countryIsoCode = null,
                            representativeId = null,
                            representativeName = null
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("ViewModel", "Ошибка обновления контактов: ${e.localizedMessage}", e)
            }
        }
    }
}

