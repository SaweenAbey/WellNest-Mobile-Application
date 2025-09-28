package com.example.wellnest_mobile_application

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class OnboardScreen2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard_screen2)


        val btnNext = findViewById<Button>(R.id.button2)
        val tvSkip = findViewById<TextView>(R.id.tvSkip)


        btnNext.setOnClickListener {
            navigateToOnboardScreen3()
        }


        tvSkip.setOnClickListener {
            navigateToMainApp()
        }
    }

    private fun navigateToOnboardScreen3() {
        val intent = Intent(this, OnboardScreen3::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun navigateToMainApp() {
        val intent = Intent(this, HabitTrackerActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finishAffinity() // Close all onboarding screens
    }
}