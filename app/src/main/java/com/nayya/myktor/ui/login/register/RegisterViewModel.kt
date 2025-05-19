package com.nayya.myktor.ui.login.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.domain.loginentity.RegisterRequest
import com.nayya.myktor.ui.login.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _status = MutableLiveData<RegisterState>()
    val status: LiveData<RegisterState> = _status

    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                repository.register(RegisterRequest(email, password))
                _status.value = RegisterState.Success
            } catch (e: Exception) {
                _status.value = RegisterState.Error(e.localizedMessage ?: "Ошибка регистрации")
            }
        }
    }
}

class RegisterViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel(repository) as T
    }
}

sealed class RegisterState {
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}
