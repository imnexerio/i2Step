package com.imnexerio.i2step

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.imnexerio.i2step.fragments.loginFragment


class buildingSplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.fragment_splash)

        val animationView = findViewById<LottieAnimationView>(R.id.lottieAnimationView)

        val iHome = Intent(this@buildingSplashScreen, loginFragment::class.java)

        Handler().postDelayed({
            startActivity(iHome)
            finish()
        }, 2500)
    }
}