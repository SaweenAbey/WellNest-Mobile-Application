package com.example.wellnest_mobile_application

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.wellnest_mobile_application.HabitTrackerActivity
import com.example.wellnest_mobile_application.R
import com.example.wellnest_mobile_application.database.DatabaseManager
import com.example.wellnest_mobile_application.models.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Sign_Up : AppCompatActivity() {

    private lateinit var databaseManager: DatabaseManager
    
    // TextInputLayout references for error handling
    private lateinit var fullNameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var phoneLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        databaseManager = DatabaseManager(this)

        // Initialize TextInputLayouts
        fullNameLayout = findViewById(R.id.fullname_input_layout)
        emailLayout = findViewById(R.id.signup_email_input_layout)
        phoneLayout = findViewById(R.id.phone_input_layout)
        passwordLayout = findViewById(R.id.signup_password_input_layout)
        confirmPasswordLayout = findViewById(R.id.confirm_password_input_layout)

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

        // Add text change listeners to clear errors when user starts typing
        fullNameInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (fullNameLayout.error != null) {
                    clearError(fullNameLayout)
                }
            }
        })

        emailInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (emailLayout.error != null) {
                    clearError(emailLayout)
                }
            }
        })

        phoneInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (phoneLayout.error != null) {
                    clearError(phoneLayout)
                }
            }
        })

        passwordInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (passwordLayout.error != null) {
                    clearError(passwordLayout)
                }
            }
        })

        confirmPasswordInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (confirmPasswordLayout.error != null) {
                    clearError(confirmPasswordLayout)
                }
            }
        })
        btnSignUp.setOnClickListener {
            val name = fullNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // Clear previous errors
            clearAllErrors()

            // Validate each field individually
            var isValid = true

            // Full Name validation
            if (name.isEmpty()) {
                showError(fullNameLayout, "Full name is required")
                isValid = false
            } else if (name.length < 2) {
                showError(fullNameLayout, "Full name must be at least 2 characters")
                isValid = false
            }

            // Email validation
            if (email.isEmpty()) {
                showError(emailLayout, "Email is required")
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError(emailLayout, "Please enter a valid email address")
                isValid = false
            }

            // Phone validation
            if (phone.isEmpty()) {
                showError(phoneLayout, "Phone number is required")
                isValid = false
            } else if (phone.length < 10) {
                showError(phoneLayout, "Phone number must be at least 10 digits")
                isValid = false
            } else if (!phone.matches(Regex("^[0-9+\\-\\s()]+$"))) {
                showError(phoneLayout, "Please enter a valid phone number")
                isValid = false
            }

            // Password validation
            if (password.isEmpty()) {
                showError(passwordLayout, "Password is required")
                isValid = false
            } else if (password.length < 6) {
                showError(passwordLayout, "Password must be at least 6 characters")
                isValid = false
            } else if (!password.matches(Regex(".*[A-Za-z].*"))) {
                showError(passwordLayout, "Password must contain at least one letter")
                isValid = false
            } else if (!password.matches(Regex(".*[0-9].*"))) {
                showError(passwordLayout, "Password must contain at least one number")
                isValid = false
            }

            // Confirm Password validation
            if (confirmPassword.isEmpty()) {
                showError(confirmPasswordLayout, "Please confirm your password")
                isValid = false
            } else if (password != confirmPassword) {
                showError(confirmPasswordLayout, "Passwords do not match")
                isValid = false
            }

            // Terms checkbox validation
            if (!termsCheckbox.isChecked) {
                Toast.makeText(this, "You must agree to Terms & Privacy Policy!", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            // If all validations pass, proceed with sign up
            if (isValid) {
                val user = User(name, email, password)
                
                CoroutineScope(Dispatchers.IO).launch {
                    val success = databaseManager.userRepository.saveUser(user)
                    
                    runOnUiThread {
                        if (success) {
                            Toast.makeText(this@Sign_Up, "Account created successfully!", Toast.LENGTH_SHORT).show()
                            
                            val intent = Intent(this@Sign_Up, HabitTrackerActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@Sign_Up, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Shows error message and changes input field outline to red
     */
    private fun showError(layout: TextInputLayout, message: String) {
        layout.error = message
        layout.boxStrokeColor = ContextCompat.getColor(this, R.color.error_red)
    }

    /**
     * Clears error from a specific input field and restores normal styling
     */
    private fun clearError(layout: TextInputLayout) {
        layout.error = null
        layout.boxStrokeColor = ContextCompat.getColor(this, R.color.primary_green)
    }

    /**
     * Clears all errors from all input fields
     */
    private fun clearAllErrors() {
        clearError(fullNameLayout)
        clearError(emailLayout)
        clearError(phoneLayout)
        clearError(passwordLayout)
        clearError(confirmPasswordLayout)
    }
}