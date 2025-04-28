package com.nayya.myktor.utils

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.util.Locale

object LocaleUtils {
    fun getCurrentLanguageCode(): String =
        Locale.getDefault().language.lowercase(Locale.ROOT)

    fun ViewGroup.getChildAtOrNull(index: Int): View? {
        return if (index in 0 until childCount) getChildAt(index) else null
    }

    fun Fragment.goBack() {
        parentFragmentManager.popBackStack()
    }
}
