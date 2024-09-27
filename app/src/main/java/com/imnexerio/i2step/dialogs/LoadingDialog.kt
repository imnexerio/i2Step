package com.imnexerio.i2step.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.airbnb.lottie.LottieAnimationView
import com.imnexerio.i2step.R

class LoadingDialog(context: Context) : Dialog(context) {
    init {
        setContentView(R.layout.dialog_loading)
        val animationView = findViewById<LottieAnimationView>(R.id.loading_animation)
        animationView.playAnimation()
        setCancelable(false) // Prevent the dialog from being dismissed

        // Set the dialog's background color to transparent
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}