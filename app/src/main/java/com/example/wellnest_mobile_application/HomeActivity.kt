package com.example.wellnest_mobile_application.activities
import com.example.wellnest_mobile_application.R
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.wellnest_mobile_application.HabitTrackerActivity
import com.example.wellnest_mobile_application.database.DatabaseManager
import com.example.wellnest_mobile_application.databinding.ActivityHomeBinding
import com.example.wellnest_mobile_application.fragments.MoodFragment
import com.example.wellnest_mobile_application.fragments.HydrationFragment
import com.example.wellnest_mobile_application.fragments.HabitsFragment
import com.example.wellnest_mobile_application.fragments.ProfileFragment
import com.example.wellnest_mobile_application.fragments.TodoFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class HomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding
    private lateinit var databaseManager: DatabaseManager

    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseManager = DatabaseManager(this)

        // Check if user is logged in using database
        CoroutineScope(Dispatchers.IO).launch {
            val isLoggedIn = databaseManager.userRepository.isLoggedIn()
            
            runOnUiThread {
                if (!isLoggedIn) {
                    redirectToLogin()
                    return@runOnUiThread
                }
                
                configureStatusBar()
                initializeUI()
                setupNavigation()
                loadDefaultFragment()
            }
        }
    }

    private fun configureStatusBar() {
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.decorView.systemUiVisibility = 
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun initializeUI() {

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }



    private fun setupNavigation() {

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

    }

    private fun loadDefaultFragment() {
        loadFragment(TodoFragment(), "Daily Todo")
        binding.bottomNavigation.selectedItemId = R.id.nav_todo
    }

    fun loadFragment(fragment: Fragment, title: String) {

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

    override fun onBackPressed() {
        when (currentFragment) {
            is HabitsFragment -> {
                if (binding.bottomNavigation.selectedItemId == R.id.nav_habits) {
                    showExitConfirmation()
                } else {
                    loadFragment(HabitsFragment(), "Habits Tracker")
                    binding.bottomNavigation.selectedItemId = R.id.nav_habits
                }
            }
            else -> {
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




    fun setBottomNavigationVisibility(visible: Boolean) {
        binding.bottomNavigation.visibility = if (visible) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
    }


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
    }
}