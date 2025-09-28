package com.example.wellnest_mobile_application

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class OnboardScreen3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard_screen3)


        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)



        btnGetStarted.setOnClickListener {
            navigateToMainApp()
        }
    }

    private fun navigateToMainApp() {
        val intent = Intent(this, HabitTrackerActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finishAffinity()
    }
}