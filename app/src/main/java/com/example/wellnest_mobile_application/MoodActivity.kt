package com.example.wellnest_mobile_application

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnest_mobile_application.database.DatabaseManager
import com.example.wellnest_mobile_application.models.MoodEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MoodActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvSelectedMood: TextView
    private lateinit var tvMoodLabel: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView
    private lateinit var etNotes: EditText
    private lateinit var btnDone: Button

    private lateinit var emoji1: ImageView
    private lateinit var emoji2: ImageView
    private lateinit var emoji3: ImageView
    private lateinit var emoji4: ImageView
    private lateinit var emoji5: ImageView
    private lateinit var emoji6: ImageView
    
    private lateinit var databaseManager: DatabaseManager
    private var selectedMood = "Happy"
    private var selectedEmoji = "😊"
    
    private val moods = arrayOf("Happy", "Sad", "Angry", "Anxious", "Excited")
    private val emojis = arrayOf("😊", "😢", "😡", "😰", "😃")
    private val moodColors = arrayOf("#FFD700", "#87CEEB", "#FF6B6B", "#FFA07A", "#98FB98")

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
        
        setContentView(R.layout.activity_mood)
        
        databaseManager = DatabaseManager(this)
        
        initializeViews()
        setupListeners()
        updateDateTime()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        tvSelectedMood = findViewById(R.id.tvSelectedMood)
        tvMoodLabel = findViewById(R.id.tvMoodLabel)
        tvDate = findViewById(R.id.tvDate)
        tvTime = findViewById(R.id.tvTime)
        etNotes = findViewById(R.id.etNotes)
        btnDone = findViewById(R.id.btnDone)
        

        emoji1 = findViewById(R.id.emoji1)
        emoji2 = findViewById(R.id.emoji2)
        emoji3 = findViewById(R.id.emoji3)
        emoji4 = findViewById(R.id.emoji4)
        emoji5 = findViewById(R.id.emoji5)
        emoji6 = findViewById(R.id.emoji6)
        

        updateMoodSelection(0)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnDone.setOnClickListener {
            saveMood()
        }


        emoji1.setOnClickListener { updateMoodSelection(0) }
        emoji2.setOnClickListener { updateMoodSelection(1) }
        emoji3.setOnClickListener { updateMoodSelection(2) }
        emoji4.setOnClickListener { updateMoodSelection(3) }
        emoji5.setOnClickListener { updateMoodSelection(4) }
        emoji6.setOnClickListener { updateMoodSelection(5) }
    }

    private fun updateMoodSelection(index: Int) {
        selectedMood = moods[index]
        selectedEmoji = emojis[index]
        

        tvSelectedMood.text = selectedEmoji
        tvMoodLabel.text = "Feeling $selectedMood"
        

        val color = android.graphics.Color.parseColor(moodColors[index])
        findViewById<View>(R.id.moodContainer).setBackgroundColor(color)
        

        val emojiViews = arrayOf(emoji1, emoji2, emoji3, emoji4, emoji5, emoji6)
        emojiViews.forEach { it.background = null }
        

        emojiViews[index].background = getDrawable(R.drawable.emoji_selected_bg)
    }

    private fun updateDateTime() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        
        tvDate.text = dateFormat.format(calendar.time)
        tvTime.text = timeFormat.format(calendar.time)
    }

    private fun saveMood() {
        try {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            
            val date = dateFormat.format(calendar.time)
            val time = timeFormat.format(calendar.time)
            val note = etNotes.text.toString().trim()
            
            val moodEntry = MoodEntry(
                id = 0, // Will be auto-generated by database
                mood = selectedMood,
                emoji = selectedEmoji,
                date = date,
                time = time,
                note = note,
                durationMinutes = 0
            )
            
            CoroutineScope(Dispatchers.IO).launch {
                databaseManager.moodEntryRepository.saveMoodEntry(moodEntry)
                
                runOnUiThread {
                    Toast.makeText(this@MoodActivity, "Mood saved successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving mood", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
