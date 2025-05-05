package com.nayya.myktor.ui.profile.detailscounterparty

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.counterpartyentity.CounterpartyContactRequest
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
    private val _hasUnsavedChanges = MutableLiveData<Boolean>(false)
    val hasUnsavedChanges: LiveData<Boolean> = _hasUnsavedChanges

    private val _countries = MutableLiveData<List<Country>>()
    val countries: LiveData<List<Country>> get() = _countries

    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> get() = _toastMessage

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
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.patchContacts(counterpartyId, contacts)
                }
                if (!response.isSuccessful) {
                    _toastMessage.postValue(Event("Ошибка сохранения: ${response.code()}"))
                    return@launch
                }

                setHasUnsavedChanges(true)
                loadCounterpartyById(counterpartyId) // ← ВСЁ обновится через LiveData
            } catch (e: Exception) {
                Log.e("ViewModel", "Ошибка обновления контактов: ${e.localizedMessage}", e)
                _toastMessage.postValue(Event("Не удалось сохранить контакты"))
            }
        }
    }

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
}
