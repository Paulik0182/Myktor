package com.nayya.myktor.ui.login

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.GsonProvider
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.domain.loginentity.LoginErrorResponse
import com.nayya.myktor.domain.loginentity.LoginRequest
import com.nayya.myktor.domain.loginentity.RegisterRequest
import com.nayya.myktor.domain.loginentity.ResetPasswordRequest
import com.nayya.myktor.domain.loginentity.ResetRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val deviceInfo = Build.MODEL ?: "UnknownDevice"
                val token = repository.login(LoginRequest(email, password, deviceInfo))
                _loginState.value = LoginState.Success(token)
            } catch (e: LoginUiException) {
                _loginState.value = LoginState.Error(code = e.code, message = e.message)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(code = "unknown", message = e.localizedMessage ?: "Ошибка входа")
            }
        }
    }
}

class LoginViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class LoginState {
    data class Success(val token: String) : LoginState()
    data class Error(val code: String, val message: String) : LoginState()
}

interface AuthRepository {
    suspend fun login(request: LoginRequest): String
    suspend fun requestPasswordReset(email: String)
    suspend fun setNewPassword(token: String, password: String)
    suspend fun register(request: RegisterRequest)

}

class DefaultAuthRepository : AuthRepository {
    override suspend fun login(request: LoginRequest): String {
        try {
            val response = RetrofitInstance.api.login(request)
            return response["token"] ?: throw IllegalStateException("Нет токена")
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val error = try {
                GsonProvider.instance.fromJson(errorBody, LoginErrorResponse::class.java)
            } catch (_: Exception) {
                null
            }
            val message = error?.message ?: "Ошибка авторизации"
            val code = error?.code ?: "unknown"
            throw LoginUiException(code, message)
        }
    }

    override suspend fun requestPasswordReset(email: String) {
        RetrofitInstance.api.requestResetPassword(ResetRequest(email))
    }

    override suspend fun setNewPassword(token: String, password: String) {
        RetrofitInstance.api.resetPassword(ResetPasswordRequest(token, password))
    }

    override suspend fun register(request: RegisterRequest) {
        RetrofitInstance.api.register(request)
    }
}
