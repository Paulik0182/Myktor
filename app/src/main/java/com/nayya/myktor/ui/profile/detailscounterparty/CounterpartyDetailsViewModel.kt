package com.nayya.myktor.ui.profile.detailscounterparty

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

class CounterpartyDetailsViewModel : ViewModel() {

    private val _counterparty = MutableLiveData<CounterpartyEntity>()
    val counterparty: LiveData<CounterpartyEntity> = _counterparty

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
}

