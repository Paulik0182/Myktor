package com.nayya.myktor.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

fun Fragment.showSnackbar(message: String) {
    val rootView = requireActivity().findViewById<View>(android.R.id.content)
    Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
}

fun BottomSheetDialogFragment.showSnackbarBottomSheetDialog(message: String) {
    view?.let {
        Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
    }
}

fun AppCompatActivity.showMessage(message: String) {
    val rootView = findViewById<View>(android.R.id.content)
    rootView?.showSnackbar(message) ?: run {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

fun View.showSnackbar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
}

fun Fragment.showSnackbarAboveKeyboard(message: String) {
    val rootView = requireActivity().findViewById<View>(android.R.id.content)
    val snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)

    // Принудительно поднимаем Snackbar на высоту клавиатуры (примерно)
    val keyboardHeight = 200.dpToPx(requireContext()) // или 250, на глаз
    snackbar.view.translationY = -keyboardHeight.toFloat()

    snackbar.show()
}

fun Int.dpToPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()
