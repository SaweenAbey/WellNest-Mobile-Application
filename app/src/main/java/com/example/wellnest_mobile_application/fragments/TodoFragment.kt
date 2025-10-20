package com.example.wellnest_mobile_application.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.wellnest_mobile_application.R
import com.example.wellnest_mobile_application.SnapActivity
import com.example.wellnest_mobile_application.database.DatabaseManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TodoFragment : Fragment() {

    private lateinit var databaseManager: DatabaseManager

    private lateinit var tvGreeting: TextView
    private lateinit var tvTimeGreeting: TextView

    private lateinit var tvHabitsProgress: TextView
    private lateinit var progressHabits: LinearProgressIndicator

    private lateinit var tvHydrationSummary: TextView
    private lateinit var progressHydrationToday: LinearProgressIndicator

    private lateinit var tvMoodSummary: TextView

    private lateinit var weeklyBars: Array<ProgressBar>
    private lateinit var weeklyLabels: Array<TextView>

    private lateinit var moodChart: LineChart
    private lateinit var tvMoodWeeklyAverage: TextView
    private lateinit var tvMoodEntriesCount: TextView

    private lateinit var cardHabits: MaterialCardView
    private lateinit var cardHydration: MaterialCardView
    private lateinit var cardMood: MaterialCardView
    private lateinit var cardSmallSnap: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_todo, container, false)

        databaseManager = DatabaseManager(requireContext())

        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvTimeGreeting = view.findViewById(R.id.tvTimeGreeting)
        tvHabitsProgress = view.findViewById(R.id.tvHabitsProgress)
        progressHabits = view.findViewById(R.id.progressHabits)
        tvHydrationSummary = view.findViewById(R.id.tvHydrationSummary)
        progressHydrationToday = view.findViewById(R.id.progressHydrationToday)
        tvMoodSummary = view.findViewById(R.id.tvMoodSummary)

        moodChart = view.findViewById(R.id.moodChart)
        tvMoodWeeklyAverage = view.findViewById(R.id.tvMoodWeeklyAverage)
        tvMoodEntriesCount = view.findViewById(R.id.tvMoodEntriesCount)

        cardHabits = view.findViewById(R.id.cardHabits)
        cardHydration = view.findViewById(R.id.cardHydration)
        cardMood = view.findViewById(R.id.cardMood)
        cardSmallSnap = view.findViewById(R.id.cardSmallSnap)

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

        setupCardClickListeners()
        setupMoodChart()

        updateGreeting()
        updateHabits()
        updateHydration()
        updateMood()
        updateMoodChart()

        return view
    }

    private fun setupCardClickListeners() {
        cardHabits.setOnClickListener {
            (activity as? com.example.wellnest_mobile_application.activities.HomeActivity)?.let { homeActivity ->
                homeActivity.loadFragment(
                    HabitsFragment(),
                    "Habits Tracker"
                )
                homeActivity.binding.bottomNavigation.selectedItemId = R.id.nav_habits
            }
        }

        cardHydration.setOnClickListener {
            (activity as? com.example.wellnest_mobile_application.activities.HomeActivity)?.let { homeActivity ->
                homeActivity.loadFragment(
                    HydrationFragment(),
                    "Hydration Tracker"
                )
                homeActivity.binding.bottomNavigation.selectedItemId = R.id.nav_hydration
            }
        }

        cardMood.setOnClickListener {
            (activity as? com.example.wellnest_mobile_application.activities.HomeActivity)?.let { homeActivity ->
                homeActivity.loadFragment(
                    MoodFragment(),
                    "Mood Journal"
                )
                homeActivity.binding.bottomNavigation.selectedItemId = R.id.nav_mood
            }
        }

        cardSmallSnap.setOnClickListener {
            val intent = Intent(requireContext(), SnapActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateHabits()
        updateHydration()
        updateMood()
        updateMoodChart()
    }

    private fun updateGreeting() {
        lifecycleScope.launch {
            val user = databaseManager.userRepository.getUser()
            val userName = if (user != null) {
                user.fullName.split(" ")[0]
            } else {
                "User"
            }

            // Enhanced greeting with user details
            tvGreeting.text = "Hello $userName!"

            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)

            val timeGreeting = when (hour) {
                in 5..11 -> "🌄 Good morning!"
                in 12..17 -> "🌞 Good afternoon!"
                in 18..21 -> "🌅 Good evening!"
                else -> "🌃 Good night!"
            }

            // Enhanced time greeting with user details
            tvTimeGreeting.text = "$timeGreeting\nWelcome to your wellness dashboard!"
        }
    }

    private fun updateHabits() {
        lifecycleScope.launch {
            val percent = databaseManager.habitRepository.getHabitCompletionPercentage()
                .toInt().coerceIn(0, 100)
            tvHabitsProgress.text = "$percent% completed"
            progressHabits.setProgressCompat(percent, true)
        }
    }

    private fun updateHydration() {
        lifecycleScope.launch {
            val goal = databaseManager.appSettingsRepository.getDailyWaterGoal()
            val today = databaseManager.hydrationRecordRepository.getTodayWaterIntake()

            tvHydrationSummary.text = "$today / $goal ml"
            val todayPercent = if (goal > 0) {
                ((today.toFloat() / goal) * 100).toInt().coerceIn(0, 100)
            } else 0

            progressHydrationToday.setProgressCompat(todayPercent, true)
            updateWeeklyHydrationChart(goal)
        }
    }

    private fun updateWeeklyHydrationChart(goal: Int) {
        lifecycleScope.launch {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()

                calendar.firstDayOfWeek = Calendar.MONDAY
                val currentDayOfWeek = if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) 6
                else calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY

                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

                val allRecords = databaseManager.hydrationRecordRepository.getHydrationRecords().first()

                for (i in 0..6) {
                    val dateStr = dateFormat.format(calendar.time)

                    val totalForDay = allRecords
                        .filter { it.date == dateStr }
                        .sumOf { it.amount }

                    val dayPercent = if (goal > 0) {
                        ((totalForDay.toFloat() / goal) * 100).toInt().coerceIn(0, 100)
                    } else 0

                    weeklyBars[i].progress = dayPercent

                    if (i == currentDayOfWeek) {
                        weeklyLabels[i].setTextColor(resources.getColor(android.R.color.black, null))
                        weeklyLabels[i].textSize = 13f
                        weeklyLabels[i].typeface = android.graphics.Typeface.DEFAULT_BOLD
                    } else {
                        weeklyLabels[i].setTextColor(resources.getColor(android.R.color.darker_gray, null))
                        weeklyLabels[i].textSize = 12f
                        weeklyLabels[i].typeface = android.graphics.Typeface.DEFAULT
                    }

                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateMood() {
        lifecycleScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Calendar.getInstance().time)
            val todayEntries = databaseManager.moodEntryRepository.getMoodEntriesByDate(today)

            if (todayEntries.isEmpty()) {
                tvMoodSummary.text = "No entries yet"
            } else {
                val latest = todayEntries.maxByOrNull { it.time }!!
                val count = todayEntries.size
                tvMoodSummary.text = "${latest.emoji} ${latest.mood} • $count entry(ies)"
            }
        }
    }

    private fun setupMoodChart() {
        moodChart.description.isEnabled = false
        moodChart.setTouchEnabled(true)
        moodChart.setDragEnabled(true)
        moodChart.setScaleEnabled(false)
        moodChart.setPinchZoom(false)
        moodChart.setDrawGridBackground(false)
        moodChart.setBackgroundColor(Color.TRANSPARENT)
        moodChart.setExtraOffsets(10f, 20f, 10f, 10f)

        val xAxis = moodChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.granularity = 1f
        xAxis.labelCount = 7
        xAxis.textColor = Color.parseColor("#8E8E93")
        xAxis.textSize = 11f
        xAxis.yOffset = 10f

        val leftAxis = moodChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#F2F2F7")
        leftAxis.gridLineWidth = 1f
        leftAxis.setDrawAxisLine(false)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 6f
        leftAxis.labelCount = 6
        leftAxis.textColor = Color.parseColor("#8E8E93")
        leftAxis.textSize = 10f
        leftAxis.xOffset = 10f

        moodChart.axisRight.isEnabled = false
        moodChart.legend.isEnabled = false
    }

    private fun updateMoodChart() {
        lifecycleScope.launch {
            try {
                val trendData = databaseManager.moodEntryRepository.getMoodTrendData(7)
                val entries = mutableListOf<Entry>()
                val dayLabels = mutableListOf<String>()

                trendData.forEachIndexed { index, (date, score) ->
                    entries.add(Entry(index.toFloat(), score))
                    val calendar = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    calendar.time = dateFormat.parse(date) ?: Calendar.getInstance().time
                    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                    dayLabels.add(dayFormat.format(calendar.time))
                }

                if (entries.isNotEmpty()) {
                    val dataSet = LineDataSet(entries, "Mood Trend")
                    val primaryColor = Color.parseColor("#10B981")

                    dataSet.color = primaryColor
                    dataSet.lineWidth = 4f
                    dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                    dataSet.cubicIntensity = 0.15f
                    dataSet.setCircleColor(primaryColor)
                    dataSet.circleHoleColor = Color.WHITE
                    dataSet.circleRadius = 8f
                    dataSet.circleHoleRadius = 5f
                    dataSet.setDrawValues(true)
                    dataSet.valueTextSize = 11f
                    dataSet.valueTextColor = Color.parseColor("#1D1D1F")
                    dataSet.setDrawFilled(true)
                    dataSet.fillAlpha = 50
                    dataSet.fillColor = primaryColor
                    dataSet.isHighlightEnabled = false

                    val lineData = LineData(dataSet)
                    moodChart.data = lineData
                    moodChart.xAxis.valueFormatter = IndexAxisValueFormatter(dayLabels.toTypedArray())
                    moodChart.animateXY(800, 800)
                    moodChart.invalidate()
                }

                val weeklyAvg = databaseManager.moodEntryRepository.getWeeklyMoodAverage()
                val weeklyEntries =
                    databaseManager.moodEntryRepository.getMoodEntries().first().count {
                        val entryDate =
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, -7)
                        entryDate?.after(cal.time) == true
                    }

                tvMoodWeeklyAverage.text = "Weekly Average: ${String.format("%.1f", weeklyAvg)}"
                tvMoodEntriesCount.text = "$weeklyEntries entries this week"

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
