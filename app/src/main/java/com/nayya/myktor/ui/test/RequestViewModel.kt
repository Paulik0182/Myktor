package com.nayya.myktor.ui.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayya.myktor.data.RequestRepository
import kotlinx.coroutines.launch

class RequestViewModel : ViewModel() {

    private val _result = MutableLiveData<String>()
    val result: LiveData<String> get() = _result

    private val _ip = MutableLiveData<String>()
    val ip: LiveData<String> get() = _ip

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private val repository = RequestRepository()

    /**
     * viewModelScope — это встроенная в ViewModel корутина, которая привязана к ее жизненному циклу.
     * Когда ViewModel уничтожается (например, при закрытии экрана), все запущенные корутины автоматически отменяются, предотвращая утечки памяти.
     */
    fun sendSumRequest(a: Double, b: Double) {
        viewModelScope.launch {
            val result = repository.getSum(a, b)
            _result.value = result
        }
    }

    fun getIP() {
        viewModelScope.launch {
            val ip = repository.getIP()
            _ip.value = ip
        }
    }

    fun getTextMessage() {
        viewModelScope.launch {
            val message = repository.getTextMessage()
            _message.value = message
        }
    }
}
