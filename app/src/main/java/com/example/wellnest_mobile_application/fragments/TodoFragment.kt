package com.example.wellnest_mobile_application.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.example.wellnest_mobile_application.R
import com.example.wellnest_mobile_application.SnapActivity
import com.example.wellnest_mobile_application.data.SharedPrefManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TodoFragment : Fragment() {

    private lateinit var pref: SharedPrefManager

    private lateinit var tvGreeting: TextView
    private lateinit var tvTimeGreeting: TextView

    private lateinit var tvHabitsProgress: TextView
    private lateinit var progressHabits: LinearProgressIndicator

    private lateinit var tvHydrationSummary: TextView
    private lateinit var progressHydrationToday: LinearProgressIndicator

    private lateinit var tvMoodSummary: TextView

    private lateinit var weeklyBars: Array<ProgressBar>
    private lateinit var weeklyLabels: Array<TextView>
    private lateinit var cardSmallSnap: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_todo, container, false)

        pref = SharedPrefManager(requireContext())

        // Initialize views
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvTimeGreeting = view.findViewById(R.id.tvTimeGreeting)

        tvHabitsProgress = view.findViewById(R.id.tvHabitsProgress)
        progressHabits = view.findViewById(R.id.progressHabits)

        tvHydrationSummary = view.findViewById(R.id.tvHydrationSummary)
        progressHydrationToday = view.findViewById(R.id.progressHydrationToday)

        tvMoodSummary = view.findViewById(R.id.tvMoodSummary)

        // Initialize weekly bars and labels
        weeklyBars = arrayOf(
            view.findViewById(R.id.pbW0),
            view.findViewById(R.id.pbW1),
            view.findViewById(R.id.pbW2),
            view.findViewById(R.id.pbW3),
            view.findViewById(R.id.pbW4),
            view.findViewById(R.id.pbW5),
            view.findViewById(R.id.pbW6)
        )

        weeklyLabels = arrayOf(
            view.findViewById(R.id.tvW0),
            view.findViewById(R.id.tvW1),
            view.findViewById(R.id.tvW2),
            view.findViewById(R.id.tvW3),
            view.findViewById(R.id.tvW4),
            view.findViewById(R.id.tvW5),
            view.findViewById(R.id.tvW6)
        )

        cardSmallSnap = view.findViewById(R.id.cardSmallSnap)

        // Set up click listener for Small Snap card
        cardSmallSnap.setOnClickListener {
            val intent = Intent(requireContext(), SnapActivity::class.java)
            startActivity(intent)
        }

        // Update all data
        updateGreeting()
        updateHabits()
        updateHydration()
        updateMood()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when fragment becomes visible
        updateHabits()
        updateHydration()
        updateMood()
    }

    private fun updateGreeting() {
        val user = pref.getUser()
        val userName = if (user != null) {
            user.fullName.split(" ")[0] // Get first name
        } else {
            "User"
        }

        tvGreeting.text = "Hello $userName!"

        // Get current time and determine appropriate greeting
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val timeGreeting = when (hour) {
            in 5..11 -> "🌄" +
                    "Good morning! "
            in 12..17 -> "🌞" +
                    "Good afternoon!"
            in 18..21 -> "🌅" +
                    "Good evening!"
            else -> "🌃" +
                    "Good night!"
        }

        tvTimeGreeting.text = timeGreeting
    }

    private fun updateHabits() {
        val percent = pref.getHabitCompletionRate().toInt().coerceIn(0, 100)
        tvHabitsProgress.text = "$percent% completed"
        progressHabits.setProgressCompat(percent, true)
    }

    private fun updateHydration() {
        val goal = pref.getDailyWaterGoal()
        val today = pref.getTodayWaterIntake()

        // Update today's progress
        tvHydrationSummary.text = "$today / $goal ml"
        val todayPercent = if (goal > 0) {
            ((today.toFloat() / goal) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
        progressHydrationToday.setProgressCompat(todayPercent, true)

        // Update weekly chart with real data
        updateWeeklyHydrationChart(goal)
    }

    private fun updateWeeklyHydrationChart(goal: Int) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Get current day of week (0 = Monday, 6 = Sunday)
        calendar.firstDayOfWeek = Calendar.MONDAY
        val currentDayOfWeek = if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            6 // Sunday is last day
        } else {
            calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY
        }

        // Set to Monday of current week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        // Get all hydration records
        val allRecords = pref.getHydrationRecords()

        // Process each day of the week
        for (i in 0..6) {
            val dateStr = dateFormat.format(calendar.time)

            // Calculate total intake for this day
            val totalForDay = allRecords
                .filter { it.date == dateStr }
                .sumOf { it.amount }

            // Calculate percentage
            val dayPercent = if (goal > 0) {
                ((totalForDay.toFloat() / goal) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }

            // Update progress bar
            weeklyBars[i].progress = dayPercent

            // Highlight current day
            if (i == currentDayOfWeek) {
                weeklyLabels[i].setTextColor(resources.getColor(android.R.color.black, null))
                weeklyLabels[i].textSize = 13f
                weeklyLabels[i].typeface = android.graphics.Typeface.DEFAULT_BOLD
            } else {
                weeklyLabels[i].setTextColor(resources.getColor(android.R.color.darker_gray, null))
                weeklyLabels[i].textSize = 12f
                weeklyLabels[i].typeface = android.graphics.Typeface.DEFAULT
            }

            // Move to next day
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun updateMood() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Calendar.getInstance().time)
        val todayEntries = pref.getMoodEntriesByDate(today)

        if (todayEntries.isEmpty()) {
            tvMoodSummary.text = "No entries yet"
        } else {
            val latest = todayEntries.maxByOrNull { it.time }!!
            val count = todayEntries.size
            tvMoodSummary.text = "${latest.emoji} ${latest.mood} • $count entry(ies)"
        }
    }
}