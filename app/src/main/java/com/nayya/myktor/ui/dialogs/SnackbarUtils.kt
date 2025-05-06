package com.nayya.myktor.ui.dialogs

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import android.view.View
import androidx.appcompat.app.AppCompatActivity

fun Fragment.showSnackbar(message: String) {
    val rootView = requireActivity().findViewById<View>(android.R.id.content)
    Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
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