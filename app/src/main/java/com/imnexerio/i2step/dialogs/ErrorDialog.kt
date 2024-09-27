package com.imnexerio.i2step.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieAnimationView
import com.imnexerio.i2step.R

class ErrorDialog(private val message: String) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(
            requireActivity()
        )
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_error, null)

        val animationView = view.findViewById<LottieAnimationView>(R.id.error_animation)
        val messageView = view.findViewById<TextView>(R.id.error_message)

        messageView.text = message
        animationView.playAnimation()

        builder.setView(view)
            .setPositiveButton("Retry") { dialog, id ->
                // Retry button clicked, you can add your retry logic here
            }

        return builder.create()
    }
}