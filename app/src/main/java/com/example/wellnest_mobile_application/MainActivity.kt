package com.example.wellnest_mobile_application

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.example.wellnest_mobile_application.activities.HomeActivity
import com.example.wellnest_mobile_application.database.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val SPLASH_DELAY: Long = 2000
    private lateinit var databaseManager: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseManager = DatabaseManager(this)
        
        // Initialize database
        CoroutineScope(Dispatchers.IO).launch {
            databaseManager.initializeDatabase()
        }

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
            CoroutineScope(Dispatchers.IO).launch {
                val isLoggedIn = databaseManager.userRepository.isLoggedIn()
                
                runOnUiThread {
                    if (isLoggedIn) {
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    } else {
                        val intent = Intent(this@MainActivity, OnboardScreen1::class.java)
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                }
            }
        }, SPLASH_DELAY)
    }
}