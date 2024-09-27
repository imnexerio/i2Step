package com.imnexerio.i2step.dialogs

import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.imnexerio.i2step.R

object ExitConfirmationDialog {
    @JvmStatic
    fun show(
        context: Context?,
        title: String?,
        message: String?,
        onConfirm: DialogInterface.OnClickListener?
    ) {
        MaterialAlertDialogBuilder(context!!)
            .setTitle(title)
            .setMessage(message)
            .setIcon(R.drawable.baseline_exit_to_app_24)
            .setPositiveButton("No") { dialog, which ->
                Toast.makeText(
                    context,
                    "Positive",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Yes", onConfirm)
            .show()
    }
}