package com.nayya.myktor.ui.login.workingpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.domain.loginentity.RegisterRequest
import com.nayya.myktor.ui.login.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {
    private val _state = MutableLiveData<AuthState>()
    val state: LiveData<AuthState> = _state

    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                repo.register(RegisterRequest(email, password))
                _state.value = AuthState.Success("Вы зарегистрированы")
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.localizedMessage ?: "Ошибка регистрации")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                repo.requestPasswordReset(email)
                _state.value = AuthState.Success("Письмо отправлено")
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.localizedMessage ?: "Ошибка отправки")
            }
        }
    }

    fun setNewPassword(token: String, password: String) {
        viewModelScope.launch {
            try {
                repo.setNewPassword(token, password)
                _state.value = AuthState.Success("Пароль обновлён")
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.localizedMessage ?: "Ошибка обновления")
            }
        }
    }
}

class AuthViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(repo) as T
    }
}

sealed class AuthState {
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
