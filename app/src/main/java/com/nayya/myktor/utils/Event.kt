package com.nayya.myktor.utils

class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) null else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content // ← если когда-нибудь нужно просто получить значение без "разового вызова"
}