package com.nayya.myktor.ui.login

class LoginUiException(
    val code: String,
    override val message: String
) : Exception()
