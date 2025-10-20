package com.example.wellnest_mobile_application.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.wellnest_mobile_application.R
import com.example.wellnest_mobile_application.database.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class HydrationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val databaseManager = DatabaseManager(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notificationsEnabled = databaseManager.appSettingsRepository.areNotificationsEnabled()
                val hydrationEnabled = databaseManager.appSettingsRepository.isHydrationReminderEnabled()
                
                if (!notificationsEnabled || !hydrationEnabled) return@launch

                // Note: Activity tracking can be implemented later if needed
                // For now, we'll focus on the core notification functionality

                showNotification(context)

                scheduleNextReminder(context, databaseManager)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showNotification(context: Context) {
        val channelId = "hydration_reminders"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Reminds you to drink water regularly"
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_water)
            .setContentTitle("💧 Alert: Time to Hydrate!")
            .setContentText("Stay healthy - drink some water and log it in Wellnest")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(1001, notification)
    }

    private suspend fun scheduleNextReminder(context: Context, databaseManager: DatabaseManager) {
        try {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, HydrationReminderReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val interval = databaseManager.appSettingsRepository.getHydrationReminderInterval() * 60 * 1000L
            val nextAlarmTime = System.currentTimeMillis() + interval

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarm.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pi
                )
            } else {
                alarm.set(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pi
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


