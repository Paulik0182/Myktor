package com.nayya.myktor.ui.dialogs

import android.app.AlertDialog
import android.content.Context

object UnsavedChangesDialogHelper {
    fun show(
        context: Context,
        onConfirm: () -> Unit,
        onDiscard: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle("Несохранённые изменения")
            .setMessage("Вы хотите сохранить изменения?")
            .setPositiveButton("Сохранить") { _, _ -> onConfirm() }
            .setNegativeButton("Отменить") { _, _ -> onDiscard() }
            .show()
    }
}