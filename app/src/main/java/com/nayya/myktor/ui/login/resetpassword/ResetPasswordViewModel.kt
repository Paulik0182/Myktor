package com.nayya.myktor.ui.login.resetpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.ui.login.AuthRepository
import kotlinx.coroutines.launch

class ResetPasswordViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _status = MutableLiveData<ResetPasswordState>()
    val status: LiveData<ResetPasswordState> = _status

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                repository.requestPasswordReset(email)
                _status.value = ResetPasswordState.Success
            } catch (e: Exception) {
                _status.value = ResetPasswordState.Error(e.localizedMessage ?: "Ошибка")
            }
        }
    }
}

class ResetPasswordViewModelFactory(private val repository: AuthRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ResetPasswordViewModel(repository) as T
    }
}

sealed class ResetPasswordState {
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}
