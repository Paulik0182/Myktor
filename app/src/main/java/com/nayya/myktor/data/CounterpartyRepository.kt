package com.nayya.myktor.data

import com.nayya.myktor.data.network.CounterpartyContactRequest
import com.nayya.myktor.data.network.CounterpartyRequest
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
import com.nayya.myktor.domain.counterpartyentity.Country
import retrofit2.Response

class CounterpartyRepository {

    suspend fun updateCounterparty(id: Long, request: CounterpartyRequest): Response<Unit> {
        return RetrofitInstance.api.updateCounterparty(id, request)
    }

    suspend fun getCounterpartyById(id: Long): CounterpartyEntity {
        return RetrofitInstance.api.getCounterpartyById(id)
    }

    suspend fun getCountries(): List<Country> {
        return RetrofitInstance.api.getCountries()
    }

    suspend fun updateContacts(id: Long, contacts: List<CounterpartyContactRequest>): Response<Unit> {
        return RetrofitInstance.api.patchContacts(id, contacts)
    }

}
