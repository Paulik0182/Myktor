package com.nayya.myktor.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
import com.nayya.myktor.domain.loginentity.MeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(private val repository: CounterpartyRepository2) : ViewModel() {

    private val _counterparty = MutableLiveData<CounterpartyEntity>()
    val counterparty: LiveData<CounterpartyEntity> = _counterparty

    private val _menuItems = MutableLiveData<List<ProfileMenuType>>()
    val menuItems: LiveData<List<ProfileMenuType>> = _menuItems

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val me = withContext(Dispatchers.IO) {
                    repository.getMe()
                }

                if (me.counterpartyId != null) {
                    val counterparty = withContext(Dispatchers.IO) {
                        repository.getCounterpartyById(me.counterpartyId)
                    }

                    _counterparty.value = counterparty
                    _menuItems.value = ProfileMenuType.getVisibleItems(counterparty, me.role)
                } else {
                    _counterparty.value = null
                    _menuItems.value = ProfileMenuType.getVisibleItemsForGuest()
                }

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Ошибка получения профиля", e)
                // fallback — как будто не авторизован
                _counterparty.value = null
                _menuItems.value = ProfileMenuType.getVisibleItemsForGuest()
            }
        }
    }
}

class ProfileViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(DefaultCounterpartyRepository()) as T
    }
}

interface CounterpartyRepository2 {
    suspend fun getCounterpartyById(id: Long): CounterpartyEntity
    suspend fun getMe(): MeResponse
}

class DefaultCounterpartyRepository : CounterpartyRepository2 {
    private val api = RetrofitInstance.api // ✅ Используем уже созданный экземпляр

    override suspend fun getCounterpartyById(id: Long): CounterpartyEntity {
        return api.getCounterpartyById(id)
    }

    override suspend fun getMe(): MeResponse {
        return api.getMe()
    }
}
