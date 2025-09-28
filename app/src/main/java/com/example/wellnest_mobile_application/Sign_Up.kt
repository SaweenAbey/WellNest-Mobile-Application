package com.example.wellnest_mobile_application

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnest_mobile_application.R
import com.example.wellnest_mobile_application.data.SharedPrefManager
import com.example.wellnest_mobile_application.models.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText

class Sign_Up : AppCompatActivity() {

    private lateinit var prefManager: SharedPrefManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        prefManager = SharedPrefManager(this)

        val fullNameInput = findViewById<TextInputEditText>(R.id.fullname_input)
        val emailInput = findViewById<TextInputEditText>(R.id.signup_email_input)
        val phoneInput = findViewById<TextInputEditText>(R.id.phone_input)
        val passwordInput = findViewById<TextInputEditText>(R.id.signup_password_input)
        val confirmPasswordInput = findViewById<TextInputEditText>(R.id.confirm_password_input)
        val termsCheckbox = findViewById<MaterialCheckBox>(R.id.terms_checkbox)
        val btnSignUp = findViewById<MaterialButton>(R.id.signup_button)
        val backButton = findViewById<ImageButton>(R.id.back_button)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        btnSignUp.setOnClickListener {
            val name = fullNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // Validation
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (phone.length < 10) {
                Toast.makeText(this, "Phone must be at least 10 digits!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!termsCheckbox.isChecked) {
                Toast.makeText(this, "You must agree to Terms & Privacy Policy!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save user in SharedPreferences
            val user = User(name,email, password)
            prefManager.saveUser(user)

            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

            // Navigate to HabitTrackerActivity
            val intent = Intent(this, HabitTrackerActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}