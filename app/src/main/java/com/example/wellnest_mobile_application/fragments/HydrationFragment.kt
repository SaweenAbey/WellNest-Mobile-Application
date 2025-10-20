package com.example.wellnest_mobile_application.fragments

import android.animation.ValueAnimator
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.wellnest_mobile_application.R
import com.example.wellnest_mobile_application.database.DatabaseManager
import com.example.wellnest_mobile_application.models.HydrationRecord
import com.example.wellnest_mobile_application.notifications.HydrationReminderReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

class HydrationFragment : Fragment() {

    private lateinit var databaseManager: DatabaseManager
    private lateinit var tvDailyIntake: TextView
    private lateinit var progressHydration: ProgressBar
    private lateinit var waterFill: View
    private lateinit var btnAdd150: Button
    private lateinit var btnAdd200: Button
    private lateinit var btnAddCustom: Button
    private lateinit var switchHydrationReminder: Switch
    private lateinit var tvReminderInterval: TextView
    private lateinit var seekBarReminderInterval: SeekBar
    
    // Table components
    private lateinit var hydrationTableContainer: ViewGroup
    private lateinit var tvWeeklyTotalIntake: TextView
    private lateinit var tvWeeklyAvgPercentage: TextView
    private lateinit var tvWeeklyGoalsAchieved: TextView

    private val dailyGoalMl = 4000
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hydration, container, false)

        databaseManager = DatabaseManager(requireContext())
        tvDailyIntake = view.findViewById(R.id.tvDailyIntake)
        progressHydration = view.findViewById(R.id.progressHydration)
        waterFill = view.findViewById(R.id.waterFill)
        btnAdd150 = view.findViewById(R.id.btnAdd150)
        btnAdd200 = view.findViewById(R.id.btnAdd200)
        btnAddCustom = view.findViewById(R.id.btnAddCustom)
        switchHydrationReminder = view.findViewById(R.id.switchHydrationReminder)
        tvReminderInterval = view.findViewById(R.id.tvReminderInterval)
        seekBarReminderInterval = view.findViewById(R.id.seekBarReminderInterval)
        
        // Table components
        hydrationTableContainer = view.findViewById(R.id.hydrationTableContainer)
        tvWeeklyTotalIntake = view.findViewById(R.id.tvWeeklyTotalIntake)
        tvWeeklyAvgPercentage = view.findViewById(R.id.tvWeeklyAvgPercentage)
        tvWeeklyGoalsAchieved = view.findViewById(R.id.tvWeeklyGoalsAchieved)

        btnAdd150.setOnClickListener { addWater(150) }
        btnAdd200.setOnClickListener { addWater(200) }
        btnAddCustom.setOnClickListener { showCustomDialog() }

        setupReminderSettings()

        updateUiForToday()
        updateWeeklyBars(view)

        return view
    }

    private fun showCustomDialog() {
        val dlg = AlertDialog.Builder(requireContext())
        val v = layoutInflater.inflate(R.layout.dialog_add_water, null)
        val input = v.findViewById<EditText>(R.id.etWaterAmount)
        dlg.setView(v)
        dlg.setTitle("Add water (ml)")
        dlg.setPositiveButton("Add") { dialog, _ ->
            val amt = input.text.toString().trim().toIntOrNull() ?: 0
            if (amt <= 0 || amt > 2000) {
                Toast.makeText(context, "Enter amount between 1 and 2000 ml", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            addWater(amt)
            dialog.dismiss()
        }
        dlg.setNegativeButton("Cancel") { d, _ -> d.cancel() }
        dlg.show()
    }

    private fun addWater(amountMl: Int) {
        lifecycleScope.launch {
            try {
                val now = Calendar.getInstance()
                val date = dateFormat.format(now.time)
                val time = timeFormat.format(now.time)

                val rec = HydrationRecord(0, amountMl, date, time) // ID will be auto-generated
                databaseManager.hydrationRecordRepository.saveHydrationRecord(rec)
                
                animateCupToCurrent()
                val reached = updateUiForToday()
                if (reached) {
                    Toast.makeText(context, "Your Today Hydarate goal Complete Sucess", Toast.LENGTH_LONG).show()
                }
                updateWeeklyBars(view ?: return@launch)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getTodayIntake(): Int {
        return runBlocking { databaseManager.hydrationRecordRepository.getTodayWaterIntake() }
    }

    private fun animateCupToCurrent() {
        val total = getTodayIntake()
        val fraction = (total.toFloat() / dailyGoalMl).coerceIn(0f, 1f)
        val targetHeight = (fraction * dpToPx(220f)).toInt()
        val startHeight = waterFill.layoutParams.height
        val animator = ValueAnimator.ofInt(startHeight, targetHeight)
        animator.duration = 600
        animator.addUpdateListener {
            val h = it.animatedValue as Int
            val lp = waterFill.layoutParams
            lp.height = h
            waterFill.layoutParams = lp
        }
        animator.start()
        progressHydration.progress = (fraction * 100).toInt()
    }

    private fun dpToPx(dp: Float): Float {
        val density = resources.displayMetrics.density
        return dp * density
    }

    private fun updateUiForToday(): Boolean {
        val total = getTodayIntake()
        tvDailyIntake.text = "$total / $dailyGoalMl ml"
        val fraction = (total.toFloat() / dailyGoalMl).coerceIn(0f, 1f)
        progressHydration.progress = (fraction * 100).toInt()
        // Ensure fill matches current without animating from 0 each time on first load
        waterFill.post { animateCupToCurrent() }
        return total >= dailyGoalMl
    }

    private fun updateWeeklyBars(root: View) {
        try {
            val bars = arrayOf(
                root.findViewById<View>(R.id.pbD0),
                root.findViewById<View>(R.id.pbD1),
                root.findViewById<View>(R.id.pbD2),
                root.findViewById<View>(R.id.pbD3),
                root.findViewById<View>(R.id.pbD4),
                root.findViewById<View>(R.id.pbD5),
                root.findViewById<View>(R.id.pbD6)
            )
            val labels = arrayOf(
                root.findViewById<TextView>(R.id.tvD0),
                root.findViewById<TextView>(R.id.tvD1),
                root.findViewById<TextView>(R.id.tvD2),
                root.findViewById<TextView>(R.id.tvD3),
                root.findViewById<TextView>(R.id.tvD4),
                root.findViewById<TextView>(R.id.tvD5),
                root.findViewById<TextView>(R.id.tvD6)
            )

            val cal = Calendar.getInstance()
            val today = Calendar.getInstance()
            
            // Move to Monday
            cal.firstDayOfWeek = Calendar.MONDAY
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            val dayNames = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val maxBarHeight = 110
            
            for (i in 0..6) {
                val dateStr = dateFormat.format(cal.time)
                val totalForDay: Int = runBlocking { 
                    databaseManager.hydrationRecordRepository.getHydrationRecords()
                        .first()
                        .filter { record -> record.date == dateStr }
                        .sumOf { record -> record.amount }
                }
                
                val pct = ((totalForDay.toFloat() / dailyGoalMl) * 100).toInt().coerceIn(0, 100)
                val targetHeight = (maxBarHeight * pct / 100f).toInt()
                
                // Set color based on percentage
                val color = when {
                    pct >= 100 -> androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary_green)
                    pct >= 75 -> androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_green_light)
                    pct >= 50 -> androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light)
                    else -> androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
                }
                
                // Create colored drawable
                val drawable = androidx.core.graphics.drawable.DrawableCompat.wrap(
                    androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.vertical_bar_fill)!!
                ).mutate()
                androidx.core.graphics.drawable.DrawableCompat.setTint(drawable, color)
                
                // Animate bar
                bars[i].apply {
                    background = drawable
                    
                    // Start from 0 height
                    layoutParams.height = 0
                    requestLayout()
                    
                    // Animate to target height
                    val animator = ValueAnimator.ofInt(0, targetHeight)
                    animator.duration = 800 + (i * 100L) // Staggered animation
                    animator.addUpdateListener { animation ->
                        val height = animation.animatedValue as Int
                        val dpHeight = (height * resources.displayMetrics.density).toInt()
                        layoutParams.height = dpHeight
                        requestLayout()
                    }
                    animator.start()
                }
                
                // Update label
                labels[i].apply {
                    text = dayNames[i]
                    
                    // Highlight today
                    val isToday = dateFormat.format(cal.time) == dateFormat.format(today.time)
                    
                    if (isToday) {
                        setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.primary_green))
                        textSize = 13f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    } else {
                        setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.text_secondary))
                        textSize = 12f
                        typeface = android.graphics.Typeface.DEFAULT
                    }
                }
                
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupReminderSettings() {
        lifecycleScope.launch {
            val hydrationEnabled = databaseManager.appSettingsRepository.isHydrationReminderEnabled()
            val currentInterval = databaseManager.appSettingsRepository.getHydrationReminderInterval()
            
            switchHydrationReminder.isChecked = hydrationEnabled
            seekBarReminderInterval.progress = currentInterval
            updateReminderIntervalText(currentInterval)

            switchHydrationReminder.setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    if (isChecked) {
                        if (checkNotificationPermission()) {
                            databaseManager.appSettingsRepository.setHydrationReminderEnabled(true)
                            scheduleHydrationReminders()
                        } else {
                            switchHydrationReminder.isChecked = false
                        }
                    } else {
                        databaseManager.appSettingsRepository.setHydrationReminderEnabled(false)
                        cancelHydrationReminders()
                    }
                }
            }

            seekBarReminderInterval.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        updateReminderIntervalText(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val interval = seekBarReminderInterval.progress
                    lifecycleScope.launch {
                        databaseManager.appSettingsRepository.setHydrationReminderInterval(interval)
                        if (databaseManager.appSettingsRepository.isHydrationReminderEnabled()) {
                            cancelHydrationReminders()
                            scheduleHydrationReminders()
                        }
                    }
                }
            })
        }
    }

    private fun updateReminderIntervalText(intervalMinutes: Int) {
        tvReminderInterval.text = "$intervalMinutes minutes"
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    true
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    showNotificationPermissionDialog()
                    false
                }
                else -> {
                    requestNotificationPermission()
                    false
                }
            }
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }

    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Notification Permission Required")
            .setMessage("Hydration reminders need notification permission to work. Please enable notifications in your device settings.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = android.net.Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun scheduleHydrationReminders() {
        try {
            val ctx = requireContext()
            val alarm = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(ctx, HydrationReminderReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                ctx,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val cal = Calendar.getInstance()
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.MINUTE, 1)

            val interval = runBlocking { databaseManager.appSettingsRepository.getHydrationReminderInterval() } * 60 * 1000L

            // Schedule repeating alarm
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarm.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis,
                    pi
                )

                scheduleNextReminder(ctx, alarm, pi, interval)
            } else {
                alarm.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis,
                    interval,
                    pi
                )
            }
            Toast.makeText(context, "Hydration reminders enabled", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to enable reminders: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleNextReminder(context: Context, alarmManager: AlarmManager, pendingIntent: PendingIntent, interval: Long) {
        val nextAlarmTime = System.currentTimeMillis() + interval
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextAlarmTime,
                pendingIntent
            )
        }
    }

    private fun cancelHydrationReminders() {
        try {
            val ctx = requireContext()
            val alarm = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(ctx, HydrationReminderReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                ctx,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarm.cancel(pi)
            Toast.makeText(context, "Hydration reminders disabled", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}