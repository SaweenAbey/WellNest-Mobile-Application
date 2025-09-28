package com.example.wellnest_mobile_application.activities
import com.example.wellnest_mobile_application.R
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.wellnest_mobile_application.HabitTrackerActivity
import com.example.wellnest_mobile_application.data.SharedPrefManager
import com.example.wellnest_mobile_application.databinding.ActivityHomeBinding
import com.example.wellnest_mobile_application.fragments.HabitsFragment
import com.example.wellnest_mobile_application.fragments.MoodFragment
import com.example.wellnest_mobile_application.fragments.HydrationFragment
import com.example.wellnest_mobile_application.fragments.ProfileFragment
import com.example.wellnest_mobile_application.fragments.TodoFragment
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var prefManager: SharedPrefManager

    // Track current fragment
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = SharedPrefManager(this)

        // Check if user is logged in
        if (!prefManager.isLoggedIn()) {
            redirectToLogin()
            return
        }

        initializeUI()
        setupNavigation()
        loadDefaultFragment()
    }

    private fun initializeUI() {
        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // Set user name in toolbar if available
        val user = prefManager.getUser()
        user?.let {
            binding.toolbar.title = "Welcome, ${it.fullName.split(" ")[0]}!"
        } ?: run {
            binding.toolbar.title = "Wellnest Wellness"
        }
    }

    private fun setupNavigation() {
        // Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_todo -> {
                    loadFragment(TodoFragment(), "Daily Todo")
                    true
                }
                R.id.nav_habits -> {
                    loadFragment(HabitsFragment(), "Habits Tracker")
                    true
                }
                R.id.nav_mood -> {
                    loadFragment(MoodFragment(), "Mood Journal")
                    true
                }
                R.id.nav_hydration -> {
                    loadFragment(HydrationFragment(), "Hydration Tracker")
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment(), "My Profile")
                    true
                }
                else -> false
            }
        }

        // Set navigation icon if needed (for future drawer implementation)
        binding.toolbar.setNavigationOnClickListener {
            // Optional: Add drawer functionality here
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDefaultFragment() {
        loadFragment(TodoFragment(), "Daily Todo")
        binding.bottomNavigation.selectedItemId = R.id.nav_todo
    }

    private fun loadFragment(fragment: Fragment, title: String) {
        // Update toolbar title
        binding.toolbar.title = title

        // Only replace fragment if it's different from current
        if (currentFragment?.javaClass != fragment.javaClass) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(R.id.fragmentContainer, fragment)
                .commit()

            currentFragment = fragment
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, HabitTrackerActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    // Handle back button press
    override fun onBackPressed() {
        when (currentFragment) {
            is HabitsFragment -> {
                // If already on habits fragment, confirm exit
                if (binding.bottomNavigation.selectedItemId == R.id.nav_habits) {
                    showExitConfirmation()
                } else {
                    // Go back to habits fragment
                    loadFragment(HabitsFragment(), "Habits Tracker")
                    binding.bottomNavigation.selectedItemId = R.id.nav_habits
                }
            }
            else -> {
                // For other fragments, go back to habits
                loadFragment(HabitsFragment(), "Habits Tracker")
                binding.bottomNavigation.selectedItemId = R.id.nav_habits
            }
        }
    }

    private fun showExitConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit Wellnest?")
            .setPositiveButton("Exit") { _, _ ->
                finishAffinity() // Close the app
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Method to update toolbar title from fragments
    fun updateToolbarTitle(title: String) {
        binding.toolbar.title = title
    }

    // Method to show/hide bottom navigation
    fun setBottomNavigationVisibility(visible: Boolean) {
        binding.bottomNavigation.visibility = if (visible) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
    }

    // Refresh current fragment (useful after data changes)
    fun refreshCurrentFragment() {
        currentFragment?.let { fragment ->
            when (fragment) {
                is HabitsFragment -> {
                    loadFragment(HabitsFragment(), "Habits Tracker")
                }
                is MoodFragment -> {
                    loadFragment(MoodFragment(), "Mood Journal")
                }
                is HydrationFragment -> {
                    loadFragment(HydrationFragment(), "Hydration Tracker")
                }
                is ProfileFragment -> {
                    loadFragment(ProfileFragment(), "My Profile")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to app
        val user = prefManager.getUser()
        user?.let {
            binding.toolbar.title = "Welcome, ${it.fullName.split(" ")[0]}!"
        }
    }
}