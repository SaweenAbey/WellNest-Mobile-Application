package com.example.wellnest_mobile_application.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.example.wellnest_mobile_application.database.DatabaseManager
import com.example.wellnest_mobile_application.models.Habit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HabitTimerService : Service() {

    private lateinit var databaseManager: DatabaseManager
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    companion object {
        private const val TIMER_INTERVAL = 60000L // 1 minute
    }

    override fun onCreate() {
        super.onCreate()
        databaseManager = DatabaseManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            startTimer()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTimer() {
        handler.post(timerRunnable)
    }

    private val timerRunnable: Runnable = object : Runnable {
        override fun run() {
            serviceScope.launch {
                try {
                    // Explicit type declarations to avoid type-inference recursion
                    val habits: List<Habit> = databaseManager.habitRepository
                        .getHabits()
                        .first()

                    val activeHabits: List<Habit> = habits.filter { !it.isCompleted }

                    if (activeHabits.isNotEmpty()) {
                        // 🔹 You can later add countdown or progress update logic here
                        scheduleNextTimer()
                    } else {
                        // Stop service when no active habits
                        stopService()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    scheduleNextTimer()
                }
            }
        }
    }

    private fun scheduleNextTimer() {
        val runnable: Runnable = timerRunnable
        val handler: Handler = this.handler
        val interval: Long = TIMER_INTERVAL
        handler.postDelayed(runnable, interval)
    }

    private fun stopService() {
        val handler: Handler = this.handler
        handler.post {
            isRunning = false
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
        serviceScope.coroutineContext.cancel()
        isRunning = false
    }
}
