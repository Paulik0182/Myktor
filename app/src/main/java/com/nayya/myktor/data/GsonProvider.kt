package com.nayya.myktor.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonProvider {
    val instance: Gson = GsonBuilder().create()
}
