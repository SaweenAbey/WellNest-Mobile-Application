package com.example.wellnest_mobile_application

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wellnest_mobile_application.activities.HomeActivity
import com.example.wellnest_mobile_application.data.SharedPrefManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText


class HabitTrackerActivity : AppCompatActivity() {

    private lateinit var prefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_habit_tracker)

        prefManager = SharedPrefManager(this)

        val emailInput = findViewById<TextInputEditText>(R.id.email_input)
        val passwordInput = findViewById<TextInputEditText>(R.id.password_input)
        val btnLogin = findViewById<MaterialButton>(R.id.login_button)
        val sigBtn = findViewById<TextView>(R.id.signup_text)

        btnLogin.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()


            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savedUser = prefManager.getUser()
            if (savedUser != null &&
                email == savedUser.email &&
                password == savedUser.password
            ) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()


                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials!", Toast.LENGTH_SHORT).show()
            }
        }


        sigBtn.setOnClickListener {
            val intent = Intent(this, Sign_Up::class.java)
            startActivity(intent)
        }
    }
}
