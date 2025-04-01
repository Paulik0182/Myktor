package com.nayya.myktor.utils

import java.util.Locale

object LocaleUtils {
    fun getCurrentLanguageCode(): String =
        Locale.getDefault().language.lowercase(Locale.ROOT)
}
