package com.nayya.myktor.ui.dialogs

import android.app.AlertDialog
import android.content.Context

object InfoDialogHelper {
    fun show(context: Context, message: String, onOk: () -> Unit = {}) {
        AlertDialog.Builder(context)
            .setTitle("Информация")
            .setMessage(message)
            .setPositiveButton("ОК") { dialog, _ ->
                dialog.dismiss()
                onOk()
            }
            .show()
    }
}
