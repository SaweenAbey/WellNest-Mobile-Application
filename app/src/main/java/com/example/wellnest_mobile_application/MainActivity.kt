package com.example.wellnest_mobile_application

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private val SPLASH_DELAY: Long = 2000 // 2 seconds
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        val nameImageView = findViewById<ImageView>(R.id.nameImageView)
        val taglineText = findViewById<TextView>(R.id.taglineText)


        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val scaleInAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_in)


        logoImageView.startAnimation(scaleInAnimation)

        Handler(Looper.getMainLooper()).postDelayed({
            nameImageView.startAnimation(fadeInAnimation)
            taglineText.startAnimation(fadeInAnimation)
        }, 500)


        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, OnboardScreen1::class.java)
            startActivity(intent)

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, SPLASH_DELAY)
    }
}