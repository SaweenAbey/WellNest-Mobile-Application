package com.example.wellnest_mobile_application.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.wellnest_mobile_application.database.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            val databaseManager = DatabaseManager(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val hydrationEnabled = databaseManager.appSettingsRepository.isHydrationReminderEnabled()
                    val notificationsEnabled = databaseManager.appSettingsRepository.areNotificationsEnabled()
                    
                    if (hydrationEnabled && notificationsEnabled) {
                        scheduleHydrationRemindersAfterBoot(context, databaseManager)
                    }
                    
                    // Note: Daily summary functionality can be added later if needed
                    // For now, we'll skip the daily summary check since it's not implemented in the database
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun scheduleHydrationRemindersAfterBoot(context: Context, databaseManager: DatabaseManager) {
        try {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, HydrationReminderReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val cal = Calendar.getInstance()
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.MINUTE, 1)

            val interval = databaseManager.appSettingsRepository.getHydrationReminderInterval() * 60 * 1000L

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarm.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis,
                    pi
                )
            } else {
                alarm.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis,
                    interval,
                    pi
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
