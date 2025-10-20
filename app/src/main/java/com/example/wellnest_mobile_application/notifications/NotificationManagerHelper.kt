package com.example.wellnest_mobile_application.notifications

import android.content.Context
import com.example.wellnest_mobile_application.database.DatabaseManager
import com.example.wellnest_mobile_application.models.NotificationSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationManagerHelper {

    fun setNotificationSettings(
        context: Context,
        hydrated: Boolean = true,
        dailySummary: Boolean = true,
        hydrationReminder: Boolean = true,
        hydrationInterval: Int = 60
    ) {
        val databaseManager = DatabaseManager(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                databaseManager.appSettingsRepository.setNotificationsEnabled(hydrated)
                // Note: Daily summary functionality can be added later if needed
                databaseManager.appSettingsRepository.setHydrationReminderEnabled(hydrationReminder)
                databaseManager.appSettingsRepository.setHydrationReminderInterval(hydrationInterval)
                
                if (hydrated && hydrationReminder) {
                    android.content.Intent(context, HydrationReminderReceiver::class.java).let { intent ->
                        val pi = android.app.PendingIntent.getBroadcast(
                            context,
                            1001,
                            intent,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                        )
                        
                        val alarm = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                        alarm.cancel(pi)
                        
                        val cal = java.util.Calendar.getInstance()
                        cal.timeInMillis = System.currentTimeMillis()
                        cal.add(java.util.Calendar.MINUTE, 1)
                        
                        val intervalMs = hydrationInterval * 60 * 1000L
                        
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            alarm.setExactAndAllowWhileIdle(
                                android.app.AlarmManager.RTC_WAKEUP,
                                cal.timeInMillis,
                                pi
                            )
                        } else {
                            alarm.setRepeating(
                                android.app.AlarmManager.RTC_WAKEUP,
                                cal.timeInMillis,
                                intervalMs,
                                pi
                            )
                        }
                    }
                } else {
                    cancelHydrationReminders(context)
                }
                
                // Note: Daily summary functionality can be added later if needed
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun getNotificationStatus(context: Context): NotificationSettings {
        val databaseManager = DatabaseManager(context)
        return NotificationSettings(
            notificationsEnabled = databaseManager.appSettingsRepository.areNotificationsEnabled(),
            hydrationRemindersEnabled = databaseManager.appSettingsRepository.isHydrationReminderEnabled(),
            dailySummaryEnabled = false, // Note: Daily summary functionality can be added later
            hydrationReminderInterval = databaseManager.appSettingsRepository.getHydrationReminderInterval(),
            todayRemindersSent = 0 // Note: Reminder counting can be added later if needed
        )
    }

    private fun cancelHydrationReminders(context: Context) {
        try {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = android.content.Intent(context, HydrationReminderReceiver::class.java)
            val pi = android.app.PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            alarm.cancel(pi)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    

    fun sendTestNotification(context: Context, type: String = "test") {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = when (type) {
            "hydration" -> "hydration_reminders"
            "daily_summary" -> "daily_summary"
            else -> "default"
        }
        

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Test Notifications",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Test notifications for debugging"
            manager.createNotificationChannel(channel)
        }
        
        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.wellnest_mobile_application.R.drawable.ic_water)
            .setContentTitle("🔔 Test Notification")
            .setContentText("This is a test notification from Wellnest")
            .setAutoCancel(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        manager.notify(9999, notification)
    }
}
