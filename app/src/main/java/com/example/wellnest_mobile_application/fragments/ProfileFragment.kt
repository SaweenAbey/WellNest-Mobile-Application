package com.example.wellnest_mobile_application.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.wellnest_mobile_application.R
import com.example.wellnest_mobile_application.data.SharedPrefManager
import com.example.wellnest_mobile_application.models.User
import com.example.wellnest_mobile_application.Sign_Up
import androidx.appcompat.app.AppCompatDelegate
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import java.util.Calendar
import com.example.wellnest_mobile_application.notifications.HydrationReminderReceiver

class ProfileFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button
    private lateinit var switchNotifications: Switch
    private lateinit var switchHydration: Switch
    private lateinit var switchDark: Switch
    private lateinit var btnExport: Button
    private lateinit var pref: SharedPrefManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        pref = SharedPrefManager(requireContext())

        tvName = view.findViewById(R.id.tvProfileName)
        tvEmail = view.findViewById(R.id.tvProfileEmail)
        btnEdit = view.findViewById(R.id.btnEditProfile)
        btnLogout = view.findViewById(R.id.btnLogout)
        switchNotifications = view.findViewById(R.id.switchNotifications)
        switchHydration = view.findViewById(R.id.switchHydration)
        switchDark = view.findViewById(R.id.switchDark)
        btnExport = view.findViewById(R.id.btnExportData)

        bindUser()

        // Bind settings state
        switchNotifications.isChecked = pref.areNotificationsEnabled()
        switchHydration.isChecked = pref.isHydrationReminderEnabled()
        switchDark.isChecked = pref.isDarkModeEnabled()

        // Listeners
        btnEdit.setOnClickListener { showEditDialog() }
        btnLogout.setOnClickListener { confirmLogout() }
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            pref.setNotificationsEnabled(isChecked)
        }
        switchHydration.setOnCheckedChangeListener { _, isChecked ->
            pref.setHydrationReminderEnabled(isChecked)
            if (isChecked) scheduleHydrationReminders() else cancelHydrationReminders()
        }
        switchDark.setOnCheckedChangeListener { _, isChecked ->
            pref.setDarkModeEnabled(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        btnExport.setOnClickListener { exportData() }

        return view
    }

    private fun bindUser() {
        val user = pref.getUser()
        if (user != null) {
            tvName.text = user.fullName
            tvEmail.text = user.email
        } else {
            tvName.text = "Guest"
            tvEmail.text = "Not logged in"
        }
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

    private fun exportData() {
        try {
            val habits = pref.getHabits()
            val moods = pref.getMoodEntries()
            val waters = pref.getHydrationRecords()
            val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
            val exportObj = mapOf(
                "habits" to habits,
                "moods" to moods,
                "hydration" to waters
            )
            val json = gson.toJson(exportObj)

            val intent = android.content.Intent(android.content.Intent.ACTION_SEND)
            intent.type = "application/json"
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Wellnest Data Export")
            intent.putExtra(android.content.Intent.EXTRA_TEXT, json)
            startActivity(android.content.Intent.createChooser(intent, "Share data"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditDialog() {
        val user = pref.getUser()
        if (user == null) {
            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val dlg = AlertDialog.Builder(requireContext())
        val v = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = v.findViewById<EditText>(R.id.etName)
        val etEmail = v.findViewById<EditText>(R.id.etEmail)

        etName.setText(user.fullName)
        etEmail.setText(user.email)

        dlg.setTitle("Edit Profile")
        dlg.setView(v)
        dlg.setPositiveButton("Save") { dialog, _ ->
            val newName = etName.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()
            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(context, "Name and email required", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            pref.saveUser(User(fullName = newName, email = newEmail, password = user.password, registrationDate = user.registrationDate))
            bindUser()
            dialog.dismiss()
        }
        dlg.setNegativeButton("Cancel") { d, _ -> d.cancel() }
        dlg.show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                pref.logout()
                val intent = Intent(requireContext(), Sign_Up::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("No", null)
            .show()
    }
}