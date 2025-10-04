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
import com.example.wellnest_mobile_application.data.SharedPrefManager

class SnapActivity : AppCompatActivity() {

    private lateinit var tvCountdown: TextView
    private lateinit var btnStartStop: Button
    private lateinit var btnAddTime: Button
    private lateinit var seekBarDuration: SeekBar
    private lateinit var tvDuration: TextView
    private lateinit var btnMusic: Button
    private lateinit var btnBack: Button
    private lateinit var btnCustomTime: Button
    
    // Star and bed icons for animation
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
    private var totalTimeInMillis = 60000L // Default 1 minute
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var pref: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make full screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        setContentView(R.layout.activity_snap)
        
        pref = SharedPrefManager(this)
        
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
        
        // Initialize star and bed icons
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
        val savedDuration = pref.getSnapDuration()
        if (savedDuration > 0) {
            totalTimeInMillis = savedDuration.toLong()
            timeLeftInMillis = totalTimeInMillis
        }
        
        seekBarDuration.progress = (totalTimeInMillis / 1000).toInt()
        updateCountdownText()
        updateDurationText()
    }

    private fun saveSettings() {
        pref.setSnapDuration((totalTimeInMillis / 1000).toInt())
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
            totalTimeInMillis += 30000 // Add 30 seconds
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
            // Create a simple tone for background music
            // In a real app, you would load actual music files
            mediaPlayer?.release()
            // For now, we'll create a simple MediaPlayer without a resource
            mediaPlayer = MediaPlayer()
            mediaPlayer?.isLooping = true
            // Note: In a real implementation, you would load actual music files
            // mediaPlayer?.setDataSource("path/to/music/file.mp3")
            // mediaPlayer?.prepare()
            // mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playCompletionSound() {
        try {
            // For now, we'll just show a visual indication that the timer is complete
            // In a real implementation, you would load actual sound files
            // val completionPlayer = MediaPlayer.create(this, R.raw.completion_sound)
            // completionPlayer?.start()
            // completionPlayer?.setOnCompletionListener { it.release() }
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

        // Pre-fill with current duration
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
        // Animate stars with twinkling effect
        animateStar(star1, 2000, 0.3f, 0.9f)
        animateStar(star2, 1500, 0.2f, 0.8f)
        animateStar(star3, 2500, 0.4f, 1.0f)
        animateStar(star4, 1800, 0.3f, 0.7f)
        animateStar(star5, 2200, 0.5f, 1.0f)
        
        // Animate bed icons with gentle floating effect
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
