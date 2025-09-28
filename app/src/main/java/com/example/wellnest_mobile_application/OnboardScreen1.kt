package com.example.wellnest_mobile_application

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OnboardScreen1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard_screen1)


        val btnNext = findViewById<Button>(R.id.button2)
        val tvSkip = findViewById<TextView>(R.id.tvSkip)


        btnNext.setOnClickListener {
            navigateToOnboardScreen2()
        }

        tvSkip.setOnClickListener {
            navigateToMainApp()
        }
    }

    private fun navigateToOnboardScreen2() {
        val intent = Intent(this, OnboardScreen2::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun navigateToMainApp() {
        // Replace with your main activity (e.g., HomeActivity or HabitTrackerActivity)
        val intent = Intent(this, HabitTrackerActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}