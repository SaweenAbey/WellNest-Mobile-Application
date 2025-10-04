package com.example.wellnest_mobile_application.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.wellnest_mobile_application.R
import com.example.wellnest_mobile_application.data.SharedPrefManager

class HydrationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pref = SharedPrefManager(context)
        if (!pref.areNotificationsEnabled() || !pref.isHydrationReminderEnabled()) return

        val channelId = "hydration_reminders"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_water)
            .setContentTitle("Time to hydrate")
            .setContentText("Drink some water and log it in Wellnest")
            .setAutoCancel(true)
            .build()
        manager.notify(1001, notification)
    }
}


