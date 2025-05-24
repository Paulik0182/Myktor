package com.nayya.myktor.ui.login.logoutaccount

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.data.prefs.TokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConfirmActionViewModel(private val repository: LogoutRepository) : ViewModel() {

    private val _actionCompleted = MutableLiveData<Unit>()
    val actionCompleted: LiveData<Unit> get() = _actionCompleted

    fun logoutCurrentDevice() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.logout()
                }
                TokenStorage.clear()
                _actionCompleted.postValue(Unit)
            } catch (e: Exception) {
                Log.e("ConfirmActionVM", "Ошибка выхода: ${e.message}", e)
            }
        }
    }

    fun logoutAllDevices() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.logoutAll()
                }
                TokenStorage.clear()
                _actionCompleted.postValue(Unit)
            } catch (e: Exception) {
                Log.e("ConfirmActionVM", "Ошибка выхода: ${e.message}", e)
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteAccount()
                }
                TokenStorage.clear()
                _actionCompleted.postValue(Unit)
            } catch (e: Exception) {
                Log.e("ConfirmActionVM", "Ошибка удаления: ${e.message}", e)
            }
        }
    }
}

class LogoutViewModelFactory(private val repository: LogoutRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfirmActionViewModel::class.java)) {
            return ConfirmActionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class LogoutState {
    data class Success(val token: String) : LogoutState()
    data class Error(val code: String, val message: String) : LogoutState()
}

interface LogoutRepository {
    suspend fun logout()
    suspend fun logoutAll()
    suspend fun deleteAccount()
}

class DefaultLogoutRepository : LogoutRepository {
    override suspend fun logout() {
        RetrofitInstance.api.logout()
    }

    override suspend fun logoutAll() {
        RetrofitInstance.api.logoutAll()
    }

    override suspend fun deleteAccount(){
        RetrofitInstance.api.deleteAccount()
    }
}
