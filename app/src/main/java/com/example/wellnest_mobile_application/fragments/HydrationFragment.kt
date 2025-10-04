package com.example.wellnest_mobile_application.fragments

import android.animation.ValueAnimator
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.example.wellnest_mobile_application.data.SharedPrefManager
import com.example.wellnest_mobile_application.models.HydrationRecord
import com.example.wellnest_mobile_application.notifications.HydrationReminderReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HydrationFragment : Fragment() {

    private lateinit var pref: SharedPrefManager
    private lateinit var tvDailyIntake: TextView
    private lateinit var progressHydration: ProgressBar
    private lateinit var waterFill: View
    private lateinit var btnAdd150: Button
    private lateinit var btnAdd200: Button
    private lateinit var btnAddCustom: Button
    private lateinit var switchHydrationReminder: Switch
    private lateinit var tvReminderInterval: TextView
    private lateinit var seekBarReminderInterval: SeekBar

    private val dailyGoalMl = 4000
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hydration, container, false)

        pref = SharedPrefManager(requireContext())
        tvDailyIntake = view.findViewById(R.id.tvDailyIntake)
        progressHydration = view.findViewById(R.id.progressHydration)
        waterFill = view.findViewById(R.id.waterFill)
        btnAdd150 = view.findViewById(R.id.btnAdd150)
        btnAdd200 = view.findViewById(R.id.btnAdd200)
        btnAddCustom = view.findViewById(R.id.btnAddCustom)
        switchHydrationReminder = view.findViewById(R.id.switchHydrationReminder)
        tvReminderInterval = view.findViewById(R.id.tvReminderInterval)
        seekBarReminderInterval = view.findViewById(R.id.seekBarReminderInterval)

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
        try {
            val now = Calendar.getInstance()
            val date = dateFormat.format(now.time)
            val time = timeFormat.format(now.time)

            val existing = pref.getHydrationRecords()
            val newId = if (existing.isNotEmpty()) existing.maxOf { it.id } + 1 else 1
            val rec = HydrationRecord(newId, amountMl, date, time)
            pref.saveHydrationRecord(rec)
            animateCupToCurrent()
            val reached = updateUiForToday()
            if (reached) {
                Toast.makeText(context, "Your Today Hydarate goal Complete Sucess", Toast.LENGTH_LONG).show()
            }
            updateWeeklyBars(view ?: return)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getTodayIntake(): Int {
        return pref.getTodayWaterIntake()
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
                root.findViewById<ProgressBar>(R.id.pbD0),
                root.findViewById<ProgressBar>(R.id.pbD1),
                root.findViewById<ProgressBar>(R.id.pbD2),
                root.findViewById<ProgressBar>(R.id.pbD3),
                root.findViewById<ProgressBar>(R.id.pbD4),
                root.findViewById<ProgressBar>(R.id.pbD5),
                root.findViewById<ProgressBar>(R.id.pbD6)
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
            // Move to Monday
            cal.firstDayOfWeek = Calendar.MONDAY
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            val dayNames = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            for (i in 0..6) {
                val dateStr = dateFormat.format(cal.time)
                val totalForDay = pref.getHydrationRecords()
                    .filter { it.date == dateStr }
                    .sumOf { it.amount }
                val pct = ((totalForDay.toFloat() / dailyGoalMl) * 100).toInt().coerceIn(0, 100)
                bars[i].progress = pct
                labels[i].text = dayNames[i]
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupReminderSettings() {
        // Initialize reminder settings
        switchHydrationReminder.isChecked = pref.isHydrationReminderEnabled()
        val currentInterval = pref.getHydrationReminderInterval()
        seekBarReminderInterval.progress = currentInterval
        updateReminderIntervalText(currentInterval)

        // Set up listeners
        switchHydrationReminder.setOnCheckedChangeListener { _, isChecked ->
            pref.setHydrationReminderEnabled(isChecked)
            if (isChecked) {
                scheduleHydrationReminders()
            } else {
                cancelHydrationReminders()
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
                pref.setHydrationReminderInterval(interval)
                if (pref.isHydrationReminderEnabled()) {
                    // Reschedule with new interval
                    cancelHydrationReminders()
                    scheduleHydrationReminders()
                }
            }
        })
    }

    private fun updateReminderIntervalText(intervalMinutes: Int) {
        tvReminderInterval.text = "$intervalMinutes minutes"
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
            cal.add(Calendar.MINUTE, 1) // first trigger in 1 minute

            val interval = pref.getHydrationReminderInterval() * 60 * 1000L // convert minutes to milliseconds

            alarm.setRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                interval,
                pi
            )
            Toast.makeText(context, "Hydration reminders enabled", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
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