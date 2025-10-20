package com.example.wellnest_mobile_application

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnest_mobile_application.database.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SnapActivity : AppCompatActivity() {

    private lateinit var tvCountdown: TextView
    private lateinit var btnStartStop: Button
    private lateinit var btnAddTime: Button
    private lateinit var seekBarDuration: SeekBar
    private lateinit var tvDuration: TextView
    private lateinit var btnMusic: Button
    private lateinit var btnBack: Button
    private lateinit var btnCustomTime: Button
    

    private lateinit var star1: ImageView
    private lateinit var star2: ImageView
    private lateinit var star3: ImageView
    private lateinit var star4: ImageView
    private lateinit var star5: ImageView
    private lateinit var bed1: ImageView
    private lateinit var bed2: ImageView
    private lateinit var bed3: ImageView
    
    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false
    private var timeLeftInMillis = 0L
    private var totalTimeInMillis = 60000L
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var databaseManager: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.decorView.systemUiVisibility = 
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        
        setContentView(R.layout.activity_snap)
        
        databaseManager = DatabaseManager(this)
        
        initializeViews()
        setupListeners()
        loadSettings()
        startBackgroundAnimations()
    }

    private fun initializeViews() {
        tvCountdown = findViewById(R.id.tvCountdown)
        btnStartStop = findViewById(R.id.btnStartStop)
        btnAddTime = findViewById(R.id.btnAddTime)
        seekBarDuration = findViewById(R.id.seekBarDuration)
        tvDuration = findViewById(R.id.tvDuration)
        btnMusic = findViewById(R.id.btnMusic)
        btnBack = findViewById(R.id.btnBack)
        btnCustomTime = findViewById(R.id.btnCustomTime)
        

        star1 = findViewById(R.id.star1)
        star2 = findViewById(R.id.star2)
        star3 = findViewById(R.id.star3)
        star4 = findViewById(R.id.star4)
        star5 = findViewById(R.id.star5)
        bed1 = findViewById(R.id.bed1)
        bed2 = findViewById(R.id.bed2)
        bed3 = findViewById(R.id.bed3)
    }

    private fun setupListeners() {
        btnStartStop.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        btnAddTime.setOnClickListener {
            addTime()
        }

        btnMusic.setOnClickListener {
            toggleMusic()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnCustomTime.setOnClickListener {
            showCustomTimeDialog()
        }

        seekBarDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && !isRunning) {
                    totalTimeInMillis = (progress * 1000).toLong()
                    timeLeftInMillis = totalTimeInMillis
                    updateCountdownText()
                    updateDurationText()
                    saveSettings()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun loadSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            val savedDuration = databaseManager.appSettingsRepository.getSnapDuration()
            runOnUiThread {
                if (savedDuration > 0) {
                    totalTimeInMillis = savedDuration.toLong()
                    timeLeftInMillis = totalTimeInMillis
                }
                
                seekBarDuration.progress = (totalTimeInMillis / 1000).toInt()
                updateCountdownText()
                updateDurationText()
            }
        }
    }

    private fun saveSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            databaseManager.appSettingsRepository.setSnapDuration((totalTimeInMillis / 1000).toInt())
        }
    }

    private fun startTimer() {
        if (timeLeftInMillis <= 0) {
            timeLeftInMillis = totalTimeInMillis
        }

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountdownText()
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                updateCountdownText()
                isRunning = false
                updateStartStopButton()
                playCompletionSound()
            }
        }.start()

        isRunning = true
        updateStartStopButton()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isRunning = false
        updateStartStopButton()
    }

    private fun addTime() {
        if (!isRunning) {
            totalTimeInMillis += 30000
            timeLeftInMillis = totalTimeInMillis
            seekBarDuration.progress = (totalTimeInMillis / 1000).toInt()
            updateCountdownText()
            updateDurationText()
            saveSettings()
        }
    }

    private fun toggleMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            btnMusic.text = "🎵 Play Music"
        } else {
            playBackgroundMusic()
            btnMusic.text = "🔇 Stop Music"
        }
    }

    private fun playBackgroundMusic() {
        try {

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer()
            mediaPlayer?.isLooping = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playCompletionSound() {
        try {
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateCountdownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        tvCountdown.text = timeFormatted
    }

    private fun updateDurationText() {
        val minutes = (totalTimeInMillis / 1000) / 60
        val seconds = (totalTimeInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        tvDuration.text = "Duration: $timeFormatted"
    }

    private fun updateStartStopButton() {
        btnStartStop.text = if (isRunning) "⏸️ Pause" else "▶️ Start"
    }

    private fun showCustomTimeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_time, null)
        val etMinutes = dialogView.findViewById<EditText>(R.id.etMinutes)
        val etSeconds = dialogView.findViewById<EditText>(R.id.etSeconds)


        val currentMinutes = (totalTimeInMillis / 1000) / 60
        val currentSeconds = (totalTimeInMillis / 1000) % 60
        etMinutes.setText(currentMinutes.toString())
        etSeconds.setText(currentSeconds.toString())

        val dialog = AlertDialog.Builder(this)
            .setTitle("Set Custom Time")
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                val minutes = etMinutes.text.toString().toIntOrNull() ?: 0
                val seconds = etSeconds.text.toString().toIntOrNull() ?: 0
                val totalSeconds = minutes * 60 + seconds

                if (totalSeconds < 15) {
                    Toast.makeText(this, "Minimum time is 15 seconds", Toast.LENGTH_SHORT).show()
                } else if (totalSeconds > 3000) {
                    Toast.makeText(this, "Maximum time is 50 minutes", Toast.LENGTH_SHORT).show()
                } else {
                    totalTimeInMillis = (totalSeconds * 1000).toLong()
                    timeLeftInMillis = totalTimeInMillis
                    seekBarDuration.progress = totalSeconds
                    updateCountdownText()
                    updateDurationText()
                    saveSettings()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun startBackgroundAnimations() {

        animateStar(star1, 2000, 0.3f, 0.9f)
        animateStar(star2, 1500, 0.2f, 0.8f)
        animateStar(star3, 2500, 0.4f, 1.0f)
        animateStar(star4, 1800, 0.3f, 0.7f)
        animateStar(star5, 2200, 0.5f, 1.0f)
        

        animateBed(bed1, 3000, 10f)
        animateBed(bed2, 4000, 15f)
        animateBed(bed3, 3500, 12f)
    }

    private fun animateStar(star: ImageView, duration: Long, minAlpha: Float, maxAlpha: Float) {
        val alphaAnimator = ObjectAnimator.ofFloat(star, "alpha", minAlpha, maxAlpha)
        alphaAnimator.duration = duration
        alphaAnimator.repeatCount = ValueAnimator.INFINITE
        alphaAnimator.repeatMode = ValueAnimator.REVERSE
        alphaAnimator.interpolator = AccelerateDecelerateInterpolator()
        alphaAnimator.start()
    }

    private fun animateBed(bed: ImageView, duration: Long, translationY: Float) {
        val translateAnimator = ObjectAnimator.ofFloat(bed, "translationY", -translationY, translationY)
        translateAnimator.duration = duration
        translateAnimator.repeatCount = ValueAnimator.INFINITE
        translateAnimator.repeatMode = ValueAnimator.REVERSE
        translateAnimator.interpolator = AccelerateDecelerateInterpolator()
        translateAnimator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        mediaPlayer?.release()
    }

    override fun onPause() {
        super.onPause()
        if (isRunning) {
            pauseTimer()
        }
    }
}
