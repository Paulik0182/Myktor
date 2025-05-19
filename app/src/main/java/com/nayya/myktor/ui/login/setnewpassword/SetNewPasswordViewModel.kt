package com.nayya.myktor.ui.login.setnewpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.ui.login.AuthRepository
import kotlinx.coroutines.launch

class SetNewPasswordViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _status = MutableLiveData<SetPasswordState>()
    val status: LiveData<SetPasswordState> = _status

    fun setNewPassword(token: String, password: String) {
        viewModelScope.launch {
            try {
                repository.setNewPassword(token, password)
                _status.value = SetPasswordState.Success
            } catch (e: Exception) {
                _status.value = SetPasswordState.Error(e.localizedMessage ?: "Ошибка")
            }
        }
    }
}

class SetNewPasswordViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SetNewPasswordViewModel(repo) as T
    }
}

sealed class SetPasswordState {
    object Success : SetPasswordState()
    data class Error(val message: String) : SetPasswordState()
}
