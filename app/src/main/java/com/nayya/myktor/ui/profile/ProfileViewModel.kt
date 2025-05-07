package com.nayya.myktor.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(private val repository: CounterpartyRepository2) : ViewModel() {

    private val _counterparty = MutableLiveData<CounterpartyEntity>()
    val counterparty: LiveData<CounterpartyEntity> = _counterparty

    private val _menuItems = MutableLiveData<List<ProfileMenuType>>()
    val menuItems: LiveData<List<ProfileMenuType>> = _menuItems

    // TODO Перенести в репозиторий или в Интерактор
    fun loadCounterparty(counterpartyId: Long) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.getCounterpartyById(counterpartyId)
                }
                _counterparty.value = result
                _menuItems.value = ProfileMenuType.getVisibleItems(result)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Ошибка загрузки данных: ${e.localizedMessage}", e)
            }
        }
    }
}

interface CounterpartyRepository2 {
    suspend fun getCounterpartyById(id: Long): CounterpartyEntity
}

class DefaultCounterpartyRepository : CounterpartyRepository2 {
    override suspend fun getCounterpartyById(id: Long): CounterpartyEntity {
        return RetrofitInstance.api.getCounterpartyById(id)
    }
}
